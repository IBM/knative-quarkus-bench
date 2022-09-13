# Thumbnailer Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.

To learn more about Quarkus, please refer to https://quarkus.io/ .

## Preparation 

Since thumbnailer benchmark attempts to download and upload files via Cloud Object Storage, following preparation steps are required.
1) Follow the instructions in [this README]:(../UsingCloudObjectStorage.md).

2) Upload input files to the bucket
- Image files we tested are found [here](https://github.com/spcl/serverless-benchmarks-data/tree/6a17a460f289e166abb47ea6298fb939e80e8beb/200.multimedia/210.thumbnailer).

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

The `/pagerank` function receives a test data size as a string, and returns result in JSON format:
```
$ curl -s -w "\n" -H 'Content-Type:application/json' -d '{"objectkey": "index.png", "height": "128", "width": "128"}' -X POST http://localhost:8080/thumbnailer | jq
{
  "result": {
    "key": "resized-index.png",
    "bucket": "sample-knative-benchmark-bucket"
  },
  "measurement": {
    "download_size": 116060,
    "download_time": 1106799,
    "compute_time": 1577,
    "upload_size": 125,
    "upload_time": 1187295
  }
}

```
Valid choices of the test data size are `test`, `small`, and `large`, where the graph sizes are set to `10`, `10,000`, and `100,000`, respectively.

Be careful with quotation marks. In Funqy, post data need to be in JSON format. So, a string value in post data needs to have double quotation marks (`"`)
around the data. You may also need another quotation mark or back-slash (`\`) to avoid shell command line interpretation.

### Building an über-jar
To build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

To build a native executable use: 
```shell script
mvn package -Pnative
```

If GraalVM is not installed, the native executable can be built in a container using: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

Run the native executable with: `./target/knative-quarkus-bench-thumbnailer-1.0.0-SNAPSHOT-runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
