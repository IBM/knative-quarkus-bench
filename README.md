# knative-quarkus-bench Project

This project contains a suite of Quarkus programs runnable as both stand-alone
HTTP applications and Knative serverless services.
We ported the individual benchmarks to this environment from those at
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks)
developed by researchers at ETH Z&uuml;rich.

Quarkus is a cloud native Java framework based on modern standard APIs.
More information about these advanced features is available at https://quarkus.io/.

## Build Instructions

### Clone Git Repo
```shell
https://github.com/IBM/knative-quarkus-bench.git
```

### Building the Application
```shell
cd knative-quarkus-bench
mvn package
```

This step builds all benchmark programs and creates Docker images for each.
Note that the version tag is set to `:jvm`, so that you can distinguish
Java images from native images, as described below.

This project uses Quarkus
[container-image-docker extension](https://quarkus.io/guides/container-image#docker)
to build Docker images.  This extension has several configuration parameters to build
images with appropriate tags and to push these images to a container repository.
The configuration parameters can be specified as Maven system parameters.

To create an image that will be pushed to the quay.io repository,
```shell
mvn package -Dquarkus.container-image.resistry=quay.io -Dquarkus.container-image.group=mygroup
```
This will create container images with tags such as: `quay.io/mygroup/thumbnailer:jvm`.

If you use `podman` instead of `docker`,
```shell
mvn package -Dquarkus.docker.executable=podman
```

If you don't need to build Docker images, you can disable it with:
```shell
mvn package -Dquarkus.container-image.build=false
```

For other configuration parameters, refer to
[the guide document](https://quarkus.io/guides/container-image#customizing).


### Creating Native Executables (Optional)

One of the major features of Quarkus is native binary support.
You can build these microservice benchmarks as native binaries
by adding `-Pnative` in the Maven command line:
```shell
mvn package -Pnative
```

The version tag for native executable images is set to `:native`.

Building native executables uses
[GraalVM Native Image](https://www.graalvm.org/22.0/reference-manual/native-image/).
If `native-image` isn't set up in your environment, Quarkus automatically downloads
a container image of the latest GraalVM with Native Image support and builds the project
in the container.  We recommend this automated containerized build
because it automatically uses the latest version of GraalVM, which is updated frequently.


## Usage Instructions

### Setting Up Cloud Object Storage

Some of the benchmark programs use cloud object storage to store input/output data.
The object storage needs to support Amazon S3 compatible APIs because we use the Quarkus
[Amazon S3 Client extension](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html)
to access object storage.

The minimum required configurations are:
1. Specify endpoint URL and region name in `src/main/resources/application.property`
under each submodule,
1. Specify credentials using two environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`

For other configuration parameters, refer to
[the guide document](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html#_configuring_s3_clients).

We tested our programs using sample input data published by the ETH Z&uuml;rich team
at https://github.com/spcl/serverless-benchmarks-data.git.


### Running as a Stand-alone Program

Each submodule creates a runnable JAR file or an executable binary.

To run the stand-alone Java version:
```shell
java -jar benchmarks/<benchname>/target/quarkus-app/quarkus-run.jar
```
To run the stand-alone native version:
```shell
benchmarks/<benchname>/target/<benchname>-1.0.0-SNAPSHOT-runner
```

The program works as an HTTP server listening to port 8080 and receiving/replying JSON objects.
You can use `curl` command to access the program, specifying the input data as POST data
in JSON format. For example:
```shell
curl http://localhost:8080/thmbnailer \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{ "input_bucket": "inBucket", "output_bucket": "outBucket", \
           "width": 1920, "height", 1080 }'
```


For more detail about running and configuring a stand-alone program, refer to the guide for
[Quarkus Funqy HTTP extension](https://quarkus.io/guides/funqy-http).


### Running in Knative Cloud Environment

#### Prerequisites

Knative needs to be installed in your Kubernetes/OpenShift environment.
If not, the easiest way to install Knative is to use Kubernetes Operator, as described at
https://knative.dev/docs/install/operator/knative-with-operators/.

A broker needs to be set up in your namespace.  This is described in
[this document](https://knative.dev/docs/eventing/getting-started/#adding-a-broker-to-the-namespace).

Note that only a single broker is needed in a namespace regardless of the number of
Knative event services.


#### Deploying Knative Services

As described in
[the getting started document](https://knative.dev/docs/eventing/getting-started/),
you need to prepare "Service" and "Trigger" resources for each service.
Although this can be deployed manually, you can skip "Deployment"
by utilizing the Knative helper feature as described in the
[Quarkus Funqy Knative Events extension](https://quarkus.io/guides/funqy-knative-events) documentation.

Note that "apiVersion" is "serving.knative.dev/v1" in the Funqy Knative guide, instead of
"v1" as described in the Knative guide.


#### Accessing Knative Services

Knative services receive input as a [Cloud Event](https://cloudevents.io/) object
from the broker and return the result back to the broker as a newly created Cloud Event.

The Cloud Event specification defines various HTTP headers starting with "Ce-".
The following table describes the minimum required headers.
|Header          |Description                     |
|:--------------:|:-------------------------------|
|Ce-Id           |An unique number                |
|Ce-Source       |Source of the event (e.g., curl)|
|Ce-Specification|Cloud Event Spec version (=1.0) |
|Ce-Type         |Name of the service             |

An example for posting a Cloud Event using the `curl` command is:
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


Note that the `curl` command simply posts a Cloud Event to the broker and exits, returning the
HTTP status code `202 Accepted`. (the `-v` option tells `curl` to show the HTTP status code)
You need to set up a listener for the event returned
from the service if you want to receive the returned value.
