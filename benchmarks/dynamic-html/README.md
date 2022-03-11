To build local jar (from top level)
`mvn clean install`

To run locally:
```
java -jar target/quarkus-app/quarkus-run.jar &
curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/dynamicHtml
killall java
```
