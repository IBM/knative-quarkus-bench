# helloworld Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.

To learn more about Quarkus, please refer to https://quarkus.io/ .

## Packaging and running the application

The application can be packaged using:
```shell script
mvn -Dskiptests clean package
```
This produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that this is not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

The server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
Functions with parameters only accept POST requests. Functions without parameters accept both GET and POST requests.

Sample curl command for testing the `/helloworld` function:
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/helloworld | jq
```
The result looks like:
```
{
  "result": 1
}
```
Valid choices of the test data size are `test`, `small`, and `large`, where the graph sizes are set to `1`, `100`, and `1,000`, respectively.

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

If GraalVM is not installed, the native executable can be built in a container using: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

To run the native image: `./target/helloworld-1.0.0-SNAPSHOT-runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

