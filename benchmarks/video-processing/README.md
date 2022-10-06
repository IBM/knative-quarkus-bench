# Video-processing Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.

This benchmark uses [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper).

To learn more about Quarkus, please refer to https://quarkus.io/ .

## Preparation 

Since video-processing benchmark attempts to download and upload files via Cloud Object Storage, the following preparation is required.

1) Follow the instructions in [this README]:(../UsingCloudObjectStorage.md).

2) Upload input files to the bucket
- MP4 file we tested is found [here](https://github.com/spcl/serverless-benchmarks-data/tree/6a17a460f289e166abb47ea6298fb939e80e8beb/200.multimedia/220.video-processing).


## Packaging and running the application

The application can be packaged using:
```shell script
mvn clean package
```
This produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

The server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
Functions with parameters only accept POST requests. Functions without parameters accept both GET and POST requests.

Sample curl command for testing the `/video-processing` function:

```
$ curl -s -w "\n" -H 'Content-Type:application/json' -d '{"key": "Anthem-30-16x9-lowres.mp4", "height": "128", "width": "128", "duration": "1", "op": "extract-gif"}' -X POST http://localhost:8080/video-processing | jq
```
The result looks like:
```
{
  "result": {
    "key": "processed-Anthem-30-16x9-lowres.gif",
    "bucket": "sample-knative-benchmark-bucket-2"
  },
  "measurement": {
    "download_time": 9790609,
    "compute_time": 2318449,
    "upload_size": 1177929,
    "upload_time": 2188424,
    "download_size": 1391747
  }
}
```

Be careful with quotation marks. In Funqy, post data needs to be JSON format. So, a string value in post data needs to have double quotation marks (`"`)
around the data. You may also need another quotation mark or back-slash (`\`) to avoid shell command line interpretation.

### Building an über-jar
To build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

To build a native executable: 
```shell script
mvn package -Pnative
```

If GraalVM is not installed, build the native executable in a container using: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

Run the native executable with: `./target/video-processing-1.0.0-SNAPSHOT.runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
