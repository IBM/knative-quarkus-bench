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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ibm.trl.funqy.ibmcos.IBMCOS;

public class JCompress extends BenchmarkTest {
    private Logger log;
    private UUID uuid;
    private IBMCOS cos;

    public JCompress() throws Exception {
        uuid = UUID.randomUUID();
        cos = new IBMCOS();
        log = Logger.getLogger(JCompress.class);
    }

    public String runTest() throws Exception {
        return (runTest("large")); // let the next method handle !cos.available() error condition
    }

    public String runTest(String key) throws Exception {
        if (!cos.available())
            return ("ERROR: JCompress.runTest() unable to run since IBMCOS unavailable.");
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

        return (retval);
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

}
