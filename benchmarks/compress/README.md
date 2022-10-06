# knative-quarkus-bench compress benchmark

The compress benchmark downloads a directory of files from cloud storage,
compresses the files locally, and then uploads back to cloud storage.

Although details on building and using benchmarks in this project are described in
[the project documentation](../../README.md), and details on using cloud storage are described in
[this file]](../UsingCloudObjectStorage.md), the following show several specific examples
as a brief introduction:

To build a jvm jar file run:
`mvn clean install`

To use the jvm executable locally, first run the application as follows:
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
curl -s --w "\n" -H 'Content-Type:application/json'  -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/compress
```

To build the native application run:
`mvn clean install -Pnative`

To use the native executable locally, first run the application as follows:
```
export AWS_REGION=AppropriateValue
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

target/compress-1.0.0-SNAPSHOT-runner
```
Then in another terminal window:
```
curl -s --w "\n" -H 'Content-Type:application/json'  -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/compress
```
