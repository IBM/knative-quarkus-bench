package com.ibm.trl.funqy.cloudevent;

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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.List;

import com.ibm.trl.funqy.ibmcos.IBMCOS;

public class JDownload extends BenchmarkTest {
    private Logger log;
    private UUID uuid;
    private IBMCOS cos;

    public JDownload() throws Exception {
        uuid = UUID.randomUUID();
        cos = new IBMCOS();
        log = Logger.getLogger(JDownload.class);
    }

    public String runTest() throws Exception {
        return (runTest("large")); // let the next method handle !cos.available() error condition
    }

    public String runTest(String key) throws Exception {
        if (!cos.available())
            return ("ERROR: JDownload.runTest() unable to run since IBMCOS unavailable.");
        System.out.println("Starting JDownload: "+key);
        File downloadPath=new File(String.format("/tmp/%s-%s",key,uuid));
        downloadPath.mkdirs();
        long downloadStartTime = System.nanoTime();
        cos.downloadDirectory(cos.getInBucket(), key, downloadPath.toString());
        long downloadStopTime = System.nanoTime();
        long downloadSize = parseDirectory(new File(downloadPath.getPath()+"/"+key));

        double downloadTime = (downloadStopTime - downloadStartTime)/1000000000.0;
        
        System.out.println("downloadTime (s): "+String.format("%.03f",downloadTime));
        System.out.println("download size: "+Long.toString(downloadSize));

        String retval = "jdownload, "+String.format("%.03f",downloadTime)+", "+
                Long.toString(downloadSize);

        return (retval);
    }

    private long parseDirectory(File dir) {
       // walk through directory totaling size of all files in it 
       long size=0;
       for (File f : dir.listFiles())
           size += f.length();
        return size;
    }

}
