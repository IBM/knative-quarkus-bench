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

import io.quarkus.funqy.Funq;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.List;

import com.ibm.trl.funqy.ibmcos.IBMCOS;

public class JDownload{
    private Logger log;
    private UUID uuid;
    private IBMCOS cos;

    public JDownload() throws Exception {
        uuid = UUID.randomUUID();
        cos = new IBMCOS();
        log = Logger.getLogger(JDownload.class);
    }

    @Funq
    public RetValType download(FunInput input) throws Exception {
        var retVal = new RetValType();
        String key = "large";

        if (!cos.available()) {
            retVal.result.put("message", "ERROR: JDownload.runTest() unable to run since IBMCOS unavailable.");
	}

        if (input != null) {
            if (input.size != null)
                key = input.size;
        }

        File downloadPath=new File(String.format("/tmp/%s-%s",key,uuid));
        downloadPath.mkdirs();
        long downloadStartTime = System.nanoTime();
        cos.downloadDirectory(cos.getInBucket(), key, downloadPath.toString());
        long downloadStopTime = System.nanoTime();
        long downloadSize = parseDirectory(new File(downloadPath.getPath()+"/"+key));

        double downloadTime = (downloadStopTime - downloadStartTime)/1000000000.0;
        
//        System.out.println("downloadTime (s): "+String.format("%.03f",downloadTime));
//        System.out.println("download size: "+Long.toString(downloadSize));

//        String retval = "jdownload, "+String.format("%.03f",downloadTime)+", "+
//                Long.toString(downloadSize);

//        retVal.result.put("bucket", bucket_name);
        retVal.result.put("input_size",    key);
        retVal.result.put("download_size",    Long.toString(downloadSize));
        retVal.measurement.put("download_time",  downloadTime);


        return (retVal);
    }

    private long parseDirectory(File dir) {
       // walk through directory totaling size of all files in it 
       long size=0;
       for (File f : dir.listFiles())
           size += f.length();
        return size;
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
