package com.ibm.trl.funqy.cloudevent;

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

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.SdkClientException;
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cloud.objectstorage.services.s3.model.Bucket;
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsV2Request;
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsV2Result;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectListing;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;

public class IBMCOS {
    private static final Logger log = Logger.getLogger(IBMCOS.class);
    private String COS_ENDPOINT = "https://s3.direct.us-south.cloud-object-storage.appdomain.cloud";
    private String COS_APIKEY = "notset";
    private String COS_AUTH_ENDPOINT = "https://iam.cloud.ibm.com/identity/token";
    private String COS_INSTANCE_CRN = "notset";
    private String COS_BUCKET_LOCATION = "us-south";
    private String COS_IN_BUCKET = "trl-knative-benchmark-bucket-1";
    private String COS_OUT_BUCKET = "trl-knative-benchmark-bucket-2";
    private AmazonS3 cosClient = null;

    public IBMCOS() throws Exception {
        String value;

        if ((value = System.getenv("COS_ENDPOINT")) != null)
            COS_ENDPOINT = value;

        if ((value = System.getenv("COS_APIKEY")) != null)
            COS_APIKEY = value;

        if ((value = System.getenv("COS_INSTANCE_CRN")) != null)
            COS_INSTANCE_CRN = value;

        if ((value = System.getenv("COS_IN_BUCKET")) != null)
            COS_IN_BUCKET = value;

        if ((value = System.getenv("COS_OUT_BUCKET")) != null)
            COS_OUT_BUCKET = value;

        if ((value = System.getenv("COS_BUCKET_LOCATION")) != null)
            COS_BUCKET_LOCATION = value;

        SDKGlobalConfiguration.IAM_ENDPOINT = COS_AUTH_ENDPOINT;

//        try {
            cosClient = createClient(COS_APIKEY, COS_INSTANCE_CRN, COS_ENDPOINT, COS_BUCKET_LOCATION);
//        } catch (SdkClientException sdke) {
//            log.info("cosClient Init Failure: SDK Error: " + sdke.getMessage());
//            sdke.printStackTrace(System.out);
//        } catch (Exception e) {
//            log.info("cosClient Init Failure: " + e.getMessage());
//            e.printStackTrace(System.out);
//        }
    } // IBMCOS initialization

    private static AmazonS3 createClient(String api_key, String service_instance_id, String endpoint_url,
            String location) {
        AWSCredentials credentials = new BasicIBMOAuthCredentials(api_key, service_instance_id);
        ClientConfiguration clientConfig = new ClientConfiguration();
//        ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000); // .withSocketBufferSizeHints(4194304,4194304);
        clientConfig.setUseTcpKeepAlive(true);

        AmazonS3 cos = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new EndpointConfiguration(endpoint_url, location))
                .withPathStyleAccessEnabled(true).withClientConfiguration(clientConfig).build();

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
        cosClient.deleteObject(bucket, key);
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
        cosClient.putObject(bucket, key, new File(filePath));
        return;
    }

    public void downloadFile(String bucket, String key, String filePath) {
        log.info("Downloading "+filePath+" as "+key+" from bucket "+bucket+".");
        File theFile = new File(filePath);
        File theDir = theFile.getParentFile();
        if (!theDir.exists())
            theDir.mkdirs();
        GetObjectRequest request = new GetObjectRequest(bucket, key);
        cosClient.getObject(request, theFile);
        return;
    }

    public void downloadDirectory(String bucket, String key, String dirPath) {
        boolean moreResults = true;
        String nextToken = "";
        int maxKeys = 128;

        log.info("Downloading "+dirPath+" with "+key+" from bucket "+bucket+".");
        
        while (moreResults) {
            ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucket).withMaxKeys(maxKeys)
                    .withContinuationToken(nextToken);

            ListObjectsV2Result result = cosClient.listObjectsV2(request);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                if (objectSummary.getKey().startsWith(key)) {
                    downloadFile(bucket, objectSummary.getKey(), dirPath.concat("/").concat(objectSummary.getKey()));
                }
            }

            if (result.isTruncated()) {
                nextToken = result.getNextContinuationToken();
            } else {
                nextToken = "";
                moreResults = false;
            }
        }
    }

}
