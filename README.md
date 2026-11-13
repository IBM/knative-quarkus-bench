# knative-quarkus-bench Project

This project contains a suite of Quarkus programs runnable as both stand-alone
HTTP applications and Knative serverless services.
We ported the individual benchmarks to this environment from those at
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks)
developed by researchers at ETH Z&uuml;rich.

Quarkus is a cloud native Java framework based on modern standard APIs.
See https://quarkus.io/ for more information.

## Prerequisites

* Java 17 or higher (need JDK to build from source)
* Maven 3.6.2+ (3.8.1+ is recommended)
* Docker  (If already using `podman` on RHEL 8 or higher, installing `podman-docker` is a fairly transparent method to provide Docker compatibility.)


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
The version tag is set to `:jvm`, to distinguish
Java images from native images, as described below.

This project uses Quarkus
[container-image-docker extension](https://quarkus.io/guides/container-image#docker)
to build Docker images.  This extension has several configuration parameters to build
images with appropriate tags and to push these images to a container repository.
The configuration parameters can be specified as Maven system parameters.

To use `podman` instead of `docker`,
```shell
mvn package -Dquarkus.docker.executable=podman
```

To prevent Docker images from being built:
```shell
mvn package -Dquarkus.container-image.build=false
```

Other configuration parameters are documented in
[Quarkus documentation](https://quarkus.io/guides/container-image#customizing).


### Creating Native Executables using GraalVM (Optional)

Native binary support is one of the major features of Quarkus.
These benchmarks can be built as native binaries
by adding `-Pnative` to the Maven command line:
```shell
mvn package -Pnative
```

The version tag for native executable images will be set to `:native`.

Building native executables uses
[GraalVM Native Image](https://www.graalvm.org/22.0/reference-manual/native-image/).
If `native-image` is not installed, Quarkus automatically downloads
a container image of the latest GraalVM with Native Image support and builds the project
in the container.  This automated containerized build is beneficial as
it automatically uses the latest version of GraalVM, which is updated frequently.

### Creating Native Executables Using qbicc (Optional)

You can optionally build native binaries using [qbicc](https://github.com/qbicc/qbicc)
by adding `-Pqbicc` to the Maven command line:
```shell
mvn package -Pqbicc
```

In the short term, you will need a local snapshot build of [quarkus-qbicc](https://github.com/qbicc/quarkus-qbicc),
the Quarkus extension for compiling with qbicc.  We have not yet released a version of `quarkus-qbicc` to maven central. 

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

The program is an HTTP server listening to port 8080 that receives and returns JSON objects.
The `curl` command can be used to access the program, specifying the input data as POST data
in JSON format. For example:
```shell
curl http://localhost:8080/pagerank \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{"size":"test"}'
```
The above example computes page rank scores of a generated graph of 10 nodes.
The number of graph nodes can be increased by setting the post data to `tiny`, `small`, `medium` or `large` to set the graph size to `100`, `1,000`, `10,000`, or `100,000` nodes, respectively.


Further details about running and configuring a stand-alone program can be found in the
[Quarkus Funqy HTTP Extension Guide](https://quarkus.io/guides/funqy-http).


### Running in Knative Cloud Environment

#### Prerequisites

Knative needs to be installed in the target Kubernetes/OpenShift environment.
The easiest way to install Knative is to use Kubernetes Operator, as described at
https://knative.dev/docs/install/operator/knative-with-operators/.

A broker needs to be set up in the target namespace.  This is described in the
[Knative documentation](https://knative.dev/docs/eventing/getting-started/#adding-a-broker-to-the-namespace).

A single broker is sufficient in a given namespace regardless of the number of
Knative event services.


#### Preparing Container Images for Uploading to a Container Registry

As described above, this project automatically builds container images by using Quarkus
[container-image-docker extension](https://quarkus.io/guides/container-image#docker).
The host and group names of the image tag can be specified by using
`quarkus.container-image.registry` and `quarkus.container-image.group` configuration
parameters, respectively.

The following example creates an image that will be pushed to the quay.io registry:
```shell
mvn package -Dquarkus.container-image.registry=quay.io -Dquarkus.container-image.group=mygroup
```
The created container image will be tagged as: `quay.io/mygroup/graph-pagerank:jvm`.

To push the built image to the registry use the `deploy` goal and set
`quarkus.container-image.push` to `true`:
```shell
mvn deploy -Dquarkus.container-image.push=true
```


#### Deploying Knative Services

As described in
[the getting started document](https://knative.dev/docs/eventing/getting-started/),
"Service" and "Trigger" resources need to be prepared for each service.
Although this can be deployed manually, the "Deployment" step can be skipped
by utilizing the Knative helper as described in the
[Quarkus Funqy Knative Events extension documentation](https://quarkus.io/guides/funqy-knative-events).

"apiVersion" is "serving.knative.dev/v1" in the Funqy Knative guide, instead of
"v1" as described in the Knative guide.

The sample services in `samples/ksvc/` read the object storage credentials from a
Secret named `cos`, the endpoint and region from a ConfigMap named `cos-endpoint`,
and the bucket names from a ConfigMap named `cos-buckets`. Create the bucket
ConfigMap before deploying the services that use buckets, using only the keys
your chosen services need:
```shell
kubectl create configmap cos-buckets \
  --from-literal=thumbnailerInputBucket=<bucket> --from-literal=thumbnailerOutputBucket=<bucket> \
  --from-literal=videoInputBucket=<bucket>       --from-literal=videoOutputBucket=<bucket> \
  --from-literal=imageRecognitionInputBucket=<bucket> \
  --from-literal=dnaVisInputBucket=<bucket>      --from-literal=dnaVisOutputBucket=<bucket> \
  --from-literal=compressInputBucket=<bucket>    --from-literal=compressOutputBucket=<bucket> \
  --from-literal=uploaderInputBucket=<bucket>    --from-literal=uploaderOutputBucket=<bucket> \
  --from-literal=networkOutputBucket=<bucket>    --from-literal=clockOutputBucket=<bucket>
```
A service whose key is missing from the ConfigMap will not start.


#### Security Considerations

These programs are benchmarks, intended to be deployed temporarily to run an
experiment and then removed. They are **not hardened for production or for
untrusted callers**:

* Bucket names are deploy-time settings. A request cannot choose them, and a
  request that includes `input_bucket` or `output_bucket` is rejected. The
  storage endpoint and credentials are also deploy-time settings.
* Object keys and similar values (such as `size`, `model` and `synset`) still
  come from the caller. The benchmarks confine them to a scratch location
  when building local file paths, but anyone who can send requests can make
  the service read any object in the configured input bucket and, where output
  is enabled, write objects in the configured output bucket.
* The contents of the input bucket are processed as given (for example, media
  files are passed to ffmpeg). Whoever can write to that bucket controls them.
* The `network` and `clock-synchronization` benchmarks take a target
  `server_address` and `server_port` from the request, because they measure
  network behavior against a server you choose. Anyone who can send requests
  can therefore make the service send small UDP packets (carrying the
  request's `request_id`) to any host and port reachable from the pod, and
  have it listen on a UDP port of their choosing. Rely on network policy and
  private access to limit where the pods can reach.
* The numeric parameters (for example `repetitions`, `width`, `height` and
  `duration`) are not capped, so a caller can make a request use a lot of
  time or memory.
* The services have no authentication of their own.

As the deployer, you are responsible for:
* Using dedicated buckets that only trusted parties can write to, and not
  storing other data in them.
* Giving the services storage credentials limited to those buckets.
* Making the services reachable only by the people conducting the experiment
  (for example, use
  [cluster-local visibility](https://knative.dev/docs/serving/services/private-services/)
  for Knative services).
* Removing the deployment when the experiment is finished.

See [benchmarks/UsingCloudObjectStorage.md](benchmarks/UsingCloudObjectStorage.md)
for how to set the buckets.


#### Accessing Knative Services

Knative services receive input as a [Cloud Event](https://cloudevents.io/) object
from the broker and return the result to the broker as a newly created Cloud Event.

The Cloud Event specification defines various HTTP headers starting with "Ce-".
The following table describes the minimum required headers.
|Header        |Description                     |
|:------------:|:-------------------------------|
|Ce-Id         |A unique number                 |
|Ce-Source     |Source of the event (e.g., curl)|
|Ce-Specversion|Cloud Event Spec version (=1.0) |
|Ce-Type       |Name of the service             |

The following is an example of posting a Cloud Event using the `curl` command:
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


The `curl` command simply posts a Cloud Event to the broker and exits, returning the
HTTP status code `202 Accepted`. (The `-v` option tells `curl` to show the HTTP status code.)
In order to receive the returned value, a listener must be configured for the event
returned from the service.
