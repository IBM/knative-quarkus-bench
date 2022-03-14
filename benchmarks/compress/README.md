To build local jvm jar (from top level)
`mvn clean install`

To run locally with jvm version:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export COS_IN_BUCKET=AppropriateValue
export COS_OUT_BUCKET=AppropriateValue

java -jar target/quarkus-app/quarkus-run.jar &

curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/compress
killall java
```

To build the native verison:
`mvn clean install -Pnative`

To run locally with native version:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export COS_IN_BUCKET=AppropriateValue
export COS_OUT_BUCKET=AppropriateValue

target/compress-1.0.0-SNAPSHOT-runner: &

curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/compress
killall compress-1.0.0-SNAPSHOT-runner
```
