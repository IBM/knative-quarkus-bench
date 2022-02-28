#!/bin/bash


runsimple() {
  url=${1}
  value=$(curl -s -w '\n' -H 'Content-Type:application/json'  -X POST ${url})
  problem=$(echo $value | grep -ci "connect error")
  seconds=1
  while [[ "${problem}" -gt 0 ]]
  do
    if [[ ${seconds} -gt 16 ]]
    then
      echo "Too many failures. Skipping to next test."
      return
    fi
    echo "Trying again in ${seconds} second(s) because output was: ${value}" >&2
    sleep ${seconds}
    value=$(curl -s -w '\n' -H 'Content-Type:application/json'  -X POST ${url})
    problem=$(echo $value | grep -ci "connect error")
    seconds=$((seconds*2))
  done
  attempt=$(echo ${value} | jq  2>/dev/null)
  if [[ $? -eq 0 ]]
  then
    echo ${value} | jq
    # yes, that was ugly
  else
    echo ${value}
  fi
}


# pro tip: spaces in the parameter json are problematic. remove them.

runone() {
  url=${1}
  p=${2}

  value=$(curl -s -w '\n' -H 'Content-Type:application/json' --data-binary ${p} -X POST ${url})
  problem=$(echo $value | grep -ci "connect error")
  seconds=1
  while [[ "${problem}" -gt 0 || "${value}z" == "z" ]]
  do
    if [[ ${seconds} -gt 16 ]]
    then
      echo "Too many failures. Skipping to next test."
      return
    fi
    echo "Trying again in ${seconds} second(s) because output was: ${value}" >&2
    sleep ${seconds}
    value=$(curl -s -w '\n' -H 'Content-Type:application/json' --data-binary ${p}  -X POST ${url})
    problem=$(echo $value | grep -ci "connect error")
    seconds=$((seconds*2))
  done
  echo p = $p

  attempt=$(echo ${value} | jq  2>/dev/null)
  if [[ $? -eq 0 ]]
  then
    echo ${value} | jq
    # yes, that was ugly
  else
    echo ${value}
  fi
}


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

# cd $(dirname $0)/../../..

URL=$(oc get ksvc -n ${NS} | grep " ${NS}-v" | tr -s " " | cut -d" " -f 2)

# tip: try running the following command in a separate window for detailed troubleshooting info:
#  stern knative-serverless-benchmark-v1-deployment -n ${NS} -c user-container --color never

echo "Base URL:"
echo $URL

# micro benchmarks
echo
echo microbenchmarks

# curl -s -w '\n' -H 'Content-Type:application/json'  -X POST ${URL}/Hello

runsimple ${URL}/Hello

echo samantha

runone ${URL}/hello '"Samantha"'

echo
echo serv rep 

runone ${URL}/server_reply '{"request_id":"tmp_key","server_address":"127.0.0.1","server_port":"20202","repetitions":"10","output_bucket":"trl-knative-benchmark-bucket","income_timestamp":"test"}'

echo
echo net bench

runone ${URL}/network_benchmark '{"request_id":"tmp_key","server_address":"127.0.0.1","server_port":"20202","repetitions":"10","request_id":"IpsumLoremomethingSayunnySay","output_bucket":"trl-knative-benchmark-bucket","income_timestamp":"test"}'



# inference
echo
echo inference benchmarks

echo
echo imagerecognition 
# takes input and model as input
runone ${URL}/imagerecognition '{"input":"0.png","model":"mlp-0002.params"}'
# runone ${URL}/imagerecognition '{"input":"index.png","model":"resnet50-19c8e357.pth"}'

echo
echo "thumbnailer 210"
# objectkey, width, height
runone ${URL}/thumbnailer '{"objectkey":"index.png","height":"128","width":"128"}'


echo
echo videoprocessing 220
# height, width, , Key, duration, opt -- from param
#  operations = { 'transcode' : transcode_mp3, 'extract-gif' : to_gif, 'watermark' : watermark }
runone ${URL}/videoprocessing '{"key":"Anthem-30-16x9-lowres.mp4","height":"128","width":"128","duration":"1","op":"extract-gif"}'


# pagerank
echo
echo pagerank benchmarks

echo
echo pagerank
runone ${URL}/pagerank '"test"'

echo
echo mst
runone ${URL}/mst '"test"'

echo
echo bfs
runone ${URL}/bfs '"test"'

# DNA
echo
echo DNA benchmark

runone ${URL}/dnavis '{"input_key":"bacillus_subtilis.fasta","output_key":"dna-squiggle.json"}'


echo dynamicHtml
echo

runsimple ${URL}/dynamicHtml

echo upload
echo

runsimple ${URL}/upload
 
echo compress
echo

runsimple ${URL}/compress
 
echo download
echo

runsimple ${URL}/download

