# Network Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.
This interim step should be useful to port the benchmark suite to knative environment using [Quarkus Funqy](https://quarkus.io/guides/funqy).
This project is also useful to verify if there is any problems to build into native images.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Optional preparation step

Network benchmark attempts to upload a result file via Cloud Object Storage unless `skipUploading` parameter is true (See [an example curl command](#sample-curl-command)). 

If you want to updalod result files, it is required to setup [application.properties](src/main/resources/application.properties) to give an endpoint URL and bucket names such as
```
knativebench.network.output_bucket=knative-benchmark-output-bucket
quarkus.s3.endpoint-override=https://s3.us-south.cloud-object-storage.appdomain.cloud
```

## Packaging and running the application

The application can be packaged using:
```shell script
mvn -Dskiptests clean package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

Now the server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
The functions taking parameters only accepts POST request. The functions taking no parameter accept both GET and POST request.

### Sample curl command:
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "20202", "repetitions": "5", "request_id":"IpsumLoremomethingSayunnySay", "income_timestamp": "test", "skipUploading":"true"}' -X POST http://localhost:8080/network | jq
```
Result looks like:
```
{
  "result": "network-benchmark-results-IpsumLoremomethingSayunnySay.csv"
}
```
Valid choices of the test data size are `test`, `small`, and `large`, where the graph sizes are set to `1`, `100`, and `1,000`, respectively.

Be careful about quotation marks. In Funqy, post data need to be JSON format. So, a string value in post data needs to have double quotation marks (`"`)
around the data. You may also need another quotation mark or back-slash (`\`) to avoid shell command line interpretation.

### Building an über-jar
If you want to build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
mvn package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/network-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
