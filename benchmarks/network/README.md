# Network Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.

To learn more about Quarkus, please refer to https://quarkus.io/ .

## Optional preparation step

This benchmark attempts to upload a result file via Cloud Object Storage unless `skipUploading` parameter is true (See [an example curl command](#sample-curl-command)). 

To upload result files, configure cloud object storage as described in [this README]:(../UsingCloudObjectStorage.md).

## Packaging and running the application

The application can be packaged using:
```shell script
mvn -Dskiptests clean package
```
This produces a `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

The server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
Functions with parameters only accept POST requests. Functions without parameters accept both GET and POST requests.

### Sample curl command:
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "20202", "repetitions": "5", "request_id":"IpsumLoremomethingSayunnySay", "income_timestamp": "test", "skipUploading":"true"}' -X POST http://localhost:8080/network | jq
```
The result looks like:
```
{
  "result": "network-benchmark-results-IpsumLoremomethingSayunnySay.csv"
}
```
Valid choices of the test data size are `test`, `small`, and `large`, where the graph sizes are set to `1`, `100`, and `1,000`, respectively.

Be careful with quotation marks. In Funqy, post data must to be in JSON format. So, a string value in post data needs to have double quotation marks (`"`)
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

If GraalVM is not installed, a native executable can be built in a container: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

To run a native executable: `./target/network-1.0.0-SNAPSHOT-runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
