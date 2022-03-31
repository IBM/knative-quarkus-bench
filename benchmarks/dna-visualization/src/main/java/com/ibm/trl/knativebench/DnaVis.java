package com.ibm.trl.knativebench;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.quarkus.funqy.Funq;

import net.sf.jfasta.impl.FASTAElementIterator;
import net.sf.jfasta.impl.FASTAFileReaderImpl;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class DnaVis {
    static double nanosecInSec = 1_000_000_000.0;

    @Inject
    S3Client s3;

    @ConfigProperty(name = "serverlessbench.dna-vis.bucket_name")
    String bucket_name;
    @ConfigProperty(name = "serverlessbench.dna-vis.input_key", defaultValue = "504/in/bacillus_subtilis.fasta")
    String input_key;
    @ConfigProperty(name = "serverlessbench.dna-vis.output_key", defaultValue = "504/out/bacillus_subtilis_squiggle.json")
    String output_key;

    @Funq
    public RetValType dnavis(FunInput input) {
        var retVal = new RetValType();
        
        File inFile  = new File("/tmp/bacillus_subtilis.fasta");
        File outFile = new File("/tmp/bacillus_subtilis_squiggle.json");

        if(input != null) {
            if(input.bucket_name != null)
                bucket_name = input.bucket_name;
            if(input.input_key != null)
                input_key = input.input_key;
            if(input.output_key != null)
                output_key = input.output_key;
        }

        try {
            if(inFile.exists())   Files.delete(inFile.toPath());
            if(outFile.exists())  Files.delete(outFile.toPath());
        } catch (IOException e) {
            retVal.result.put("message", e.toString());
            e.printStackTrace();
            return retVal;
        }

        long download_begin = System.nanoTime();
        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket_name)
                    .key(input_key)
                    .build();
            s3.getObject(getReq, inFile.toPath());
        } catch (Exception e) {
            retVal.result.put("message", e.toString());
            e.printStackTrace();
            return retVal;
        }
        long download_end = System.nanoTime();

        FASTAElementIterator itr;
        String seq;
        try {
            itr = new FASTAFileReaderImpl(inFile).getIterator();
            if(!itr.hasNext()) {
                retVal.result.put("message", "ERROR: No FASTA data");
                return retVal;
            }
            seq = itr.next().getSequence();
        } catch (IOException e) {
            retVal.result.put("message", e.toString());
            e.printStackTrace();
            return retVal;
        }

        int      len = seq.length();
        double[] x = new double[len*2];
        double[] y = new double[len*2];
        double   curX = 0.0;
        double   curY = 0.0;

        long process_begin = System.nanoTime();
        for(int i = 0; i < len; i++, curX += 1.0) {
            switch(seq.charAt(i)) {
            case 'A':  y[i * 2] = curY + 0.5; y[i * 2 + 1] = curY; break;
            case 'C':  y[i * 2] = curY - 0.5; y[i * 2 + 1] = curY; break;
            case 'G':  y[i * 2] = curY + 0.5; y[i * 2 + 1] = curY + 1.0; curY += 1.0; break;
            case 'T':  y[i * 2] = curY - 0.5; y[i * 2 + 1] = curY - 1.0; curY -= 1.0; break;
//            default:   y[i * 2] = curY;       y[i * 2 + 1] = curY; break;
            }
            x[i * 2]     = curX;
            x[i * 2 + 1] = curX + 0.5;
        }
        long process_end = System.nanoTime();

        long json_begin = System.nanoTime();
        JsonGenerator gen;
        try {
            gen = new JsonFactory().createGenerator(outFile, JsonEncoding.UTF8);
            gen.writeStartObject();
            gen.writeFieldName("x");
            gen.writeArray(x, 0, x.length);
            gen.writeFieldName("y");
            gen.writeArray(y, 0, y.length);
            gen.writeEndObject();
            gen.close();
        } catch (IOException e) {
            retVal.result.put("message", e.toString());
            e.printStackTrace();
            return retVal;
        }
        long json_end = System.nanoTime();

        long upload_begin = System.nanoTime();
        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket_name)
                    .key(output_key)
                    .build();
            s3.putObject(putReq, outFile.toPath());
        } catch (Exception e) {
            retVal.result.put("message", e.toString());
            e.printStackTrace();
            return retVal;
        }
        long upload_end = System.nanoTime();

        retVal.result.put("bucket", bucket_name);
        retVal.result.put("key",    output_key);
        retVal.measurement.put("download_time",  (download_end - download_begin)/nanosecInSec); 
        retVal.measurement.put("compute_time",   (process_end - process_begin)/nanosecInSec); 
        retVal.measurement.put("serialize_time", (json_end - json_begin)/nanosecInSec); 
        retVal.measurement.put("upload_time",    (upload_end - upload_begin)/nanosecInSec); 
        
        return retVal;
    }


    public static class FunInput {
        public String bucket_name;
        public String input_key;
        public String output_key;
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
