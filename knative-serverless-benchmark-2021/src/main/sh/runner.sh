#!/bin/bash

if [[ -e ~/.env  ]]
then 
  . ~/.env
fi

if [[ "${1}z" == "-?z" || "${1}z" == "--helpz" ]]
then
  echo "usage: ${0} [Output Base Name] [Test type] [iterations]"
  echo "e.g., ${0} nj8 jcompress 8"
  exit 0
fi

if [[ -z "${NS}" ]]
then
  echo "Please set environment variable NS to the namespace."
  exit 1
fi


# NS=knative-serverless-benchmark
APP="knative-serverless-benchmark-v1-deployment"

ITERATIONS="1"
# TEST="jcompress"
TEST="j110"

count=$(oc get pods -n ${NS} | grep -w curler | wc -l)
if [[ ${count} -ne 1 ]]
then
  echo "exactly 1 instance of curler pod should be running."
  exit
fi

# verify that previous pods are terminated
count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
if [[ ${count} -ne 0 ]] 
then
    echo "Waiting for previous pods to terminate."
fi

while [[ ${count} -gt 0 ]]
do
  sleep 10
  count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
  echo -n "."
done

# possibly verify that curler is running

CSVFILE=benchout.csv
csvoutput=false

if [[ "${1}z" != "z" ]]
then
  CSVFILE=${1}.csv
fi

if [[ "${2}z" != "z" ]]
then
  TEST=${2}
fi

if [[ "${3}z" != "z" ]]
then
  ITERATIONS=${3}
fi

# obtain log file

LOGFILE=${CSVFILE%.csv}.log
rm -rf ${LOGFILE}

# kubectl -n ${NS} exec curler -- /usr/bin/curl -v "http://broker-ingress.knative-eventing.svc.cluster.local/knative-serverless-benchmark/knative-serverless-benchmark" -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"one"'

# kubectl -n ${NS} exec curler -- /usr/bin/curl -v "http://broker-ingress.knative-eventing.svc.cluster.local/knative-serverless-benchmark/knative-serverless-benchmark" -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"pcompress"'

# kubectl -n ${NS} exec curler -- /usr/bin/curl -v "http://broker-ingress.knative-eventing.svc.cluster.local/knative-serverless-benchmark/knative-serverless-benchmark" -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"jcompress"'

date > curlout.txt
for i in $(seq 1 ${ITERATIONS})
do
  kubectl -n ${NS} exec curler -- /usr/bin/curl -v "http://broker-ingress.knative-eventing.svc.cluster.local/knative-serverless-benchmark/knative-serverless-benchmark" -X POST -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" -H "Ce-Type: cloudeventbenchmark" -H "Ce-Source: curl" -H "Content-Type: application/json" -d '"'${TEST}'"' >>curlout.txt
done



# sleep 10

# then monitor output from newly created pod(s)

# testing for overandout will not work well with multiple pods....

# oc logs -n ${NS} -f deployment/knative-serverless-benchmark-v1-deployment -c user-container --all-containers --pod-running-timeout=1m | while read a
stern knative-serverless-benchmark-v1-deployment -n ${NS} -c user-container --color never | while read a
do
  echo ${a}
  echo ${a} >> ${LOGFILE}
  if [[ "$a" == *###* ]]
  then
    if [[ "${csvoutput}" == "false" ]]
    then
      csvoutput=true
      rm -rf ${CSVFILE}
    fi
#    echo ${a##*###} >> ${CSVFILE}
#    echo ${a/ ###/, } | >> ${CSVFILE}
    part2=$(echo ${a##*###}) >> ${CSVFILE}
    part1=$(echo ${a} | cut -d" " -f1)
    echo ${part1}", "${part2} >> ${CSVFILE}
  fi
  count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
  if [[ ${count} -eq 0 ]]
  then
    echo all application pods terminated.
    exit 0
  fi
 
done

if [[ "${csvoutput}" == "true" ]]
then
  echo "CSV file placed in ${CSVFILE}."
fi
