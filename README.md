# knative-quarkus-bench Project

This project contains a suite of Quarkus programs runnable as both stand-alone
HTTP applications and Knative serverless services.
We ported the individual benchmarks to this environment from those at
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks)
developed by researchers at ETH Z&uuml;rich.

Quarkus is a cloud native Java framework based on modern standard APIs.
More information about these advanced features is available at https://quarkus.io/.

## Prerequisite

* Java 11 or higher (need JDK to build from source)
* Maven 3.6.2+ (3.8.1+ is recommended)
* Docker


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
curl http://localhost:8080/pagerank \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{"size":"test"}'
```
computes page rank scores of a generated graph of 10 nodes.
You can increase the number of graph nodes by setting the post data to `tiny`, `small`, `medium` or `large` to set the graph size to `100`, `1,000`, `10,000`, or `100,000` nodes, respectively.


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


#### Preparing Container Images for Uploading to a Container Registry

As described above, this project automatically build container image by using Quarkus
[container-image-docker extension](https://quarkus.io/guides/container-image#docker).
You can specify the host and group names of the image tag by using
`quarkus.container-image.registry` and `quarkus.container-image.group` configuration
parameters, respectively.

Following example creates an image that will be pushed to the quay.io registry:
```shell
mvn package -Dquarkus.container-image.registry=quay.io -Dquarkus.container-image.group=mygroup
```
The created container image will be tagged as: `quay.io/mygroup/graph-pagerank:jvm`.

You can push the built image to the registry by using `deploy` goal with setting
`quarkus.container-image.push` to `true`:
```shell
mvn deploy -Dquarkus.container-image.push=true
```


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
|Header        |Description                     |
|:------------:|:-------------------------------|
|Ce-Id         |An unique number                |
|Ce-Source     |Source of the event (e.g., curl)|
|Ce-Specversion|Cloud Event Spec version (=1.0) |
|Ce-Type       |Name of the service             |

An example for posting a Cloud Event using the `curl` command is:
```shell
curl http://<broker-endpoint>:<port>/ \
     -v \
     -X POST \
     -H 'Ce-Id: 1234' \
     -H 'Ce-Source: curl' \
     -H 'Ce-Specversion: 1.0' \
     -H 'Ce-Type: pagerank' \
     -H 'Content-Type: application/json' \
     -d '{"size":"test"}'
```


Note that the `curl` command simply posts a Cloud Event to the broker and exits, returning the
HTTP status code `202 Accepted`. (the `-v` option tells `curl` to show the HTTP status code)
You need to set up a listener for the event returned
from the service if you want to receive the returned value.
