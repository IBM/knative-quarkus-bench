To build local a jvm jar file run:
`mvn clean install`

To run locally with `jvm`:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

java -jar target/quarkus-app/quarkus-run.jar &

curl -s --w "\n" -H 'Content-Type:application/json'  -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/compress
killall java
```

To build the native image run:
`mvn clean install -Pnative`

To run the local local native image:
```
export AWS_REGION=AppropriateValue
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

target/compress-1.0.0-SNAPSHOT-runner: &

curl -s --w "\n" -H 'Content-Type:application/json'  -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/compress
killall compress-1.0.0-SNAPSHOT-runner
```
