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

public class J120 extends BenchmarkTest {
    private Logger log;
    private UUID uuid;
    private IBMCOS cos;

    public J120() throws Exception {
        uuid = UUID.randomUUID();
        cos = new IBMCOS();
        log = Logger.getLogger(J120.class);
    }

    public String runTest() throws Exception {
        return (runTest("large")); // let the next method handle !cos.available() error condition
    }

    public String runTest(String key) throws Exception {
        if (!cos.available())
            return ("ERROR: J120.runTest() unable to run since IBMCOS unavailable.");
        System.out.println("Starting J120: "+key);
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
        
        System.out.println("downloadTime (s): "+String.format("%.03f",downloadTime));
        System.out.println("download size: "+Long.toString(downloadSize));
        System.out.println("uploadTime (s): "+String.format("%.03f",uploadTime));

        String retval = "j120, "+String.format("%.03f",downloadTime)+", "+
                Long.toString(downloadSize)+", "+String.format("%.03f",uploadTime);

        cos.deleteFile(cos.getOutBucket(), filePath.toString());

        return (retval);
    }

}
