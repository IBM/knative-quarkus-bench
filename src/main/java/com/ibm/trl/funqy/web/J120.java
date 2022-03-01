package com.ibm.trl.funqy.web;

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

import com.ibm.trl.funqy.cosutils.COSUtils;

public class J120 {
    private Logger log;
    private UUID uuid;
    private COSUtils cos;

    public J120() throws Exception {
        uuid = UUID.randomUUID();
        cos = new COSUtils();
        log = Logger.getLogger(J120.class);
    }

    @Funq
    public RetValType upload(FunInput input) throws Exception {
        var retVal = new RetValType();
        String key = "large";

        if (!cos.available()) {
            retVal.result.put("message", "ERROR: J120.runTest() unable to run since COSUtils unavailable.");
            return retVal;

	}

        if (input != null) {
            if (input.size != null)
                key = input.size;
        }

        File filePath=new File(String.format("/tmp/120-%s.txt",key,uuid));
        long downloadStartTime = System.nanoTime();
        cos.downloadFile(cos.getInBucket(), key+"/yes.txt", filePath.toString());
        long downloadStopTime = System.nanoTime();
        long downloadSize = filePath.length();

        long uploadStartTime = System.nanoTime();
        cos.uploadFile(cos.getOutBucket(), filePath.toString(), filePath.toString());
        long uploadStopTime = System.nanoTime();

        double downloadTime = (downloadStopTime - downloadStartTime)/1000000000.0;
        double uploadTime = (uploadStopTime - uploadStartTime)/1000000000.0;
        
//        System.out.println("downloadTime (s): "+String.format("%.03f",downloadTime));
//        System.out.println("download size: "+Long.toString(downloadSize));
//        System.out.println("uploadTime (s): "+String.format("%.03f",uploadTime));

//        String retval = "j120, "+String.format("%.03f",downloadTime)+", "+
//                Long.toString(downloadSize)+", "+String.format("%.03f",uploadTime);

//        retVal.result.put("bucket", bucket_name);
        retVal.result.put("input_size",   key);
        retVal.result.put("download_size",    Long.toString(downloadSize));
        retVal.measurement.put("download_time",  (double)downloadTime);
        retVal.measurement.put("upload_time",   (double)uploadTime);

        cos.deleteFile(cos.getOutBucket(), filePath.toString());

        return (retVal);
    }

    public static class FunInput {
//        public String bucket_name;
//        public String input_key;
//        public String output_key;
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
