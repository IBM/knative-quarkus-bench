# multimedia-funqy Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.
This interim step should be useful to port the benchmark suite to knative environment using [Quarkus Funqy](https://quarkus.io/guides/funqy).
This project is also useful to verify if there is any problems to build into native images.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Packaging and running the application

The application can be packaged using:
```shell script
$ ./mvnw clean package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:
```shell script
$ java -jar target/quarkus-app/quarkus-run.jar
```

Now the server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
The functions taking parameters only accespt POST request. The functions taking no parameter accept both GET and POST request.

The `/pagerank` function receives a test data size as a string, and returns result in JSON format:
```
$ curl -s -w "\n" -H 'Content-Type:application/json' -d '{"objectkey": "index.png", "height": "128", "width": "128"}' -X POST http://localhost:8080/thumbnailer | jq
{
  "result": {
    "key": "resized-index.png",
    "bucket": "trl-knative-benchmark-bucket-2"
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
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/knative-serverless-benchmark-thumbnailer-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
