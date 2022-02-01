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

URL=$(oc get ksvc -n ${NS} | grep " ${NS}-v" | tr -s " " | cut -d" " -f 2)

echo $URL

### THIS WORKS!! (but sometimes randomly fails)
# echo simple hello - without jq
# curl -s -w "\n" -H 'Content-Type:application/json' -X POST ${URL}/Hello

# echo hello with headers - no jq
# error
# knative-serverless-benchmark-v1-deployment-5c5c957dc5-74dsc user-container 2022-02-01 09:11:20,561 ERROR [io.qua.funqy] (executor-thread-0) Couldn't map CloudEvent type: 'null' to a function.
# curl -s -w '\n' -H 'Ce-Id:1234' -H 'Ce-Specversion:1.0' -H 'Ce-Source:curl' -H 'Content-Type:application/json'  -X POST ${URL}/Hello

# echo hello with ce type
# knative-serverless-benchmark-v1-deployment-5c5c957dc5-crbb8 user-container 2022-02-01 09:12:45,869 ERROR [io.qua.ver.htt.run.QuarkusErrorHandler] (executor-thread-0) HTTP Request to /Hello failed, error id: 1cd078b6-f0b4-47b1-8c97-868f2390c211-1: java.io.UncheckedIOException: com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
# knative-serverless-benchmark-v1-deployment-5c5c957dc5-crbb8 user-container  at [Source: (byte[])""; line: 1, column: 0]

# curl -s -w '\n' -H 'Ce-Id:1234' -H 'Ce-Specversion:1.0' -H 'Ce-Type:cloudeventbenchmark' -H 'Ce-Source:curl' -H 'Content-Type:application/json'  -X POST ${URL}/Hello | jq

# FOLLOWING DOES WORK!!!!!
# echo samatha 1
# curl -s -w "\n" -H 'Content-Type:application/json' -d '"Samantha"' -X POST ${URL}/hello | jq

### FOLLOWING WORKS!
# echo j110
# export TEST=j110
# curl ${URL}/cloudeventbenchmark -s -X POST -H "Ce-Id:1234" -H "Ce-Specversion:1.0" -H "Ce-Type:cloudeventbenchmark" -H "Ce-Source:curl" -H "Content-Type:application/json" -d '"'${TEST}'"'

# micro benchmarks
echo
echo microbenchmarks

curl -s -w "\n" -H 'Content-Type:application/json'  -X POST ${URL}/Hello | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '"Samantha"' -X POST ${URL}/hello | jq

echo
echo clock sync


curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "8080", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST ${URL}/clock_synchronization | jq

echo
echo net bench

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "0.0.0.0", "server_port": "8080", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST ${URL}/network_benchmark | jq

echo
echo serv rep 


curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "8080", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST ${URL}/server_reply | jq

# inference
echo
echo inference benchmarks

echo
echo imagerecognition
# takes input and model as input
# index.png is ok I think
# QUESTION: model?
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input": "index.png", "model": ""}' -X POST ${URL}/imagerecognition | jq

echo
echo "thumbnailer 210"
# objectkey, width, height
# Question: is object key OK?
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"objectkey": "220/in/processed-city.gif", "height": "128", "width: "128"}' -X POST ${URL}/thumbnailer | jq

echo
echo videoprocessing 220
# height, width, , Key, duration, opt -- from param
#  operations = { 'transcode' : transcode_mp3, 'extract-gif' : to_gif, 'watermark' : watermark }

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"key": "220/in/processed-city.gif", "height": "128", "width: "128", "duration": "5", "op": "watermark"}' -X POST ${URL}/videoprocessing | jq

# pagerank
echo
echo pagerank benchmarks

echo
echo pagerank
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST ${URL}/pagerank | jq

echo
echo mst
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST ${URL}/mst | jq

echo
echo bfs
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST ${URL}/bfs | jq

# DNA
echo
echo DNA benchmarks

# bucketname, inputkey,outputkey can be set by param. All have defaults...
# curl -s -X POST ${URL}/dna | jq
curl -s --w "\n" -H 'Content-Type:application/json' -X POST ${URL}/dna | jq

curl -s --w "\n" -H 'Content-Type:application/json' -d '{"input";"GCF_000772165.1_ASM77216v1_genomic.fna", "output":"dna-squiggle.json"}' -X POST ${URL}/dna | jq


# others
echo
echo cloudevent stuff
for TEST in jcompress j110 j120 
do 
  echo $TEST
  export TEST
  curl ${URL}/cloudeventbenchmark -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"'
  echo
  echo
done
 

