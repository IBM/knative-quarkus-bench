# knative-quarkus-bench dynamic-html benchmark

The dynamic-html benchmark uses the [jinjava library](https://github.com/HubSpot/jinjava)
to dynamically create html using a template and generated data.

Although details on building and using benchmarks in this project are described in
[the project documentation](../../README.md), the following show several specific examples
as a brief introduction:

To build a jvm jar:
`mvn clean install`

To run this `jvm` jar locally, first start the application:
```
java -jar target/quarkus-app/quarkus-run.jar
```
Then in another terminal window:
```
curl -s --w "\n" -H 'Content-Type:application/json' -d '{"size":"small"}' -X POST http://localhost:8080/dynamic-html
```

To build a native executable:
`mvn clean install -Pnative`

To run this native executable, first start the application:
```
target/dynamic-html-1.0.0-SNAPSHOT-runner
```
Then in another terminal window:
```
curl -s --w "\n" -H 'Content-Type:application/json' -d '{"size":"small"}' -X POST http://localhost:8080/dynamic-html
```

This benchmark supports the following load sizes:
| size label | random integers added |
|:----------:|:---------------------:|
| test       | 10        |
| tiny       | 100       |
| small      | 1000      |
| medium     | 10000     |
| large      | 100000    |
| huge       | 1000000   |
| massive    | 10000000  |

