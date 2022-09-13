To build a local jvm jar:
`mvn clean install`

To run a local jvm jar:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

java -jar target/quarkus-app/quarkus-run.jar &
curl -s --w "\n" -H 'Content-Type:application/json'  -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/uploader
killall java
```

To build a native image:
`mvn clean install -Pnative`

To run the local native image:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export QUARKUS_S3_ENDPOINT_OVERRIDE=AppropriateValue

target/uploader-1.0.0-SNAPSHOT-runner: &

curl -s --w "\n" -H 'Content-Type:application/json' -d '{"size":"somevalue", "input_bucket":"somevalue", "output_bucket":"somevalue"}' -X POST http://localhost:8080/uploader
killall uploader-1.0.0-SNAPSHOT-runner
```

