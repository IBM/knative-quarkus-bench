#!/bin/bash

if [[ -f ${HOME}/.env ]]
then
  . ${HOME}/.env
  export COS_ENDPOINT
  export COS_APIKEY
  export COS_INSTANCE_CRN
  export COS_IN_BUCKET
  export COS_OUT_BUCKET
  export NS
  export IMAGESFX
fi

cd $(dirname $0)/../../..

echo start quarkus
java -jar target/quarkus-app/quarkus-run.jar &
JPID=$!
echo wait for 30 seconds...

sleep 30

# micro benchmarks
echo
echo microbenchmarks

curl -s -w "\n" -H 'Content-Type:application/json'  -X POST http://localhost:8080/Hello | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '"Samantha"' -X POST http://localhost:8080/hello | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "0.0.0.0", "server_port": "80", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/network_benchmark | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "0.0.0.0", "server_port": "80", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/clock_synchronization | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "0.0.0.0", "server_port": "80", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/server_reply | jq


# inference
echo
echo inference benchmarks

# need key and model from param
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input": "tmp_key", "model": "test"}' -X POST http://localhost:8080/imagerecognition | jq

echo

# height, width, objectKey, Key, duration, opt -- from param?
#  operations = { 'transcode' : transcode_mp3, 'extract-gif' : to_gif, 'watermark' : watermark }
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"key": "tmp_key", "height": "128", "width: "128", "duration": "10", "op": "watermark"}' -X POST http://localhost:8080/videoprocessing | jq



# pagerank
echo
echo pagerank benchmarks

curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/pagerank | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/mst | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/bfs | jq


# DNA
echo
echo DNA benchmarks

# bucketname, inputkey,outputkey can be set by param. All have defaults...
curl -s -X POST http://localhost:8080/dna | jq
curl -s --w "\n" -H 'Content-Type:application/json' -d '""' -X POST http://localhost:8080/dna | jq

# others
echo
echo cloudevent stuff
export URL=http://localhost:8080/cloudeventbenchmark
for TEST in jcompress jdownloadlong j110 j120 
  do 
  echo $TEST
  export TEST
  curl ${URL} -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"'
  echo
  echo
  done
 

# try to clean up
kill -9 $JPID

