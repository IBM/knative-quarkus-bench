# microbenchmark-funqy Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.
This interim step should be useful to port the benchmark suite to knative environment using [Quarkus Funqy](https://quarkus.io/guides/funqy).
This project is also useful to verify if there is any problems to build into native images.

This project currently focus on graph-based benchmarks in `000.microbenchmarks` group, but it should be usable for other groups of the benchmark suite. 
We use [JGraphT graph library](https://github.com/jgrapht/jgrapht) as a replacement of [Python igraph library](https://igraph.org/), 
 nwhich is used in the original benchmark implementation.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Packaging and running the application

The application can be packaged using:
```shell script
$ mvn -Dskiptests clean package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:
```shell script
$ java -jar target/quarkus-app/quarkus-run.jar
```

Now the server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
The functions taking parameters only accespt POST request. The functions taking no parameter accept both GET and POST request.

The `/sleep` function receives a test data size as a string, and returns result in JSON format:
```
$ curl -s -d '"test"' -X POST http://localhost:8080/[method-name] | jq
{
  "result": ...
}
```
Valid choices of the test data size are `test`, `small`, and `large`, where the graph sizes are set to `1`, `100`, and `1,000`, respectively.

Be careful about quartation marks. In Funqy, post data need to be JSON format. So, a string value in post data needs to have double quatation marks (`"`)
aroud the data. You may also need another quartation marks or back-quote (`\`) to avoid shell command line interpretation.

### Building an über-jar
If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
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

You can then execute your native executable with: `./target/jgrapht-funqy-0.0.1-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.


### Sample curl commands for testing
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "0.0.0.0", "server_port": "20202", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test", "skipUploading":"true"}' -X POST http://localhost:8080/clock_synchronization | jq
```
