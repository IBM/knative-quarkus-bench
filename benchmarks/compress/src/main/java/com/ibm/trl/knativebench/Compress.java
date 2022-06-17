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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

import io.quarkus.funqy.Funq;

public class Compress {
    private Logger log;

    private String AWS_REGION = "ap-south-1";
    private String AWS_ENDPOINT = "defaultvalue";
    private String input_bucket = "";
    private String output_bucket = "";

    private Region region = Region.AP_SOUTH_1; // any region is OK
    private URI endpointOverride = null;
    private String access_key_id = null;
    private String secret_access_key = null;
    private StaticCredentialsProvider credential = null;
    private S3Client s3 = null;

    private File downloadPath = null;

    void deleteLocalDir(File file) {
        log.info("Deleting from local file system: "+file.toPath());
        File[] flist = file.listFiles();
        if (flist != null)
            for (File f : flist)
                if (!Files.isSymbolicLink(f.toPath()))
                    deleteLocalDir(f);
        file.delete();
    }

    public void finalize() {
	if (downloadPath != null) {
                log.info("Compress: Running finalizer.");
		try {
			deleteLocalDir(downloadPath);
		}
			catch (Exception e) {
			log.info("Exception deleting from local filesystem: "+e.toString());
		}
	}
    }

    public void StorageSetup() throws Exception {
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

    } // initialization


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


    private void downloadDirectory(String bucket, String key, String dirPath) throws Exception {
        boolean moreResults = true;
        String nextToken = "";
        int maxKeys = 128;

        log.info("Downloading "+dirPath+" with "+key+" from bucket "+bucket+".");
        
        while (moreResults) {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).maxKeys(maxKeys)
                    .continuationToken(nextToken).build();

            ListObjectsV2Response result = s3.listObjectsV2(request);
            for (S3Object object : result.contents()) {
                if (object.key().startsWith(key)) {
                    downloadFile(bucket, object.key(), dirPath.concat("/").concat(object.key()));
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


    public Compress() throws Exception {
        log = Logger.getLogger(Compress.class);
        log.info("Initializing Compress");
        StorageSetup();
    }

    @Funq
    public RetValType compress(FunInput input) throws Exception {
        UUID uuid = UUID.randomUUID();
        var retVal = new RetValType();
        String key = "large";

        if (s3 == null) {
            retVal.result.put("message", "ERROR: Compress unable to run due to storage initialization.");
            return retVal;
	}

        if (input != null) {
            if (input.size != null)
                key = input.size;
            if (input.input_bucket != null)
                input_bucket = input.input_bucket;
            if (input.output_bucket != null)
                output_bucket = input.output_bucket;
        }

	if (input_bucket == null || output_bucket == null) {
            retVal.result.put("message", "ERROR: Compress unable to run. input_bucket and output_bucket need to be set.");
            return (retVal);
	}

        downloadPath=new File(String.format("/tmp/%s-%s",key,uuid));
        downloadPath.mkdirs();
        long downloadStartTime = System.nanoTime();
        downloadDirectory(input_bucket, key, downloadPath.toString());
        long downloadStopTime = System.nanoTime();
        long downloadSize = parseDirectory(new File(downloadPath.getPath()+"/"+key));

        long compressStartTime = System.nanoTime();
        File destinationFile = new File(String.format("%s/%s-%s.zip",downloadPath.toString(),key,uuid));
        zipDir(destinationFile, new File(downloadPath.getPath()+"/"+key));
        long compressStopTime = System.nanoTime();

        long uploadStartTime = System.nanoTime();
        String archiveName = String.format("%s-%s.zip",key,uuid);
        uploadFile(output_bucket, archiveName, destinationFile.toString());
        long uploadStopTime = System.nanoTime();
        long compressSize = destinationFile.length();

        double downloadTime = (downloadStopTime - downloadStartTime)/1000000000.0;
        double compressTime = (compressStopTime - compressStartTime)/1000000000.0;
        double uploadTime = (uploadStopTime - uploadStartTime)/1000000000.0;
        
	try {
		deleteFile(output_bucket, archiveName);
	}
	catch (Exception e) {
		log.info("Exception deleting from cloud storage: "+e.toString());
	}

	try {
		deleteLocalDir(downloadPath);
	}
	catch (Exception e) {
		log.info("Exception deleting from local filesystem: "+e.toString());
	}

        retVal.result.put("download_size",    Long.toString(downloadSize));
        retVal.result.put("compress_size",    Long.toString(compressSize));
        retVal.result.put("input_size",    key);
        retVal.measurement.put("download_time",  downloadTime);
        retVal.measurement.put("compress_time",   compressTime);
        retVal.measurement.put("upload_time",   uploadTime);
        
        log.info("retVal.measurement="+retVal.measurement.toString());

        return (retVal);
    }

    private long parseDirectory(File dir) {
       // walk through directory totaling size of all files in it 
       long size=0;
       for (File f : dir.listFiles())
           size += f.length();
        return size;
    }

    private void zipDir(File dstFile, File srcDir) throws IOException {
        int length;
        byte[] bytes = new byte[16384];
        log.info("in ZipDir(): source directory: "+srcDir+" destination file: "+dstFile);
        FileOutputStream fos = new FileOutputStream(dstFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        
         for (File f:srcDir.listFiles())
            if (!f.getName().equals(dstFile.getName()))
                {
                    FileInputStream fis = new FileInputStream(f);
                    ZipEntry zipEntry = new ZipEntry(f.getName());
                    zipOut.putNextEntry(zipEntry);
                    
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    fis.close();
                }
        zipOut.close();
        fos.close();
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
