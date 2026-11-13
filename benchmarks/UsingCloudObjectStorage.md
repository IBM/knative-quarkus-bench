# Using Cloud Object Storage in this Project

## Prerequisites

* Access to a cloud object storage that supports Amazon S3 API, such as Amazon S3,
IBM Cloud Object Storage, or local MinIO server
* Data files appropriate for each benchmark


## Configurations for Cloud Object Storage

Some of the benchmark programs use cloud object storage to store input/output data.
The object storage needs to support Amazon S3 compatible APIs as this project uses the Quarkus
[Amazon S3 Client extension](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html).

The minimum required configurations are:
* Specify credentials using two environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
* Specify endpoint URL using an environment variable `QUARKUS_S3_ENDPOINT_OVERRIDE` or
  the system property `quarkus.s3.endpoint-override`

To run the stand-alone Java version:
```shell
export AWS_ACCESS_KEY_ID=<KeyID>
export AWS_SECRET_ACCESS_KEY=<AccessKey>
export QUARKUS_S3_ENDPOINT_OVERRIDE=<EndpointURL>
java -jar benchmarks/<benchName>/target/quarkus-app/quarkus-run.jar
```

To run the stand-alone native version:
```shell
export AWS_ACCESS_KEY_ID=<KeyID>
export AWS_SECRET_ACCESS_KEY=<AccessKey>
export QUARKUS_S3_ENDPOINT_OVERRIDE=<EndpointURL>
benchmarks/<benchName>/target/<benchName>-1.0.0-SNAPSHOT-runner
```

Likewise, these environment variables will need to be set as part of a container runtime environment.

The endpoint URL can also be specified in
`benchmarks/<benchName>/src/main/resources/application.property`.
For other configuration parameters, refer to
[quarkus-amazon-services documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html#_configuring_s3_clients).


## Copying Input Data to Cloud Object Storage

We used sample input data published by the ETH Z&uuml;rich team to test many of the benchmarks.
These are available at https://github.com/spcl/serverless-benchmarks-data.git.


## Specifying Bucket Names (Deploy-Time Settings)

Bucket names are **deploy-time settings**. They are not accepted in a request:
a request that includes `input_bucket` or `output_bucket` is rejected.
Only object keys (AKA file names) are specified as POST data in JSON format.

Set the bucket names with environment variables or with
`benchmarks/<benchName>/src/main/resources/application.properties`.
The environment variable name is the property name in upper case with `.` and `-` replaced by `_`:

|Benchmark          |Environment Variables                                                                       |
|:------------------|:-------------------------------------------------------------------------------------------|
|thumbnailer        |`KNATIVEBENCH_THUMBNAILER_INPUT_BUCKET`, `KNATIVEBENCH_THUMBNAILER_OUTPUT_BUCKET`           |
|video-processing   |`KNATIVEBENCH_VIDEO_PROCESSING_INPUT_BUCKET`, `KNATIVEBENCH_VIDEO_PROCESSING_OUTPUT_BUCKET` |
|image-recognition  |`KNATIVEBENCH_IMAGE_RECOGNITION_INPUT_BUCKET`                                               |
|dna-visualization  |`KNATIVEBENCH_DNA_VIS_INPUT_BUCKET`, `KNATIVEBENCH_DNA_VIS_OUTPUT_BUCKET`                   |
|compress           |`KNATIVEBENCH_COMPRESS_INPUT_BUCKET`, `KNATIVEBENCH_COMPRESS_OUTPUT_BUCKET`                 |
|uploader           |`KNATIVEBENCH_UPLOADER_INPUT_BUCKET`, `KNATIVEBENCH_UPLOADER_OUTPUT_BUCKET`                 |
|network            |`KNATIVEBENCH_NETWORK_OUTPUT_BUCKET`                                                        |
|clock-synchronization|`KNATIVEBENCH_CLOCK_SYNCHRONIZATION_OUTPUT_BUCKET`                                        |

For example, to run the stand-alone Java version of thumbnailer:
```shell
export KNATIVEBENCH_THUMBNAILER_INPUT_BUCKET=inBucket
export KNATIVEBENCH_THUMBNAILER_OUTPUT_BUCKET=outBucket
java -jar benchmarks/thumbnailer/target/quarkus-app/quarkus-run.jar
```

For a Knative service, set the same variables in the container's `env` section.

**Deployer responsibilities.** Because the service reads from and writes to the configured buckets
using the credentials you give it:
* Use dedicated buckets for the benchmark, and do not store other data in them.
* Allow only trusted parties to write to the input bucket. Whoever can write there controls
  the files the benchmark processes (including media files passed to ffmpeg).
* Use credentials that are limited to those buckets.
* Keep the service endpoint private (see the Security Considerations section of the top-level README).

## Specifying Object Keys within a Request

For example, to send a request to a local server:
```shell
curl http://localhost:8080/thumbnailer \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{ "objectKey": "test.jpg", \
           "width": 300, \
           "height", 200 }'
```

To send a request to a Knative service:

```shell
curl http://<broker-endpoint>:<port>/ \
     -v \
     -X POST \
     -H 'Ce-Id: 1234' \
     -H 'Ce-Source: curl' \
     -H 'Ce-Specversion: 1.0' \
     -H 'Ce-Type: thumbnailer' \
     -H 'Content-Type: application/json' \
     -d '{ "objectKey": "test.jpg", \
           "width": 300, \
           "height", 200 }'
```

**Note for Knative services:** The `curl` command simply posts a Cloud Event to the broker and exits,
returning the HTTP status code `202 Accepted`.
(The `-v` option tells `curl` to show the HTTP status code.)
A listener must be configured for the event returned
from the service in order to receive the returned value.
