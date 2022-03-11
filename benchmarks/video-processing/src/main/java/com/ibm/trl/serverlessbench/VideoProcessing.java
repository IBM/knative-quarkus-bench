package com.ibm.trl.serverlessbench;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiFunction;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;

import javax.inject.Inject;

import io.quarkus.funqy.Funq;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


public class VideoProcessing {
    @Inject
    S3Client s3;

    @ConfigProperty(name = "serverlessbench.video-processing.input_bucket")
    String input_bucket;

    @ConfigProperty(name = "serverlessbench.video-processing.output_bucket")
    String output_bucket;

    private void call_ffmpeg(String[] args) {
    }

    BiFunction<String, Integer, String> to_gif = (video, duration) -> {
        String output = String.format("/tmp/processed-%s.gif", video.substring(video.lastIndexOf('/')+1, video.lastIndexOf('.')));
        FFmpeg ffmpeg = null;
        FFprobe ffprobe = null;
        try {
            ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            ffprobe = new FFprobe("/usr/bin/ffprobe");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(video)
                .overrideOutputFiles(true)
                .addOutput(output)
                .setFormat("mp4")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);        
        executor.createJob(builder).run();

        return output;
    };

    BiFunction<String, Integer, String> watermark = (video, duration) -> {
        String output = String.format("/tmp/processed-%s.gif", video.substring(video.lastIndexOf('/')+1, video.lastIndexOf('.')));
        String watermark_file = "src/main/resources/watermark.png";
        FFmpeg ffmpeg = null;
        FFprobe ffprobe = null;
        try {
            ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            ffprobe = new FFprobe("/usr/bin/ffprobe");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(video)
                .addInput(watermark_file)
                .overrideOutputFiles(true)
                .addOutput(output)
                .setFormat("mp4")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        return output;
    };

    BiFunction<String, Integer, String> transcode_mp3 = (video, duration) -> { return null; };

    private Map<String, BiFunction<String, Integer, String>> operations = Map.of("transcode", transcode_mp3,
                                                                                 "extract-gif", to_gif,
                                                                                 "watermark", watermark);
    
    @Funq
    public RetValType video_processing(FunInput input) throws Exception {
        String key = input.getKey();
        int duration = input.getDuration();
        String op = input.getOp();
        String download_path = String.format("/tmp/%s", key);
	if (input.getInput_bucket() != null) {
            input_bucket = input.getInput_bucket();
	}
	if (input.getOutput_bucket() != null) {
            output_bucket = input.getOutput_bucket();
	}

        long download_begin = System.nanoTime();
        download(input_bucket, key, download_path);
        long download_size = Files.size(new File(download_path).toPath());
        long download_stop = System.nanoTime();

        long process_begin = System.nanoTime();
        String upload_path = operations.get(op).apply(download_path, duration);
        long process_end = System.nanoTime();

        long upload_begin = System.nanoTime();
        File f1 = new File(upload_path);
        File f = new File(key);
        String out_key = ((f.getParent() != null)? f.getParent() + "/" : "") + f1.getName();
        long upload_size = Files.size(new File(upload_path).toPath());
        upload(output_bucket, out_key, upload_path);
        long upload_stop = System.nanoTime();

        long download_time = (download_stop - download_begin)/1000;
        long upload_time = (upload_stop - upload_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        RetValType retVal = new RetValType();
        retVal.result = Map.of(     "bucket", output_bucket,
                                    "key", out_key);
        retVal.measurement = Map.of("download_time", download_time,
                                    "download_size", download_size,
                                    "upload_time", upload_time,
                                    "upload_size", upload_size,
                                    "compute_time", process_time);
        return retVal;
    }
    
    private void upload(String output_bucket, String filename, String upload_path) throws Exception {        
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(output_bucket).key(filename).build();
        s3.putObject(objectRequest, RequestBody.fromFile(new File(upload_path).toPath()));
    }

    private void download(String input_bucket, String key, String download_path) throws Exception {
        File theFile = new File(download_path);
        File theDir = theFile.getParentFile();
        if (!theDir.exists())
            theDir.mkdirs();
        GetObjectRequest request = GetObjectRequest.builder().bucket(input_bucket).key(key).build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(request);
        byte[] data = objectBytes.asByteArray();
        OutputStream os = new FileOutputStream(theFile);
        os.write(data);
        os.close();
    }

    public static class FunInput {
        private int height;
        private int width;
        private String key;
        private int duration;
        private String op;
	private String input_bucket;
	private String output_bucket;

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
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
    }
}
