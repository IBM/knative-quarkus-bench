# Thumbnailer Project

This is a Quarkus port of 210.thumbnailer from [SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This application downloads an image file as the input from Cloud Object Storage (COS), generates a thumbnail image of the given file, and uploads the thumbnail image.

## Preparing Input Data

Input files we tested can be found found [here](https://github.com/spcl/serverless-benchmarks-data/tree/6a17a460f289e166abb47ea6298fb939e80e8beb/200.multimedia/210.thumbnailer).

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
target/thumbnailer-1.0.0-SNAPSHOT-runner
```

This application receives the following parameters from POST data in JSON format:

|Name         |Value                         |Required?|Default|
|:-----------:|:------------------------------------|:-:|:------:|
|input_bucket |COS bucket to download input files     |Y|(None) |
|output_bucket|COS bucket to upload output files      |Y|(None) |
|objectkey    |COS object key of the input file       |Y|(None) |
|height       |Height of resized image file           |Y|(None) |
|width        |Width of resized image file            |Y|(None) |
|debug        |Flag if output is uploaded to COS      |N|false  |

For example:

```shell
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"objectkey": "index.png", "height": "128", "width": "128"}' -X POST http://localhost:8080/thumbnailer | jq
{
  "result": {
    "bucket": "trl-knative-benchmark-bucket-2",
    "key": ""
  },
  "measurement": {
    "download_time": 1.988398514,
    "download_size": 116060,
    "upload_time": 0,
    "upload_size": 21704,
    "compute_time": 0.050419163
  }
}
```
## Customizing the Default Value of Input Parameters

Some parameters can be customized via environment variables or `application.properties`.
