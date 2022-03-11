#!/bin/bash

if [[ -e ~/.env  ]]
then
  . ~/.env
fi

if [[ "${1}z" == "-?z" || "${1}z" == "--helpz" ]]
then
  echo "usage: ${0} [Test type] [iterations]"
  echo "e.g., ${0} jcompress 8"
  exit 0
fi

if [[ -z "${NS}" ]]
then
  echo "Please set environment variable NS to the namespace."
  exit 1
fi

ITERATIONS="1"
TEST="j110"


if [[ "${1}z" != "z" ]]
then
  TEST=${1}
fi

if [[ "${2}z" != "z" ]]
then
  ITERATIONS=${2}
fi

# The following two URL lines were used for hardcoded verification and can be expected to change
# URL=https://knative-serverless-benchmark-knative-serverless-benchmark.trl-dal-roks-b8ef7649236a07f2b2866d2585e12cb2-0000.us-south.containers.appdomain.cloud/cloudeventbenchmark 
# URL=https://knative-serverless-benchmark-knative-serverless-benchmark.trl-osk-roks-b8ef7649236a07f2b2866d2585e12cb2-0000.jp-osa.containers.appdomain.cloud/cloudeventbenchmark

# This version does a slightly better job of working on different clusters and namespaces.
# With current use "-v" is probably OK. "/cloudeventbenchmark" has a dependency on java code...
URL=$(oc get ksvc | grep " ${NS}-v" | tr -s " " | cut -d" " -f 2)/cloudeventbenchmark


for i in $(seq 1 ${ITERATIONS})
do
  value=$(curl ${URL} -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"')
  problem=$(echo $value | grep -ci "connect error")
  seconds=1
  while [[ "${problem}" -gt 0 ]]
  do
    echo "Trying again in ${seconds} second(s) because output was: ${value}"
    sleep ${seconds}
    value=$(curl ${URL} -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"')
    problem=$(echo $value | grep -ci "connect error")
    seconds=$((seconds*2))
  done
  echo ${TEST},${i},${value}
done



