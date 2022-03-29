# knative-serverless-benchmark Project

This project contains a suite of quarkus programs runnable as both stand-alone
HTTP applications and knative serverless services.
We borrowed the set of programs from
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks)
developed by a team of ETH Z&uuml;rich.

Quarkus is a cloud native Java framework based on modern standard APIs.
More information about its fancy features are available at https://quarkus.io/.

## Build Instructions

### Clone git repo
```shell
git clone https://github.ibm.com/trl-quarkus/knative-serverless-benchmark.git
```
<!-- Replace with the following link when this project is published -->
<!-- https://github.com/IBM/knative-serverless-benchmark.git -->

### Building the application
```shell
cd knative-serverless-benchmark
mvn package
```

This step builds all benchmark programs and creates docker iamges for each of them.
Note that the version tag is set to `:jvm`, so that you can distinguish if an image
is for a Java implementation or a native implementation, described bellow.

This project uses quarkus
[container-image-docker extension](https://quarkus.io/guides/container-image#docker)
to build docker image.  This extension has several configuration parameters to build
images with appropriate tags and to push the image to a container repository.
The configuration parameters can be specified as system parameters of maven.

To create an image that will be pushed to quay.io repository,
```shell
mvn package -Dquarkus.container-image.resistry=quay.io -Dquarkus.container-image.group=mygroup
```
Then you will get container images with tags like: `quay.io/mygroup/thumbnailer:jvm`.

If you use `podman` instead of `docker`,
```shell
mvn package -Dquarkus.docker.executable=podman
```

If you don't need to build docker images, you can disable it by:
```shell
mvn package -Dquarkus.container-image.build=false
```

For other configuration parameters, refer to
[the guide document](https://quarkus.io/guides/container-image#customizing).


### Creating a native executable if interested

One of a major feature of Quarkus is native binary support.
You can build the five microservices as native binaries
by adding `-Pnative` in the Maven command line:
```shell
mvn package -Pnative
```

The version tag for native executable images is set to `:native`.

Building native executables uses
[GraalVM Native Image](https://www.graalvm.org/22.0/reference-manual/native-image/).
If `native-image` isn't set up in your machine, quarkus automatically downloads
a container image of the latest GraalVM with Native Image and builds the project
in the container.  We recommned this automated containerized build
because it automatically uses the latest version of GraalVM, which is updated frequetly.


## Usage Instructions

### Setting up cloud object storage for storing benchmark data

Some of the benchmark programs use cloud object storage to store input/output data.
The object storage needs to support Amazon S3 compatible APIs because we use quarkus
[Amazon S3 Client extension](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html)
to access object storage.

The minimum required configurations are:
1. Specify endpoint URL and region name in `src/main/resources/application.property`
under each submodule,
1. Specify credentials using two environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`

For other configuration parameters, refer to
[the guide document](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html#_configuring_s3_clients).

We tested our programs using sample input data that is published by the ETH Z&uuml;rich team
in https://github.com/spcl/serverless-benchmarks-data.git.


### Running as stand-alone programs

Each submodule creates a runnable JAR file or an executable binary.

To run Java version:
```shell
java -jar benchmarks/<benchname>/target/quarkus-app/quarkus-run.jar
```
To run native version:
```shell
benchmarks/<benchname>/target/<benchname>-1.0.0-SNAPSHOT-runner
```

The program works as an HTTP server listning a port 8080 and receiving/replying a JSON object.
You can use `curl` command to access the program, with specifying the input data as a POST data
in JSON format. For example:
```shell
curl http://localhost:8080/thmbnailer \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{ "input_bucket": "inBucket", "output_bucket": "outBucket", \
           "width": 1920, "height", 1080 }'
```


For more detail about running and configuring a stand-alone program, refer to the guide of
[quarkus Funqy HTTP extension](https://quarkus.io/guides/funqy-http).


### Running in knative cloud environment

#### Prerequisite

Knative needs to be installed in your Kubernetes/OpenShift environment.
If not, the easiest way to install Knative is to use Kubernetes Operator, as described in
https://knative.dev/docs/install/operator/knative-with-operators/.

A broker needs to be set up in your namespace if it hasn't.  The step is described in
[the document](https://knative.dev/docs/eventing/getting-started/#adding-a-broker-to-the-namespace).

Note that only a single broke is needed in a namespace regardless of the nubmer of
knative events services.


#### Deploying Knative Services

As described in
[the getting stated document](https://knative.dev/docs/eventing/getting-started/),
you need to prepare "Service" and "Trigger" resources for each service.
Although this decument configure "Deployment" manually, you can skip "Deployment"
by utilizing helper feature of knative as described in the guide of
[quarkus Funqy Knative Events extension](https://quarkus.io/guides/funqy-knative-events).

Note that "apiVersion" is "serving.knative.dev/v1" in the funkey knative's guide, instead of
"v1" in the knative's guide.


#### Accessing Knative Services

Knative services receives input as a [Cloud Event](https://cloudevents.io/) object
from the broker and returns the result back to the broker as a newly created Cloud Event.

The Cloud Event specification defines various HTTP headers starting with "Ce-".
The following table describes the minumum required headers.
|Header          |Description                     |
|:--------------:|:-------------------------------|
|Ce-Id           |An unique number                |
|Ce-Source       |Source of the event (e.g., curl)|
|Ce-Specification|Cloud Event Spec version (=1.0) |
|Ce-Type         |Name of the service             |

An example for posting a Cloud Event using curl command is:
```shell
curl http://<broker-endpoint>:<port>/ \
     -v \
     -X POST \
     -H 'Ce-Id: 1234' \
     -H 'Ce-Source: curl' \
     -H 'Ce-Specification: 1.0' \
     -H 'Ce-Type: thumbnailer' \
     -H 'Content-Type: application/json' \
     -d '{ "input_bucket": "inBucket", "output_bucket": "outBucket", \
           "width": 1920, "height", 1080 }'
```


Note that the curl command just post a Cloud Event to the broker and exit, retruning
HTTP status code `202 Accepted`. (`-v` option tell `curl` to show HTTP status code)
You need to set up a listener of the event retruned
from the service if you want to get the retruned value.