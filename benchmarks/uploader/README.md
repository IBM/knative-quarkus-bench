To build local jar (from top level)
`mvn clean install`

To run locally:
```
export AWS_ENDPOINT=AppropriateValue
export AWS_ACCESS_KEY_ID=AppropriateValue
export AWS_SECRET_ACCESS_KEY=AppropriateValue
export AWS_REGION=AppropriateValue
export COS_IN_BUCKET=AppropriateValue
export COS_OUT_BUCKET=AppropriateValue

java -jar target/quarkus-app/quarkus-run.jar &
curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/uploader
killall java
```

