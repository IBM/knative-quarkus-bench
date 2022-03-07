package com.ibm.trl.serverlessbench;

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

import io.quarkus.funqy.Funq;

import software.amazon.awssdk.services.s3.S3Client;


public class Thumbnailer {
    private COSUtils client;

    @Inject
    S3Client s3;

    public Thumbnailer() throws Exception {
        client = new COSUtils();
    }
    
    public BufferedImage resize_image(BufferedImage bimg, int w, int h) throws IOException {
        Image thumbnail = bimg.getScaledInstance(w, h, Image.SCALE_DEFAULT);
        BufferedImage ret_image = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
        return ret_image;
    }
    
    @Funq
    public RetVal thumbnailer(Param param) throws Exception {
        int object_height = param.getHeight();
        int object_width = param.getWidth();
        String input_bucket = client.getInBucket();
        String output_bucket = client.getOutBucket();
        String key = param.getObjectKey().replaceAll(" ", "+");
        int width = object_width;
        int height = object_height;

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
        File f = new File(key);
        String[] out_key = new String[] {f.getParent(), "resized-" + f.getName()};
        String key_name = upload_stream(output_bucket, out_key, resized);
        long upload_end = System.nanoTime();

        long download_time = (download_end - download_begin)/1000;
        long upload_time = (upload_end - upload_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        RetVal retVal = new RetVal();
        retVal.result = Map.of(     "bucket", output_bucket,
                                    "key", key_name);
        retVal.measurement = Map.of("download_time", download_time,
                                    "download_size", image_size,
                                    "upload_time", upload_time,
                                    "upload_size", resized_size,
                                    "compute_time", process_time);

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
        InputStream data = this.client.get_object(s3, bucket, file);
        return data;
    }
    
    private String upload_stream(String bucket, String[] file, BufferedImage bytes_data) throws IOException {
        File upload_file = new File("/tmp/" + file[1]);
        ImageIO.write(bytes_data, "png", upload_file);
        String key = String.join("/", file);
        this.client.put_object(s3, bucket, key, upload_file);
        return key;
    }
    
    public static class RetVal {
        Map<String, String> result;
        Map<String, Long> measurement;

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public Map<String, Long> getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Map<String, Long> measurement) {
            this.measurement = measurement;
        }

        RetVal() {
            measurement = new HashMap<String, Long>();
        }
    }
}
