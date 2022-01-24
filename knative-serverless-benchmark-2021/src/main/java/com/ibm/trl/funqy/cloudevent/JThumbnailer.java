package com.ibm.trl.funqy.cloudevent;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.jboss.logging.Logger;


public class JThumbnailer extends BenchmarkTest {
    private IBMCOS client;
    private Logger log;
    private UUID uuid;
    
    

    public JThumbnailer() throws Exception {
        uuid = UUID.randomUUID();
        client = new IBMCOS();
        log = Logger.getLogger(JThumbnailer.class);
    }
    
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
    public Image resize_image(InputStream image_bytes, int w, int h) throws IOException {
        Image thumbnail = ImageIO.read(image_bytes).getScaledInstance(w, h, Image.SCALE_DEFAULT);
        return thumbnail;
    }
    
    public String runTest(int object_height, String object_key, int object_width) throws Exception {
        if (!client.available())
            return ("ERROR: JThumbnailer.runTest() unable to run since IBMCOS unavailable.");

        System.out.printf("Starting JThumbnailer:%d, %s, %d\n", object_height, object_key, object_width );
 
        /*
         * input_bucket = event.get('bucket').get('input')
         * output_bucket = event.get('bucket').get('output')
         * key = unquote_plus(event.get('object').get('key'))
         * width = event.get('object').get('width')
         * height = event.get('object').get('height')
         */
        String input_bucket = client.getInBucket();
        String output_bucket = client.getOutBucket();
        String key = object_key.replaceAll(" ", "+");
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
        long process_begin = System.nanoTime();
        Image resized = resize_image(img, width, height);
        int resized_size = 0; // how can i get nbytes?
        long process_end = System.nanoTime();
        
        /*
         * upload_begin = datetime.datetime.now()
         * key_name = client.upload_stream(output_bucket, key, resized)
         * upload_end = datetime.datetime.now()
         */
        long upload_begin = System.nanoTime();
//        String key_name = upload_stream(output_bucket, key, resized);  // KO commented out
        long upload_end = System.nanoTime();

        return null;  // KO added
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
    }
    
    /*
     * def download_stream(self, bucket, file):
     *   data = self.client.get_object(bucket, file)
     *   return data.read()
     */
    private InputStream download_stream(String bucket, String key) throws FileNotFoundException {
//        InputStream data = this.client.get_object(bucket, key);  // KO commented out
//        return data;  // KO commented out
        return null;  // KO added
    }
    
    /*
     * def upload_stream(self, bucket, file, bytes_data):
     *   key_name = storage.unique_name(file)
     *   self.client.put_object(bucket, key_name, bytes_data, bytes_data.getbuffer().nbytes)
     *   return key_name
     */
    private void upload_stream(String bucket, String file, Image bytes_data) {
//        return client.upload_stream();  // KO commented out
    }
    
    public String runTest() throws Exception {  // KO added "throws Eception"
        /*
         * "object": {
         *   "height": 200,
         *   "key": "7_beach-bird-s-eye-view-coast-2499700.jpg",
         *   "width": 200
         * }
         */
        return runTest(200, "7_beach-bird-s-eye-view-coast-2499700.jpg", 200);
    }
    
}
