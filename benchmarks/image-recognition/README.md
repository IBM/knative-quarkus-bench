# Image-recognition Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
mvn compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
mvn package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

Now the server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path.
The functions taking parameters only accespt POST request. The functions taking no parameter accept both GET and POST request.

The `/image_recognition` function receives a test data size as a string, and returns result in JSON format:
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input":"782px-Pumiforme.JPG","model":"resnet50.pt"}' -X POST http://localhost:8080/image_recognition | jq

{
  "result": {
    "idx": "",
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

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)