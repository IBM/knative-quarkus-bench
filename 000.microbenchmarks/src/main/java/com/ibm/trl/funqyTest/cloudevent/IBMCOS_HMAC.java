package com.ibm.trl.funqyTest.cloudevent;

import org.jboss.logging.Logger;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;

import java.time.LocalDateTime;
import java.util.List;

import java.net.URI;
import java.nio.file.Paths;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

public class IBMCOS_HMAC {
    private static final Logger log = Logger.getLogger(IBMCOS_HMAC.class);
    private String COS_ENDPOINT = "https://s3.us-south.cloud-object-storage.appdomain.cloud";
    // private String COS_AUTH_ENDPOINT = "https://iam.cloud.ibm.com/identity/token";
    private String COS_BUCKET_LOCATION = "us-south";
    private String COS_IN_BUCKET = "trl-knative-benchmark-bucket";
    private String COS_OUT_BUCKET = "trl-knative-benchmark-bucket";
    private String COS_HMAC_ACCESS_KEY_ID = "notset";
    private String COS_HMAC_SECRET_ACCESS_KEY = "notset";

    private S3Client cosClient = null;

    public IBMCOS_HMAC() throws Exception {
        String value;

        if ((value = System.getenv("COS_ENDPOINT")) != null)
            COS_ENDPOINT = value;

        if ((value = System.getenv("COS_HMAC_ACCESS_KEY_ID")) != null)
            COS_HMAC_ACCESS_KEY_ID = value;

        if ((value = System.getenv("COS_HMAC_SECRET_ACCESS_KEY")) != null)
            COS_HMAC_SECRET_ACCESS_KEY = value;

        if ((value = System.getenv("COS_IN_BUCKET")) != null)
            COS_IN_BUCKET = value;

        if ((value = System.getenv("COS_OUT_BUCKET")) != null)
            COS_OUT_BUCKET = value;

        if ((value = System.getenv("COS_BUCKET_LOCATION")) != null)
            COS_BUCKET_LOCATION = value;

        // try {
            cosClient = createClient(
                COS_HMAC_ACCESS_KEY_ID,
                COS_HMAC_SECRET_ACCESS_KEY,
                COS_ENDPOINT,
                COS_BUCKET_LOCATION
            );
        // } catch (SdkClientException sdke) {
        //        log.info("cosClient Init Failure: SDK Error: " + sdke.getMessage());
        //        sdke.printStackTrace(System.out);
        // } catch (Exception e) {
        //        log.info("cosClient Init Failure: " + e.getMessage());
        //        e.printStackTrace(System.out);
        // }
    } // IBMCOS_HMAC initialization

    private static S3Client createClient(
        String access_key_id,
        String secret_access_key,
        String endpoint_url,
        String location
    ) {
        StaticCredentialsProvider credential = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                access_key_id,
                secret_access_key));

        // ClientConfiguration clientConfig = new ClientConfiguration();
        // ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000); // .withSocketBufferSizeHints(4194304,4194304);
        Region region = Region.US_WEST_2; // any regions are OK
        // clientConfig.setUseTcpKeepAlive(true);

        S3Client cos = S3Client.builder()
            .region(region)
            .endpointOverride(URI.create(endpoint_url))
            .credentialsProvider(credential)
            .build();

        // log.info(
        //     "Creating S3Client with region:"+region+
        //     ", endpoint: "+endpoint_url+
        //     ", access_key_id: "+access_key_id+
        //     ", secret_access_key: "+secret_access_key+"."
        // );
        return cos;
    }

    public boolean available() {
    if (cosClient == null)
        return false;
    else
        return true;
    }

    public void deleteFile(String bucket, String key) {
        log.info("Deleting "+key+" from bucket "+bucket+".");
        DeleteObjectRequest deleteObjectRequest =
            DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        cosClient.deleteObject(deleteObjectRequest);
        return;
    }

    public String getInBucket() {
        return (COS_IN_BUCKET);
    }

    public String getOutBucket() {
        return (COS_OUT_BUCKET);
    }

    public void uploadFile(String bucket, String key, String filePath) {
        log.info("Uploading "+filePath+" as "+key+" to bucket "+bucket+".");
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        RequestBody path = RequestBody.fromFile(Paths.get(filePath));
        cosClient.putObject(objectRequest, path);
        log.info("The upload of "+filePath+" as "+key+" to bucket "+bucket+" completed.");
        return;
    }

    public void downloadFile(String bucket, String key, String filePath) {
        log.info("Downloading "+filePath+" as "+key+" from bucket "+bucket+".");
        File theFile = new File(filePath);
        File theDir = theFile.getParentFile();
        if (!theDir.exists())
            theDir.mkdirs();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();
        cosClient.getObject(getObjectRequest);
        return;
    }

    public void downloadDirectory(String bucket, String key, String dirPath) {
        boolean moreResults = true;
        String nextToken = "";
        int maxKeys = 128;

        log.info("Downloading "+dirPath+" with "+key+" from bucket "+bucket+".");
        
        while (moreResults) {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .maxKeys(maxKeys)
                .continuationToken(nextToken)
                .build();

            ListObjectsV2Response result = cosClient.listObjectsV2(request);
            for (S3Object object : result.contents()) {
                if (object.key().startsWith(key)) {
                    downloadFile(
                        bucket,
                        object.key(),
                        dirPath.concat("/").concat(object.key())
                    );
                }
            }

            if (result.isTruncated()) {
                nextToken = result.nextContinuationToken();
            } else {
                nextToken = "";
                moreResults = false;
            }
        }
    }
}
