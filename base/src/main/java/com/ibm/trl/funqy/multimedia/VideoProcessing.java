package com.ibm.trl.funqy.multimedia;

import com.ibm.trl.funqy.cosutils.COSUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;

import io.quarkus.funqy.Funq;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import javax.inject.Inject;

import software.amazon.awssdk.services.s3.S3Client;

public class VideoProcessing {
    private COSUtils client;

    @Inject
    S3Client s3;

    public VideoProcessing() throws Exception {
        client = new COSUtils();
    }
    
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
    public RetVal videoprocessing(Param param) throws Exception {
        String input_bucket = client.getInBucket();
        String output_bucket = client.getOutBucket();
        String key = param.getKey();
        int duration = param.getDuration();
        String op = param.getOp();
        String download_path = String.format("/tmp/%s", key);

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
        String out_key = f.getParent() + "/" + f1.getName();
        long upload_size = Files.size(new File(upload_path).toPath());
        upload(output_bucket, out_key, upload_path);
        long upload_stop = System.nanoTime();

        long download_time = (download_stop - download_begin)/1000;
        long upload_time = (upload_stop - upload_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        RetVal retVal = new RetVal();
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
        this.client.uploadFile(s3, output_bucket, filename, upload_path);
    }

    private void download(String input_bucket, String key, String download_path) throws Exception {
        this.client.downloadFile(s3, input_bucket, key, download_path);
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
