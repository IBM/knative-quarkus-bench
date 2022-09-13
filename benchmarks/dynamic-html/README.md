To build local jvm jar:
`mvn clean install`

To run the local `jvm` jar:
```
java -jar target/quarkus-app/quarkus-run.jar &
curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/dynamicHtml
killall java
```

To build a native image:
`mvn clean install -Pnative`

To run a local native image:
```
target/dynamic-html-1.0.0-SNAPSHOT-runner &

curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/dynamicHtml
killall dynamic-html-1.0.0-SNAPSHOT-runner
```

