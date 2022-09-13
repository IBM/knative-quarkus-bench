# Graph-pagerank Project

This is a Quarkus port of 501.graph-pagerank from
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This application generates an undirected graph of a given number of nodes,
computes PageRank scores of the nodes, and sorts the nodes in descending score order.

The input graph is generated with a Barab&aacute;si-Albert graph generator.
This application uses [JGraphT](https://jgrapht.org/) to generate and process graphs.


## Preparing Input Data

The input to this application is the number of nodes of a generated graph.
No input file is required.


## Building and Running the Application

Build project as described in [this README](../../README.md).

The application can run as a local HTTP server.
To run the stand-alone Java version:
```shell
java -jar target/quarkus-app/quarkus-run.jar
```
To run the stand-alone native version:
```shell
target/graph-pagerank-1.0.0-SNAPSHOT-runner
```


## Sending a Request to the Application

This application receives the following parameters from POST data in JSON format:

|Name         |Value                 |Required?(&starf;)|Default|Default is customizable?|
|:-----------:|:------------------------------------|:-:|:-----:|:----------------------:|
|size         |Number of nodes of a generated graph | N |    10 | N |
|debug        |Flag if sorted scores are printed out| N | false | N |

&starf; Although both `size` and `debug` can be omitted, an object still needs to be sent
as a POST data, e.g., `-d '{}'`.

The `size` parameter can be __an integer__ or __*a data size name*__ as listed below:
|Name  |Number of nodes|
|:----:|:-------------:|
|test  |            10 |
|tiny  |           100 |
|small |         1,000 |
|medium|        10,000 |
|large |       100,000 |


For example:
```shell
curl http://localhost:8080/graph-pagerank \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{"size":"small"}'
```
generates an undirected graph of 1,000 nodes and computes the PageRank scores of the nodes,
but does not return the scores because the `debug` parameter is `false`.

Note that returning the computed scores can take much longer than generating a graph and
computing the scores. The Quarkus runtime serializes the returned scores into JSON, but this is
a time-consuming process.
Therefore, skipping returning the scores is recommended for evaluation of performance.

To send a request to a Knative eventing service,
```shell
curl http://<broker-endpoint>:<port>/ \
     -v \
     -X POST \
     -H 'Ce-Id: 1234' \
     -H 'Ce-Source: curl' \
     -H 'Ce-Specversion: 1.0' \
     -H 'Ce-Type: graph-pagerank' \
     -H 'Content-Type: application/json' \
     -d '{"size":30, "debug":"true"}'
```
This request creates a graph of 30 nodes, computes scores, and returns the sorted scores
as a JSON string because `debug` parameter is set to `true`.


## Customizing the Default Value of Input Parameters

This application takes all input parameters from the POST data, and there are no parameters
that can be customized via environment variables or `application.properties`.
