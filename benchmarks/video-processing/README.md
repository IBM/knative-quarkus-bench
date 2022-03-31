# Video-processing Project

This is a project to port and test [serverless-benchmarks](https://github.com/spcl/serverless-benchmarks) using Quarkus
[Funqy HTTP Binding](https://quarkus.io/guides/funqy-http), which creates a stand-alone application using serverless functions.
This interim step should be useful to port the benchmark suite to knative environment using [Quarkus Funqy](https://quarkus.io/guides/funqy).
This project is also useful to verify if there is any problems to build into native images.

Video-processing benchmark uses [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper).

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Packaging and running the application

The application can be packaged using:
```shell script
mvn clean package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

Now the server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
The functions taking parameters only accespt POST request. The functions taking no parameter accept both GET and POST request.

Sample curl command for testing the `/video-processing` function:

Sample curl command for testing the `/video_processing` function:
```
$ curl -s -w "\n" -H 'Content-Type:application/json' -d '{"key": "Anthem-30-16x9-lowres.mp4", "height": "128", "width": "128", "duration": "1", "op": "extract-gif"}' -X POST http://localhost:8080/video_processing | jq
```
Result looks like:
```
{
  "result": {
    "key": "processed-Anthem-30-16x9-lowres.gif",
    "bucket": "trl-knative-benchmark-bucket-2"
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

Be careful about quartation marks. In Funqy, post data need to be JSON format. So, a string value in post data needs to have double quatation marks (`"`)
aroud the data. You may also need another quartation marks or back-quote (`\`) to avoid shell command line interpretation.

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

You can then execute your native executable with: `./target/video-processing-1.0.0-SNAPSHOT.jar`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
