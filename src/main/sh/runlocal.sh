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

# java -jar line below is for jvm version
# target/*-runner line below is for native version
echo start quarkus
java -jar target/quarkus-app/quarkus-run.jar &
# target/*-runner &
QPID=$!
echo wait for 15 seconds...
sleep 15

# micro benchmarks
echo
echo microbenchmarks

curl -s -w "\n" -H 'Content-Type:application/json'  -X POST http://localhost:8080/Hello | jq

echo

curl -s -w "\n" -H 'Content-Type:application/json' -d '"Samantha"' -X POST http://localhost:8080/hello | jq

echo
echo serv rep 

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "20202", "repetitions": "5", "output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/server_reply | jq

echo
echo net bench

curl -s -w "\n" -H 'Content-Type:application/json' -d '{"request_id": "tmp_key", "server_address": "127.0.0.1", "server_port": "30303", "repetitions": "5", "request_id":"IpsumLoremomethingSayunnySay","output_bucket": "trl-knative-benchmark-bucket", "income_timestamp": "test"}' -X POST http://localhost:8080/network_benchmark | jq


# inference
echo
echo inference benchmarks

echo
echo imagerecognition
# takes input and model as input
curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input": "index.png", "model": "resnet50-19c8e357.pth"}' -X POST http://localhost:8080/imagerecognition | jq
# curl -s -w "\n" -H 'Content-Type:application/json' -d '{"input": "0.png", "model": "mlp-0002.params"}' -X POST http://localhost:8080/imagerecognition | jq


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

echo dynamicHtml
echo

out=$(curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/dynamicHtml)
echo ${out} | jq
if [[ $? -ne 0 ]]
then
  echo ${out}
fi

echo upload
echo
 
out=$(curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/upload)
echo ${out} | jq
if [[ $? -ne 0 ]]
then
  echo ${out}
fi
 
echo compress
echo

out=$(curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/compress)
echo ${out} | jq
if [[ $? -ne 0 ]]
then
  echo ${out}
fi
 
echo download
echo

out=$(curl -s --w "\n" -H 'Content-Type:application/json' -X POST http://localhost:8080/download)
echo ${out} | jq
if [[ $? -ne 0 ]]
then
  echo ${out}
fi
 


# try to clean up
kill -9 $QPID

