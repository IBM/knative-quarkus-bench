package com.ibm.trl.knativebench;

import org.jboss.logging.Logger;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.List;

import io.quarkus.funqy.Funq;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;


public class Uploader {


    private Logger log;
    private UUID uuid;
    private String AWS_REGION = "ap-south-1";
    private String AWS_ENDPOINT = "defaultvalue";
    @ConfigProperty(name = "knativebench.uploader.input_bucket", defaultValue = "")
    String input_bucket;
    @ConfigProperty(name = "knativebench.uploader.output_bucket", defaultValue = "")
    String output_bucket;
    private Region region = Region.AP_SOUTH_1; // any region is OK
    private URI endpointOverride = null;
    private String access_key_id = null;
    private String secret_access_key = null;
    private StaticCredentialsProvider credential = null;
    private S3Client s3 = null;


    private void StorageSetup() throws Exception {
        String value;

        if ((value = System.getenv("AWS_ENDPOINT")) != null) {
            AWS_ENDPOINT = value;
            endpointOverride = URI.create(AWS_ENDPOINT);
        }

        if ((value = System.getenv("AWS_ACCESS_KEY_ID")) != null)
            access_key_id = System.getenv("AWS_ACCESS_KEY_ID");

        if ((value = System.getenv("AWS_SECRET_ACCESS_KEY")) != null)
            secret_access_key = System.getenv("AWS_SECRET_ACCESS_KEY");

        if ((value = System.getenv("AWS_REGION")) != null) {
            AWS_REGION = value;
            region = Region.of(AWS_REGION);
        } 

        credential = StaticCredentialsProvider
            .create(AwsBasicCredentials.create(access_key_id, secret_access_key));

        s3 = S3Client.builder().region(region).endpointOverride(endpointOverride)
            .credentialsProvider(credential).build();
    }

    private void deleteFile(String bucket, String key) {
        log.info("Deleting "+key+" from bucket "+bucket+".");
        DeleteObjectRequest deleteObjectRequest =
            DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3.deleteObject(deleteObjectRequest);
        return;
    }

    private void uploadFile(String bucket, String key, String filePath) {
        log.info("Uploading "+filePath+" as "+key+" to bucket "+bucket+".");
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
        s3.putObject(objectRequest, RequestBody.fromFile(new File(filePath).toPath()));
        return;
    }

    private void downloadFile(String bucket, String key, String filePath) throws Exception {
        log.info("Downloading "+filePath+" as "+key+" from bucket "+bucket+".");
        File theFile = new File(filePath);
        File theDir = theFile.getParentFile();
        if (!theDir.exists())
            theDir.mkdirs();
        GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(request);
        
        byte[] data = objectBytes.asByteArray();
        OutputStream os = new FileOutputStream(theFile);
        os.write(data);
        os.close();

        return;
    }


    public Uploader() throws Exception {
        uuid = UUID.randomUUID();
        StorageSetup();
        log = Logger.getLogger(Uploader.class);
    }

    @Funq
    public RetValType uploader(FunInput input) throws Exception {
        var retVal = new RetValType();
        String key = "large";

        if (s3 == null) {
            retVal.result.put("message", "ERROR: Uploader unable to run since s3 null.");
            return retVal;
	}

        if (input != null) {
            if (input.size != null)
                key = input.size;
            // Buckets are deploy-time settings only; a request must not choose them.
            if (input.input_bucket != null || input.output_bucket != null)
                throw new IllegalArgumentException("input_bucket and output_bucket cannot be set in a request");
        }

        if (input_bucket == null || input_bucket.isEmpty() || output_bucket == null || output_bucket.isEmpty()) {
            retVal.result.put("message", "ERROR: Uploader unable to run. knativebench.uploader.input_bucket and knativebench.uploader.output_bucket need to be configured.");
            return (retVal);
        }

        // size comes from the request and is used in a local file name, so only allow simple names.
        if (!key.matches("[A-Za-z0-9_-]+")) {
            retVal.result.put("message", "ERROR: Uploader unable to run. size contains invalid characters.");
            return (retVal);
        }

        // Private per-request directory created by the JVM, not named by the caller.
        java.nio.file.Path workDir = java.nio.file.Files.createTempDirectory("uploader-");
        File filePath=workDir.resolve(String.format("120-%s-%s.txt",key,uuid.toString())).toFile();
        long downloadStartTime = System.nanoTime();
        downloadFile(input_bucket, key+"/yes.txt", filePath.toString());
        long downloadStopTime = System.nanoTime();
        long downloadSize = filePath.length();

        long uploadStartTime = System.nanoTime();
        uploadFile(output_bucket, filePath.toString(), filePath.toString());
        long uploadStopTime = System.nanoTime();

        deleteFile(output_bucket, filePath.toString());
	filePath.delete();
        workDir.toFile().delete();

        double downloadTime = (downloadStopTime - downloadStartTime)/1000000000.0;
        double uploadTime = (uploadStopTime - uploadStartTime)/1000000000.0;
        
        retVal.result.put("input_size",   key);
        retVal.result.put("download_size",    Long.toString(downloadSize));
        retVal.measurement.put("download_time",  (double)downloadTime);
        retVal.measurement.put("upload_time",   (double)uploadTime);

        log.info("retVal.measurement="+retVal.measurement.toString());

        return (retVal);
    }

    public static class FunInput {
	public String input_bucket;
        public String output_bucket;
	public String size;
    }

    public static class RetValType {
        public Map<String, String> result;
        public Map<String, Double> measurement;

        RetValType() {
            result      = new HashMap<String, String>();
            measurement = new HashMap<String, Double>();
        }
    }

}
