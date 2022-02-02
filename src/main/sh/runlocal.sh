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
echo clock sync


curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "8080", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/clock_synchronization | jq

echo
echo net bench

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "8080", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/network_benchmark | jq

echo
echo serv rep 


curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "8080", "repetitions": "1", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/server_reply | jq

# inference
echo
echo inference benchmarks

echo
echo imagerecognition
# takes input and model as input
# index.png is ok I think
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input": "index.png", "model": "synset.txt"}' -X POST http://localhost:8080/imagerecognition | jq

echo
echo "thumbnailer 210"

# objectkey, width, height
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"objectkey": "index.png", "height": "128", "width": "128"}' -X POST http://localhost:8080/thumbnailer | jq

echo
echo videoprocessing 220

# height, width, , Key, duration, opt -- from param
#  operations = { 'transcode' : transcode_mp3, 'extract-gif' : to_gif, 'watermark' : watermark }
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"key": "Anthem-30-16x9-lowres.mp4", "height": "128", "width": "128", "duration": "1", "op": "extract-gif"}' -X POST http://localhost:8080/videoprocessing | jq


# pagerank
echo
echo pagerank benchmarks

echo
echo pagerank
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/pagerank | jq

echo
echo mst
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/mst | jq

echo
echo bfs
curl -s -w "\n" -H 'Content-Type:application/json' -d '"test"' -X POST http://localhost:8080/bfs | jq

# DNA
echo
echo DNA benchmark

out=$(curl -s --w "\n" -H 'Content-Type:application/json' -d '{"input_key":"bacillus_subtilis.fasta", "output_key":"dna-squiggle.json"}' -X POST http://localhost:8080/dnavis)
echo ${out} | jq
if [[ $? -ne 0 ]]
then
  echo ${out}
fi

# others
echo
echo cloudevent stuff
export URL=http://localhost:8080/cloudeventbenchmark
for TEST in jcompress j110 j120 
do 
  echo $TEST
  export TEST
  curl ${URL} -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"'
  echo
  echo
done
 

# try to clean up
kill -9 $JPID

