# Image-recognition Project

This is a Quarkus port of 400.inference from [SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This application provides image classification feature using a deep learning model from Model Zoo with [Deep Java Library](https://djl.ai/).

## Preparing Input Data

- [resnet50.pt](src/main/resources/resnet50.pt)
- [synset.txt](src/main/resources/synset.txt)
- JPG files for inference processing. The JPG files we tested are found at https://github.com/spcl/serverless-benchmarks-data/tree/6a17a460f289e166abb47ea6298fb939e80e8beb/400.inference/411.image-recognition/fake-resnet.

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
target/image-recognition-1.0.0-SNAPSHOT-runner
```

This application receives the following parameters from POST data in JSON format:

|Name         |Value                         |Required?|Default|
|:-----------:|:------------------------------------|:-:|:------:|
|input_bucket |COS bucket to download input files     |Y|(None) |
|input        |COS object key of the input file       |Y|(None) |
|model        |Model file name                        |Y|(None) |
|synset       |Synset file name                       |Y|(None) |

For example:

```shell
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input":"782px-Pumiforme.JPG","model":"resnet50.pt","synset":"synset.txt"}' -X POST http://localhost:8080/image-recognition | jq
```
The result looks like:
```
{
  "result": {
    "class": "Egyptian cat"
  },
  "measurement": {
    "model_time": 0.000400447,
    "download_time": 60.572403084,
    "compute_time": 0.340404358,
    "model_download_time": 57.910619015
  }
}
```

## Customizing the Default Value of Input Parameters

Some parameters can be customized via environment variables or `application.properties`.
