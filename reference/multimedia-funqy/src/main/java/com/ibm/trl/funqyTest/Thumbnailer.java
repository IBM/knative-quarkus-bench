package com.ibm.trl.funqyTest;

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

import io.quarkus.funqy.Funq;


public class Thumbnailer {
    private COSUtils client = new COSUtils();
    
    /*
     * def resize_image(image_bytes, w, h):
     *   with Image.open(io.BytesIO(image_bytes)) as image:
     *       image.thumbnail((w,h))
     *       out = io.BytesIO()
     *       image.save(out, format='jpeg')
     *       # necessary to rewind to the beginning of the buffer
     *       out.seek(0)
     *       return out
     */
    public BufferedImage resize_image(BufferedImage bimg, int w, int h) throws IOException {
        Image thumbnail = bimg.getScaledInstance(w, h, Image.SCALE_DEFAULT);
        BufferedImage ret_image = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
        return ret_image;
    }
    
    @Funq
    public RetVal thumbnailer(Param param) throws Exception {
        /*
         * input_bucket = event.get('bucket').get('input')
         * output_bucket = event.get('bucket').get('output')
         * key = unquote_plus(event.get('object').get('key'))
         * width = event.get('object').get('width')
         * height = event.get('object').get('height')
         */
        int object_height = param.getHeight();
        int object_width = param.getWidth();
        String input_bucket = client.getInBucket();
        String output_bucket = client.getOutBucket();
        String key = param.getObjectKey().replaceAll(" ", "+");
        int width = object_width;
        int height = object_height;

        /*
         * download_begin = datetime.datetime.now()
         * img = client.download_stream(input_bucket, key)
         * download_end = datetime.datetime.now()
         */
        long download_begin = System.nanoTime();
        InputStream img = download_stream(input_bucket, key);
        long download_end = System.nanoTime();

        /*
         * process_begin = datetime.datetime.now()
         * resized = resize_image(img, width, height)
         * resized_size = resized.getbuffer().nbytes
         * process_end = datetime.datetime.now()
         */
        BufferedImage bimg = ImageIO.read(img);
        long image_size = len(bimg);
        long process_begin = System.nanoTime();
        BufferedImage resized = resize_image(bimg, width, height);
        long resized_size = len(resized);
        long process_end = System.nanoTime();

        /*
         * upload_begin = datetime.datetime.now()
         * key_name = client.upload_stream(output_bucket, key, resized)
         * upload_end = datetime.datetime.now()
         */
        long upload_begin = System.nanoTime();
        File f = new File(key);
        String[] out_key = new String[] {f.getParent(), "resized-" + f.getName()};
        String key_name = upload_stream(output_bucket, out_key, resized);
        long upload_end = System.nanoTime();

        /*
         * download_time = (download_end - download_begin) / datetime.timedelta(microseconds=1)
         * upload_time = (upload_end - upload_begin) / datetime.timedelta(microseconds=1)
         * process_time = (process_end - process_begin) / datetime.timedelta(microseconds=1)
         */
        long download_time = (download_end - download_begin)/1000;
        long upload_time = (upload_end - upload_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        /*
         * return {
         *   'result': {
         *       'bucket': output_bucket,
         *       'key': key_name
         *   },
         *   'measurement': {
         *       'download_time': download_time,
         *       'download_size': len(img),
         *       'upload_time': upload_time,
         *       'upload_size': resized_size,
         *       'compute_time': process_time
         *   }
         */
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

    /*
     * def download_stream(self, bucket, file):
     *   data = self.client.get_object(bucket, file)
     *   return data.read()
     */
    private InputStream download_stream(String bucket, String file) throws FileNotFoundException {
        InputStream data = this.client.get_object(bucket, file);
        return data;
    }
    
    /*
     * def upload_stream(self, bucket, file, bytes_data):
     *   key_name = storage.unique_name(file)
     *   self.client.put_object(bucket, key_name, bytes_data, bytes_data.getbuffer().nbytes)
     *   return key_name
     */
    private String upload_stream(String bucket, String[] file, BufferedImage bytes_data) throws IOException {
        File upload_file = new File("/tmp/" + file[1]);
        ImageIO.write(bytes_data, "png", upload_file);
        String key = String.join("/", file);
        this.client.put_object(bucket, key, upload_file);
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
