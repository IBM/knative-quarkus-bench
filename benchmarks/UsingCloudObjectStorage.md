# Guide to Use Cloud Object Storage from the Benchmarks

## Prerequisite

* Access to a cloud object storage that suppots Amazon S3 API, such as Amazon S3,
IBM Cloud Object Storage, or locally running MinIO server
* Input data files appropriate for each benchmark


## Configurations for Cloud Object Storage

Some of the benchmark programs use cloud object storage to store input/output data.
The object storage needs to support Amazon S3 compatible APIs because we use the Quarkus
[Amazon S3 Client extension](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html)
to access object storage.

The minimum required configurations are:
* Specify credentials using two environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
* Specify endpoint URL using an environment variable `QUARKUS_S3_ENDPOINT_OVERRIDE` or
  a system property `quarkus.s3.endpoint-override`

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

The endpoint URL can also be specified in
`benchmarks/<benchName>/src/main/resources/application.property`.
For other configuration parameters, refer to
[the guide document](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-s3.html#_configuring_s3_clients).


## Copying Input Data to the Cloud Object Storage

We used sample input data published by the ETH Z&uuml;rich team for testing many of the benchmarks.
It is available at https://github.com/spcl/serverless-benchmarks-data.git.


## Sending a Request with Specifying Object Storage Parameters

You can specify the bucket names and object keys (= file names) as POST data in JSON format.

For example:
```shell
curl http://localhost:8080/thmbnailer \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{ "input_bucket": "inBucket", \
           "output_bucket": "outBucket", \
           "objectKey": "test.jpg", \
           "width": 300, \
           "height", 200 }'
```
to send a request to a local server, or

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
to send a Knative service.

**Note for Knative services:** the `curl` command simply posts a Cloud Event to the broker and exits,
returning the HTTP status code `202 Accepted`.
(the `-v` option tells `curl` to show the HTTP status code)
You need to set up a listener for the event returned
from the service if you want to receive the returned value.
