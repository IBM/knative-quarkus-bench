package com.ibm.trl.serverlessbench;

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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.List;

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



public class COSUtils {
    private static final Logger log = Logger.getLogger(COSUtils.class);
    private String AWS_REGION = "ap-south-1";
    private String AWS_ENDPOINT = "needstobeset";
    private String COS_IN_BUCKET = "trl-knative-benchmark-bucket-1";
    private String COS_OUT_BUCKET = "trl-knative-benchmark-bucket-2";
    private String COS_MODEL_BUCKET = "trl-knative-benchmark-bucket-1";
    private String COS_BUCKET = "trl-knative-benchmark-bucket";

    private Region region = Region.AP_SOUTH_1; // any region is OK
    private URI endpointOverride = null;
    private String access_key_id = null;
    private String secret_access_key = null;
    private StaticCredentialsProvider credential = null;
    private S3Client s3 = null;
    private boolean debug = false;


    public COSUtils() throws Exception {
        String value;

        if ((value = System.getenv("AWS_ENDPOINT")) != null) {
	    AWS_ENDPOINT = value;
            endpointOverride = URI.create(AWS_ENDPOINT);
	}

        if ((value = System.getenv("AWS_ACCESS_KEY_ID")) != null)
            access_key_id = System.getenv("AWS_ACCESS_KEY_ID");

        if ((value = System.getenv("AWS_SECRET_ACCESS_KEY")) != null)
            secret_access_key = System.getenv("AWS_SECRET_ACCESS_KEY");

        if ((value = System.getenv("COS_IN_BUCKET")) != null)
            COS_IN_BUCKET = value;

        if ((value = System.getenv("COS_OUT_BUCKET")) != null)
            COS_OUT_BUCKET = value;

        if ((value = System.getenv("COS_MODEL_BUCKET")) != null)
            COS_MODEL_BUCKET = value;

        if ((value = System.getenv("COS_BUCKET")) != null)
            COS_BUCKET = value;

        if ((value = System.getenv("AWS_REGION")) != null) {
            AWS_REGION = value;
	    region = Region.of(AWS_REGION); // right method?!?!?!
	    } 

	if (debug == true) {
	    System.out.println("AWS_ENDPOINT="+AWS_ENDPOINT);
	    System.out.println("effective endpoint="+endpointOverride.toString());
	    System.out.println("AWS_ACCESS_KEY_ID="+access_key_id);
	    System.out.println("AWS_SECRET_ACCESS_KEY="+secret_access_key);
	    System.out.println("COS_IN_BUCKET="+COS_IN_BUCKET);
	    System.out.println("COS_OUT_BUCKET="+COS_OUT_BUCKET);
	    System.out.println("COS_MODEL_BUCKET="+COS_MODEL_BUCKET);
	    System.out.println("COS_BUCKET="+COS_BUCKET);
	    System.out.println("AWS_REGION="+AWS_REGION);
	    System.out.println("effective region="+region.toString());
	}

        credential = StaticCredentialsProvider
            .create(AwsBasicCredentials.create(access_key_id, secret_access_key));

        s3 = S3Client.builder().region(region).endpointOverride(endpointOverride)
            .credentialsProvider(credential).build();

    } // COSUtils initialization


    public boolean available() {
        if (s3 == null)
            return false;
        else
            return true;
    }

    public void deleteFile(String bucket, String key) {
        log.info("Deleting "+key+" from bucket "+bucket+".");
//        s3.deleteObject(bucket, key);
        DeleteObjectRequest deleteObjectRequest =
            DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3.deleteObject(deleteObjectRequest);
        return;
    }

    public String getInBucket() {
        return (COS_IN_BUCKET);
    }

    public String getOutBucket() {
        return (COS_OUT_BUCKET);
    }

    public String getModelBucket() {
        return (COS_MODEL_BUCKET);
    }


    public void uploadFile(String bucket, String key, String filePath) {
        log.info("Uploading "+filePath+" as "+key+" to bucket "+bucket+".");
//        s3.putObject(bucket, key, new File(filePath));
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
        s3.putObject(objectRequest, RequestBody.fromFile(new File(filePath).toPath()));

        return;
    }

    public void uploadFile(S3Client s3, String bucket, String key, String filePath) {
        log.info("Uploading "+filePath+" as "+key+" to bucket "+bucket+".");
//        s3.putObject(bucket, key, new File(filePath));
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
        s3.putObject(objectRequest, RequestBody.fromFile(new File(filePath).toPath()));

        return;
    }

    public void downloadFile(S3Client s3, String bucket, String key, String filePath) throws Exception {
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

//      s3.getObject(request, theFile);
//	log.info("Actual downloaded file size of "+filePath+" = "+theFile.length());
        return;
    }

    public InputStream get_object(String bucket, String key) {
        GetObjectRequest objectRequest = GetObjectRequest.builder().key(key).bucket(bucket).build();
        ResponseInputStream<GetObjectResponse> in = s3.getObject(objectRequest);
	return in;

//        GetObjectRequest request = new GetObjectRequest(bucket, key);
//        S3Object s3o = cosClient.getObject(request);
//        return (InputStream)s3o.getObjectContent();
    }

    public InputStream get_object(S3Client s3, String bucket, String key) {
        GetObjectRequest objectRequest = GetObjectRequest.builder().key(key).bucket(bucket).build();
        ResponseInputStream<GetObjectResponse> in = s3.getObject(objectRequest);
	return in;

//        GetObjectRequest request = new GetObjectRequest(bucket, key);
//        S3Object s3o = cosClient.getObject(request);
//        return (InputStream)s3o.getObjectContent();
    }

    public String put_object(String bucket, String key, File file) {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
        PutObjectResponse res = s3.putObject(objectRequest, RequestBody.fromFile(file.toPath()));
	return res.eTag();

//        PutObjectResult res = cosClient.putObject(bucket, key,  file);
//        return res.getETag();

    }

    public String put_object(S3Client s3, String bucket, String key, File file) {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
        PutObjectResponse res = s3.putObject(objectRequest, RequestBody.fromFile(file.toPath()));
	return res.eTag();

//        PutObjectResult res = cosClient.putObject(bucket, key,  file);
//        return res.getETag();

    }
}
