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


if [[ -f ${HOME}/.env ]]
then
  . ${HOME}/.env
  export COS_ENDPOINT
  export COS_APIKEY
  export COS_ACCESS_KEY_ID
  export COS_SECRET_ACCESS_KEY
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

runsimple ${URL}/Hello

