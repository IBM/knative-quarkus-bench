package com.ibm.trl.funqy.multimedia;

import com.ibm.trl.funqy.cloudevent.IBMCOS;

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


public class VideoProcessing {
    private IBMCOS client;

    public VideoProcessing() throws Exception {
        client = new IBMCOS();
    }
    
    /*
     * def call_ffmpeg(args):
     * ret = subprocess.run([os.path.join(SCRIPT_DIR, 'ffmpeg', 'ffmpeg'), '-y'] + args,
     *         #subprocess might inherit Lambda's input for some reason
     *         stdin=subprocess.DEVNULL,
     *         stdout=subprocess.PIPE, stderr=subprocess.STDOUT
     * )
     * if ret.returncode != 0:
     *     print('Invocation of ffmpeg failed!')
     *     print('Out: ', ret.stdout.decode('utf-8'))
     *     raise RuntimeError()
     */
    private void call_ffmpeg(String[] args) {
    }

    /*
     * # https://superuser.com/questions/556029/how-do-i-convert-a-video-to-gif-using-ffmpeg-with-reasonable-quality
     * def to_gif(video, duration, event):
     *     output = '/tmp/processed-{}.gif'.format(os.path.basename(video))
     *     call_ffmpeg(["-i", video,
     *         "-t",
     *         "{0}".format(duration),
     *         "-vf",
     *         "fps=10,scale=320:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse",
     *         "-loop", "0",
     *         output])
     *     return output
     */
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

    /*
     * # https://devopstar.com/2019/01/28/serverless-watermark-using-aws-lambda-layers-ffmpeg/
     * def watermark(video, duration, event):
     *     output = '/tmp/processed-{}'.format(os.path.basename(video))
     *     watermark_file = os.path.dirname(os.path.realpath(__file__))
     *     call_ffmpeg([
     *         "-i", video,
     *         "-i", os.path.join(watermark_file, os.path.join('resources', 'watermark.png')),
     *         "-t", "{0}".format(duration),
     *         "-filter_complex", "overlay=main_w/2-overlay_w/2:main_h/2-overlay_h/2",
     *         output])
     *     return output
     */
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

    /*
     * def transcode_mp3(video, duration, event):
     *    pass
     */
    BiFunction<String, Integer, String> transcode_mp3 = (video, duration) -> { return null; };

    /*
     * operations = { 'transcode' : transcode_mp3, 'extract-gif' : to_gif, 'watermark' : watermark }
     */
    private Map<String, BiFunction<String, Integer, String>> operations = Map.of("transcode", transcode_mp3,
                                                                                 "extract-gif", to_gif,
                                                                                 "watermark", watermark);
    
    @Funq
    public RetVal videoprocessing(Param param) throws Exception {
        /*
         * input_bucket = event.get('bucket').get('input')
         * output_bucket = event.get('bucket').get('output')
         * key = event.get('object').get('key')
         * duration = event.get('object').get('duration')
         * op = event.get('object').get('op')
         * download_path = '/tmp/{}'.format(key)
         */
        String input_bucket = client.getInBucket();
        String output_bucket = client.getOutBucket();
        String key = param.getKey();
        int duration = param.getDuration();
        String op = param.getOp();
        String download_path = String.format("/tmp/%s", key);

        /*
         * # Restore executable permission
         * ffmpeg_binary = os.path.join(SCRIPT_DIR, 'ffmpeg', 'ffmpeg')
         * # needed on Azure but read-only filesystem on AWS
         * try:
         *     st = os.stat(ffmpeg_binary)
         *     os.chmod(ffmpeg_binary, st.st_mode | stat.S_IEXEC)
         * except OSError:
         *     pass
         */
        /* no explicit code for java port */

        /*
         * download_begin = datetime.datetime.now()
         * client.download(input_bucket, key, download_path)
         * download_size = os.path.getsize(download_path)
         * download_stop = datetime.datetime.now()
         */
        long download_begin = System.nanoTime();
        download(input_bucket, key, download_path);
        long download_size = Files.size(new File(download_path).toPath());
        long download_stop = System.nanoTime();

        /*
         * process_begin = datetime.datetime.now()
         * upload_path = operations[op](download_path, duration, event)
         * process_end = datetime.datetime.now()
         */
        long process_begin = System.nanoTime();
        String upload_path = operations.get(op).apply(download_path, duration);
        long process_end = System.nanoTime();

        /*
         * upload_begin = datetime.datetime.now()
         * filename = os.path.basename(upload_path)
         * upload_size = os.path.getsize(upload_path)
         * client.upload(output_bucket, filename, upload_path)
         * upload_stop = datetime.datetime.now()
         */
        long upload_begin = System.nanoTime();
        File f1 = new File(upload_path);
        File f = new File(key);
        String out_key = f.getParent() + "/" + f1.getName();
        long upload_size = Files.size(new File(upload_path).toPath());
        upload(output_bucket, out_key, upload_path);
        long upload_stop = System.nanoTime();

        /*
         * download_time = (download_stop - download_begin) / datetime.timedelta(microseconds=1)
         * upload_time = (upload_stop - upload_begin) / datetime.timedelta(microseconds=1)
         * process_time = (process_end - process_begin) / datetime.timedelta(microseconds=1)
         */
        long download_time = (download_stop - download_begin)/1000;
        long upload_time = (upload_stop - upload_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        /*
         * return {
         *     'result': {
         *         'bucket': output_bucket,
         *         'key': filename
         *     },
         *     'measurement': {
         *         'download_time': download_time,
         *         'download_size': download_size,
         *         'upload_time': upload_time,
         *         'upload_size': upload_size,
         *         'compute_time': process_time
         *     }
         * }
         */
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
        this.client.uploadFile(output_bucket, filename, upload_path);
    }

    private void download(String input_bucket, String key, String download_path) {
        this.client.downloadFile(input_bucket, key, download_path);
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
