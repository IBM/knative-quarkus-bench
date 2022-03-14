To build local jvm jar (from top level)
`mvn clean install`

To run local jvm jar:
```
java -jar target/quarkus-app/quarkus-run.jar &
curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/dynamicHtml
killall java
```

To build the native verison:
`mvn clean install -Pnative`

To run locally with native version:
```
target/compress-1.0.0-SNAPSHOT-dynamic-html: &

curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/dynamicHtml
killall compress-1.0.0-SNAPSHOT-dynamic-html
```

