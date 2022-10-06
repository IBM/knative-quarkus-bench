# Clock-synchronization Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.

To learn more about Quarkus, please refer to https://quarkus.io/ .

## Optional preparation step

The Clock-synchronization benchmark attempts to upload a result file via Cloud Object Storage unless the `skipUploading` parameter is set to `true` (See [an example curl command](#sample-curl-command)). 

Refer to [cloud Storage Configuration](../UsingCloudObjectStorage.md) to enable upload of result files to Cloud Storage.

## Packaging and running the application

The application can be packaged using:
```shell script
mvn -Dskiptests clean package
```
This produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
It is not an _über-jar_ as dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

The server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
Functions that have parameters only accept POST requests. Functions without parameters accept both GET and POST requests.

### Sample curl command
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "20202", "repetitions": "1", "output_bucket": "sample-knative-benchmark-bucket", "income_timestamp": "test", "skipUploading":"true"}' -X POST http://localhost:8080/clock-synchronization | jq
```
The result looks like this:
```
{
  "result": "clock-synchronization-benchmark-results-tmp_key.csv"
}
```

### Building an über-jar
To build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

To build a native executable: 
```shell script
mvn package -Pnative
```

If GraalVM is not installed, the the native executable can be built in a container: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

The native image can be executed with: `./target/clock-synchronization-1.0.0-SNAPSHOT.runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

