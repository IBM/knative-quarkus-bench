# Clock-synchronization Project

This is a Quarkus port of 030.clock-synchronization from [SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This benchmark measures minimum send and receive time of a datagram socket.

## Optional preparation step

This benchmark attempts to upload a result file via Cloud Object Storage (COS) unless the `debug` parameter is set to be `true` (See [example](#sample-curl-command)). 

Refer to [cloud Storage Configuration](../UsingCloudObjectStorage.md) to enable upload of result files to Cloud Storage.

## Building and Running the Application

Build the project as described in [this README](../../README.md).

Then, the application can run as a local HTTP server.
To run the stand-alone Java version:
```shell
java -jar target/quarkus-app/quarkus-run.jar
```
To run the stand-alone native version:
```shell
target/imae-recognition-1.0.0-SNAPSHOT-runner
```

This application receives the following parameters from POST data in JSON format:

|Name         |Value                         |Required?|Default|
|:---------------:|:-----------------------------------------------|:-:|:-----:|
|output_bucket    |COS bucket to download input files              |Y  |(None) |
|server_address   |Server's IP address                             |Y  |(None) |
|server_pot       |Server's port number                            |Y  |(None) |
|repetitions      |Number of repetition to finish earlier          |N  |1      |
|request_id       |any string that will be included in the message |N  |"test" |
|debug            |Flag if output is uploaded to COS               |N  |false  |

For example:

```shell
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"output_bucket":"MyOutputBucket", "server_address": "127.0.0.1", "server_port": "20202"}' -X POST http://localhost:8080/clock-synchronization | jq
```
The result looks like this:
```
{
  "result": "clock-synchronization-benchmark-results-tmp_key.csv"
}
```
