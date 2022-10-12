# Video-processing Project

This is a Quarkus port of 220.video-processing from [SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This benchmark receives a video file as an input and generates a gif file or an watermark video using [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper).

## Preparing Input Data 

MP4 file we tested is found [here](https://github.com/spcl/serverless-benchmarks-data/tree/6a17a460f289e166abb47ea6298fb939e80e8beb/200.multimedia/220.video-processing).

The input file is assumed to be stored in Cloud Object Storage (COS). COS environment variable configuration is described [here]( ../UsingCloudObjectStorage.md).

## Building and Running the Application

Build the project as described in [this README](../../README.md).

Then, the application can run as a local HTTP server.
To run the stand-alone Java version:
```shell
java -jar target/quarkus-app/quarkus-run.jar
```
To run the stand-alone native version:
```shell
target/imae-recognition-1.0.0-SNAPSHOT-runner
```

This application receives the following parameters from POST data in JSON format:

|Name         |Value                         |Required?|Default|
|:-----------:|:------------------------------------|:-:|:------:|
|input_bucket |COS bucket to download input files     |Y|(None) |
|output_bucket|COS bucket to upload output files      |Y|(None) |
|key          |COS object key of the input file       |Y|(None) |
|duration     |Video duration in seconds              |N|duration of the input video |
|op           |"extract-gif" for gif generation or "watermark" for watermark video generation |Y|(None)|
|debug        |Flag if output is uploaded to COS      |N|false  |

For example:

```shell
$ curl -s -w "\n" -H 'Content-Type:application/json' -d '{"key": "Anthem-30-16x9-lowres.mp4", "duration": "1", "op": "extract-gif"}' -X POST http://localhost:8080/video-processing | jq
```
The result looks like:
```
{
  "result": {
    "bucket": "trl-knative-benchmark-bucket-2",
    "key": ""
  },
  "measurement": {
    "upload_time": 0,
    "compute_time": 0.854352925,
    "output_size": 1038553,
    "download_time": 2.680993329,
    "download_size": 1391747
  }
}
```

## Customizing the Default Value of Input Parameters

Some parameters can be customized via environment variables or `application.properties`.
