package com.ibm.trl.funqyTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class COSUtils {

    private final Region region = Region.US_WEST_2; // any region is OK
    private final String bucketName = "trl-knative-benchmark-bucket";
    private final URI endpointOverride = URI.create("https://s3.us-south.cloud-object-storage.appdomain.cloud");
    private final String access_key_id = System.getenv("AWS_ACCESS_KEY_ID");
    private final String secret_access_key = System.getenv("AWS_SECRET_ACCESS_KEY");
    private final StaticCredentialsProvider credential = StaticCredentialsProvider
            .create(AwsBasicCredentials.create(access_key_id, secret_access_key));
    private final S3Client s3 = S3Client.builder().region(region).endpointOverride(endpointOverride)
            .credentialsProvider(credential).build();

    public String getInBucket() {
        return bucketName;
    }
    
    public String getOutBucket() {
        return bucketName;
    }
    
    public void uploadFile(String output_bucket, String filename, String upload_path) throws Exception {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(filename).build();
        s3.putObject(objectRequest, RequestBody.fromFile(new File(upload_path).toPath()));
    }

    public void downloadFile(String input_bucket, String objectKey, String download_path) {
        GetObjectRequest objectRequest = GetObjectRequest.builder().key(objectKey).bucket(bucketName).build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();
        File myFile = new File(download_path);
        try {
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public InputStream get_object(String bucket, String key) {
        GetObjectRequest objectRequest = GetObjectRequest.builder().key(key).bucket(bucketName).build();
        ResponseInputStream<GetObjectResponse> in = s3.getObject(objectRequest);
        
        return in;
    }

    public String put_object(String bucket, String key, File file) {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        PutObjectResponse res = s3.putObject(objectRequest, RequestBody.fromFile(file.toPath()));

        return res.eTag();
    }
}
