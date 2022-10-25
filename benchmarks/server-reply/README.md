# Server-reply Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

To learn more about Quarkus, please visit  https://quarkus.io/.

## Packaging and running the application

The application can be packaged using:
```shell script
mvn -Dskiptests clean package
```
This creates `quarkus-run.jar` in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is runnable using:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

The server listens to `localhost:8080`, and functions are accessible at `/<functionName>` path. 
Functions with parameters only accept POST requests. Functions without parameters accept both GET and POST requests.

The `/server-reply` function receives a test data size as a string, and returns the result in JSON format:
```
{
  "result": {
    "size": "1024",
    "result": ""
  },
  "measurement": {
    "process_time": 0.002436617
  }
}
```
Valid choices of the test data size are `test`, `small`, and `large`, where the graph sizes are set to `1`, `100`, and `1,000`, respectively.

Be careful with quotation marks. In Funqy, post data needs to be JSON format. So, a string value in post data needs to have double quotation marks (`"`)
around the data. You may also need another quotation mark or back-slash (`\`) to avoid shell command line interpretation.

### Building an über-jar
To build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

To build a native executable: 
```shell script
mvn package -Pnative
```

If GraalVM is not installed, the native executable can be built in a container: 
```shell script
mvn package -Pnative -Dquarkus.native.container-build=true
```

To execute the native image: `./target/server-reply-1.0.0-SNAPSHOT-runner`

To learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.


### Sample curl commands for testing
```
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"server_address": "127.0.0.1", "server_port": "20202"}' -X POST http://localhost:8080/server-reply  | jq
```
