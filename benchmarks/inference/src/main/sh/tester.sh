#!/bin/bash

if [[ -e ~/.env  ]]
then
  . ~/.env
fi

if [[ -e ~/.env  ]]
then
  . ~/.env-exp
fi

if [[ -z "${NS}" ]]
then
  echo "Please set environment variable NS to the namespace."
  exit 1
fi

ITERATIONS="10"
# ITERATIONS="1"

CSVFILE=allout.csv
LOGFILE=allout.log

rm -rf ${LOGFILE}
rm -rf ${CSVFILE}


# URL=https://knative-serverless-benchmark-knative-serverless-benchmark.trl-dal-roks-b8ef7649236a07f2b2866d2585e12cb2-0000.us-south.containers.appdomain.cloud/cloudeventbenchmark 

# This version does a slightly better job of working on different clusters and namespaces.
# With current use "-v" is probably OK. "/cloudeventbenchmark" has a dependency on java code...
URL=$(oc get ksvc | grep " ${NS}-v" | tr -s " " | cut -d" " -f 2)/cloudeventbenchmark




for TEST in pcompress p110 p120 jcompress j110 j120 jlargepagerank jlargemst jlargebfs
do
  for i in $(seq 1 ${ITERATIONS})
  do
    value=$(/usr/bin/time --format='%e' -o time.txt curl ${URL} -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"')
    problem=$(echo $value | grep -ci "connect error")
    seconds=1
    while [[ "${problem}" -gt 0 ]] 
    do
      echo "output was: ${value}"
      echo "Trying again in "${seconds}" second(s)."
      sleep ${seconds}
      value=$(/usr/bin/time --format='%e' -o time.txt curl ${URL} -s -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"')
      problem=$(echo $value | grep -ci "connect error")
      seconds=$((seconds*2))
    done
    echo ${TEST},${i},${value},$(cat time.txt)
    echo ${value},$(cat time.txt) >> ${CSVFILE}
  done
done




