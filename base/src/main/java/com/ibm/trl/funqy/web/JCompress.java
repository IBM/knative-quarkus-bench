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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.quarkus.funqy.Funq;

import com.ibm.trl.funqy.cosutils.COSUtils;

public class JCompress {
    private Logger log;
    private UUID uuid;
    private COSUtils cos;

    public JCompress() throws Exception {
        uuid = UUID.randomUUID();
        cos = new COSUtils();
        log = Logger.getLogger(JCompress.class);
    }

    @Funq
    public RetValType compress(FunInput input) throws Exception {
        var retVal = new RetValType();
        String key = "large";

        if (!cos.available()) {
            retVal.result.put("message", "ERROR: JCompress.runTest() unable to run since COSUtils unavailable.");
            return retVal;
	}

        if (input != null) {
            if (input.size != null)
                key = input.size;
        }

        System.out.println("Starting JCompress: "+key);
        File downloadPath=new File(String.format("/tmp/%s-%s",key,uuid));
        downloadPath.mkdirs();
        long downloadStartTime = System.nanoTime();
        cos.downloadDirectory(cos.getInBucket(), key, downloadPath.toString());
        long downloadStopTime = System.nanoTime();
        long downloadSize = parseDirectory(new File(downloadPath.getPath()+"/"+key));

        long compressStartTime = System.nanoTime();
        File destinationFile = new File(String.format("%s/%s-%s.zip",downloadPath.toString(),key,uuid));
//        try {
            zipDir(destinationFile, new File(downloadPath.getPath()+"/"+key));
//        } catch (Exception e) {
//            System.out.println("Error creating zip file: "+e);
//        }
        long compressStopTime = System.nanoTime();

        long uploadStartTime = System.nanoTime();
        String archiveName = String.format("%s-%s.zip",key,uuid);
        cos.uploadFile(cos.getOutBucket(), archiveName, destinationFile.toString());
        long uploadStopTime = System.nanoTime();
        long compressSize = destinationFile.length();

        double downloadTime = (downloadStopTime - downloadStartTime)/1000000000.0;
        double compressTime = (compressStopTime - compressStartTime)/1000000000.0;
        double uploadTime = (uploadStopTime - uploadStartTime)/1000000000.0;
        
        System.out.println("downloadTime (s): "+String.format("%.03f",downloadTime));
        System.out.println("download size: "+Long.toString(downloadSize));
        System.out.println("compressTime (s): "+String.format("%.03f",compressTime));
        System.out.println("compress size: "+Long.toString(compressSize));
        System.out.println("uploadTime (s): "+String.format("%.03f",uploadTime));

        String retval = "jcompress, "+String.format("%.03f",downloadTime)+", "+
                Long.toString(downloadSize)+", "+String.format("%.03f",compressTime)+", "+
                Long.toString(compressSize)+", "+String.format("%.03f",uploadTime);

        cos.deleteFile(cos.getOutBucket(), archiveName);

//        retVal.result.put("bucket", bucket_name);
        retVal.result.put("download_size",    Long.toString(downloadSize));
        retVal.result.put("compress_size",    Long.toString(compressSize));
        retVal.result.put("input_size",    key);
        retVal.measurement.put("download_time",  downloadTime);
        retVal.measurement.put("compress_time",   compressTime);
        retVal.measurement.put("upload_time",   uploadTime);



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
