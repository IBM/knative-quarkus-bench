# knative-quarkus-bench uploader benchmark

The uploader benchmark uploads files to cloud storage.

Although details on building and using benchmarks in this project are described in
[the project documentation](../../README.md), and details on using cloud storage are described in
[this file](../UsingCloudObjectStorage.md), the following show several specific examples
as a brief introduction:

To build a jvm jar file:
`mvn clean install`

To use the jvm jar locally, run the application as follows:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

java -jar target/quarkus-app/quarkus-run.jar
```
Then in another terminal window:
```
curl -s --w "\n" -H 'Content-Type:application/json'  -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/uploader
```

To build a native application:
`mvn clean install -Pnative`

To run the native application locally:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

target/uploader-1.0.0-SNAPSHOT-runner
```
Then in another terminal window:
```
curl -s --w "\n" -H 'Content-Type:application/json' -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/uploader
```

