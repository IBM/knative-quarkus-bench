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


## Specifying Object Storage Parameters within a Request

Bucket names and object keys (AKA file names) are specified as POST data in JSON format.

For example, to send a request to a local server:
```shell
curl http://localhost:8080/thumbnailer \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{ "input_bucket": "inBucket", \
           "output_bucket": "outBucket", \
           "objectKey": "test.jpg", \
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
     -H 'Ce-Specification: 1.0' \
     -H 'Ce-Type: thumbnailer' \
     -H 'Content-Type: application/json' \
     -d '{ "input_bucket": "inBucket", \
           "output_bucket": "outBucket", \
           "objectKey": "test.jpg", \
           "width": 300, \
           "height", 200 }'
```

**Note for Knative services:** The `curl` command simply posts a Cloud Event to the broker and exits,
returning the HTTP status code `202 Accepted`.
(The `-v` option tells `curl` to show the HTTP status code.)
A listener must be configured for the event returned
from the service in order to receive the returned value.
