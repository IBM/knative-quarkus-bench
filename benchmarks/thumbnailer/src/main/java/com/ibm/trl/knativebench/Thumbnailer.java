package com.ibm.trl.knativebench;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;


public class Thumbnailer {
    @Inject
    S3Client s3;
    @Inject
    Logger log;

    @ConfigProperty(name = "knativebench.thumbnailer.input_bucket")
    String input_bucket;

    @ConfigProperty(name = "knativebench.thumbnailer.output_bucket")
    String output_bucket;

    public BufferedImage resize_image(BufferedImage bimg, int w, int h) throws IOException {
        Image thumbnail = bimg.getScaledInstance(w, h, Image.SCALE_DEFAULT);
        BufferedImage ret_image = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
        return ret_image;
    }
    
    @Funq
    public RetValType thumbnailer(FunInput input) throws Exception {
        int object_height = input.getHeight();
        int object_width = input.getWidth();
        String key = input.getObjectKey().replaceAll(" ", "+");
        int width = object_width;
        int height = object_height;
        boolean debug = Boolean.parseBoolean(input.debug);
        if (input.getInput_bucket() != null) {
            input_bucket = input.getInput_bucket();
        }
        if (input.getOutput_bucket() != null) {
            output_bucket = input.getOutput_bucket();
        }

        long download_begin = System.nanoTime();
        InputStream img = download_stream(input_bucket, key);
        long download_end = System.nanoTime();

        BufferedImage bimg = ImageIO.read(img);
        long image_size = len(bimg);
        long process_begin = System.nanoTime();
        BufferedImage resized = resize_image(bimg, width, height);
        long resized_size = len(resized);
        long process_end = System.nanoTime();

        long upload_begin = System.nanoTime();
        long upload_end   = upload_begin;
        File f = new File(key);
        String[] out_key = new String[] {f.getParent(), "resized-" + f.getName()};
        String key_name = "";
        if(debug) {
            key_name = upload_stream(output_bucket, out_key, resized);
            upload_end = System.nanoTime();
        }

        double download_time = (download_end - download_begin)/1000_000_000.0;
        double upload_time   = (upload_end - upload_begin)/1000_000_000.0;
        double process_time   = (process_end - process_begin)/1000_000_000.0;

        RetValType retVal = new RetValType();
        retVal.result = Map.of(     "bucket", output_bucket,
                                    "key", key_name);
        retVal.measurement = Map.of("download_time", Double.valueOf(download_time),
                                    "download_size", Double.valueOf(image_size),
                                    "upload_time",   Double.valueOf(upload_time),
                                    "upload_size",   Double.valueOf(resized_size),
                                    "compute_time",  Double.valueOf(process_time));
        log.info("retVal.measurement="+retVal.measurement.toString());

        return retVal;
    }

    private long len(BufferedImage img) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        out.flush();
        byte[] image_bytes = out.toByteArray();
        out.close();

        return image_bytes.length;
    }

    private InputStream download_stream(String bucket, String file) throws FileNotFoundException {
        GetObjectRequest objectRequest = GetObjectRequest.builder().key(file).bucket(bucket).build();
        ResponseInputStream<GetObjectResponse> in = s3.getObject(objectRequest);
        return in;
    }
    
    private String upload_stream(String bucket, String[] file, BufferedImage bytes_data) throws IOException {
        File upload_file = new File(file[1]);
        ImageIO.write(bytes_data, "png", upload_file);
        String key = String.join("/", file);
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
        s3.putObject(objectRequest, RequestBody.fromFile(upload_file.toPath()));

        return key;
    }

    public static class FunInput {
        private int height;
        private int width;
        private String objectKey;
        private String input_bucket;
        private String output_bucket;
        public  String debug;

        public int getHeight() {
            return height;
	}

        public void setHeight(int h) {
            this.height = h;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int w) {
            this.width = w;
        }

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }

        public String getInput_bucket() {
            return input_bucket;
        }

        public void setInput_bucket(String input_bucket) {
            this.input_bucket = input_bucket;
        }

        public String getOutput_bucket() {
            return output_bucket;
        }

        public void setOutput_bucket(String output_bucket) {
            this.output_bucket = output_bucket;
        }
    }

    
    public static class RetValType {
        Map<String, String> result;
        Map<String, Double> measurement;

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public Map<String, Double> getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Map<String, Double> measurement) {
            this.measurement = measurement;
        }
    }
}
