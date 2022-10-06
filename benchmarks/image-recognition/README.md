# Image-recognition Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.

This benchmark uses [Deep Java Library](https://djl.ai/).

To learn more about Quarkus, please refer to https://quarkus.io/ .

## Preparation 

Since the image-recognition benchmark attempts to download three input files from Cloud Object Storage, following preparation steps are required.
1) Follow the instructions in [this README]:(../UsingCloudObjectStorage.md).

2) Upload input files to the bucket:
- [resnet50.pt](src/main/resources/resnet50.pt)
- [synset.txt](src/main/resources/synset.txt)
- JPG files for inference processing. The JPG files we tested are found at https://github.com/spcl/serverless-benchmarks-data/tree/6a17a460f289e166abb47ea6298fb939e80e8beb/400.inference/411.image-recognition/fake-resnet.

## Running the application in dev mode

To run the application in dev mode that enables live coding use:
```shell script
mvn compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
mvn package
```
This creates a `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

The server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path.
Functions with parameters only accept POST requests. Functions without parameters accept both GET and POST requests.

A sample curl command for testing the `/image-recognition` function:
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input":"782px-Pumiforme.JPG","model":"resnet50.pt","synset":"synset.txt"}' -X POST http://localhost:8080/image-recognition | jq
```
The result looks like:
```
{
  "result": {
    "class": "Egyptian cat"
  },
  "measurement": {
    "model_download_time": 26881487,
    "compute_time": 323451,
    "download_time": 28149240,
    "model_time": 514
  }
}
```


To build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is runnable using `java -jar target/*-runner.jar`.

## Building a native executable

Build a native executable using: 
```shell script
mvn package -Pnative
```

If GraalVM is not installed, a native executable can be built in a container using: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

To execute the native executable: `./target/image-recognition-1.0.0-SNAPSHOT.runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.
