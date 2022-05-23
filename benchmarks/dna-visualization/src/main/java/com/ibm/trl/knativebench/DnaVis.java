package com.ibm.trl.knativebench;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;
import net.sf.jfasta.FASTAFileReader;
import net.sf.jfasta.impl.FASTAElementIterator;
import net.sf.jfasta.impl.FASTAFileReaderImpl;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class DnaVis {
    static double nanosecInSec = 1_000_000_000.0;

    @Inject
    S3Client s3;

    @ConfigProperty(name = "knativebench.dna-vis.input_bucket")
    String input_bucket;
    @ConfigProperty(name = "knativebench.dna-vis.output_bucket")
    String output_bucket;
    @ConfigProperty(name = "knativebench.dna-vis.key", defaultValue = "504/in/bacillus_subtilis.fasta")
    String key;

    @Inject
    Logger log;

    @Funq("dna-visualization")
    public RetValType dnavis(FunInput input) {
        boolean debug = Boolean.parseBoolean(input.debug);
        var retVal = new RetValType();
        File inFile = null, outFile = null;

        log.info(String.format("input_bucket=%s, output_bucket=%s, key=%s, debug=%b",
                               input.input_bucket, input.output_bucket, input.key, debug));

        try {
            inFile  = File.createTempFile("dnavis_input_", ".fasta");
            outFile = File.createTempFile("dnavis_squiggle_", ".json");
            inFile.deleteOnExit();
            outFile.deleteOnExit();
        } catch(IOException e) {
            retVal.result.put("message", e.toString());
            log.info(e.getStackTrace());
            cleanFile(inFile, log);
            cleanFile(outFile, log);
            return retVal;
        }

        if(input != null) {
            if(input.input_bucket != null)
                input_bucket = input.input_bucket;
            if(input.output_bucket != null)
                output_bucket = input.output_bucket;
            if(input.key != null)
                key = input.key;
        }

        long download_begin = System.nanoTime();
        try (FileOutputStream fos = new FileOutputStream(inFile);
             BufferedOutputStream os = new BufferedOutputStream(fos)){
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(input_bucket)
                    .key(key)
                    .build();
            s3.getObject(getReq, ResponseTransformer.toOutputStream(os));
        } catch (Exception e) {
            retVal.result.put("message", e.toString());
            log.info(e.getStackTrace());
            cleanFile(inFile, log);
            cleanFile(outFile, log);
            return retVal;
        }
        long download_end = System.nanoTime();
        retVal.measurement.put("download_time", (download_end - download_begin)/nanosecInSec);

        FASTAElementIterator itr;
        String seq;
        double[] x;
        double[] y;

        try (FASTAFileReader fasta = new FASTAFileReaderImpl(inFile)) {
            itr = fasta.getIterator();
            if(!itr.hasNext()) {
                retVal.result.put("message", "ERROR: No FASTA data");
                cleanFile(inFile, log);
                cleanFile(outFile, log);
                return retVal;
            }
            seq = itr.next().getSequence();

            int      len = seq.length();
            double   curX = 0.0;
            double   curY = 0.0;

            x = new double[len*2];
            y = new double[len*2];

            long process_begin = System.nanoTime();
            for(int i = 0; i < len; i++, curX += 1.0) {
                switch(seq.charAt(i)) {
                    case 'A':  y[i * 2] = curY + 0.5; y[i * 2 + 1] = curY; break;
                    case 'C':  y[i * 2] = curY - 0.5; y[i * 2 + 1] = curY; break;
                    case 'G':  y[i * 2] = curY + 0.5; y[i * 2 + 1] = curY + 1.0; curY += 1.0; break;
                    case 'T':  y[i * 2] = curY - 0.5; y[i * 2 + 1] = curY - 1.0; curY -= 1.0; break;
//                    default:   y[i * 2] = curY;       y[i * 2 + 1] = curY; break;
                }
                x[i * 2]     = curX;
                x[i * 2 + 1] = curX + 0.5;
            }
            long process_end = System.nanoTime();
            retVal.measurement.put("compute_time", (process_end - process_begin)/nanosecInSec);
        } catch (Exception e) {
            retVal.result.put("message", e.toString());
            log.info(e.getStackTrace());
            cleanFile(inFile, log);
            cleanFile(outFile, log);
            return retVal;
        }

        long json_begin = System.nanoTime();
        try (JsonGenerator gen = new JsonFactory().createGenerator(outFile, JsonEncoding.UTF8)) {
            gen.writeStartObject();
            gen.writeFieldName("x");
            gen.writeArray(x, 0, x.length);
            gen.writeFieldName("y");
            gen.writeArray(y, 0, y.length);
            gen.writeEndObject();
        } catch (Exception e) {
            retVal.result.put("message", e.toString());
            log.info(e.getStackTrace());
            cleanFile(inFile, log);
            cleanFile(outFile, log);
            return retVal;
        }
        long json_end = System.nanoTime();
        retVal.measurement.put("serialize_time", (json_end - json_begin)/nanosecInSec);

        long upload_begin = System.nanoTime();
        long upload_end   = upload_begin;
        if(debug && output_bucket != null && !output_bucket.isEmpty()) {
            try {
                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(output_bucket)
                        .key(key)
                        .build();
                s3.putObject(putReq, outFile.toPath());
            } catch (Exception e) {
                retVal.result.put("message", e.toString());
                log.info(e.getStackTrace());
                cleanFile(inFile, log);
                cleanFile(outFile, log);
                return retVal;
            }
            upload_end = System.nanoTime();
        }

        retVal.result.put("bucket", output_bucket);
        retVal.result.put("key",    key);
        retVal.measurement.put("upload_time", (upload_end - upload_begin)/nanosecInSec);

        cleanFile(inFile, log);
        cleanFile(outFile, log);

        log.info("retVal.measurement="+retVal.measurement.toString());

        return retVal;
    }


    private static void cleanFile(File file, Logger log) {
        try {
            if(file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            log.info(e.getStackTrace());
        }
    }

    public static class FunInput {
        public String input_bucket;
        public String output_bucket;
        public String key;
        public String debug;
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
