#!/bin/bash

# LABEL=nativetest
LABEL=basetest

# assuming that main/src/sh/ireport.sh is installed at /root/bin/report.sh on all worker nodes.
# ireport.sh is a slightly hacked version of https://github.ibm.com/TYOS/cpe/blob/v2.pin/perf/report.sh

# also this requres use of introspect tools:
# https://github.ibm.com/CognitiveAdvisor/hcbench/tree/master/tools/inspector

# advice:
# yum install kernel-debug kernel-debug-devel

findnode () {
  oc get pods -n kube-system --no-headers -o name | grep pod/inspectnode-trl- | while read worker
  do
    NN=$(oc get ${worker} -n kube-system --no-headers -o custom-columns=nodeName:spec.nodeName)
    if [[ ${NN} == ${1} ]]
      then
      echo ${worker}
      break
      fi
  done
}

REPCOUNT=0
oc get pods -n knative-serverless-benchmark --no-headers -o name | grep knative-serverless-benchmark-v1-deployment- | while read podname
do
  echo looking at ${podname}
  podnode=$(oc get ${podname} -n knative-serverless-benchmark --no-headers -o custom-columns=nodeName:spec.nodeName)
  echo "running on ${podnode}"
  worker=$(findnode ${podnode})
  echo worker node is ${worker}
  appcount=$(oc exec ${worker} -n kube-system -- ssh localhost ps -eaf | egrep './application|java' | grep quarkus | wc -l )
  if [[ ${appcount} -gt 0 ]]
  then
    echo Found application. count=${appcount}
    apppid=$(oc exec ${worker} -n kube-system -- ssh localhost ps -eaf | egrep './application|java' | grep quarkus | tr -s " " | cut -d" " -f 2 | head -n 1)
    echo looking at pid ${apppid}. First collect perf.data for 60 seconds.
    if [[ z != "${apppid}z" ]]
      then
      oc exec ${worker} -n kube-system -- ssh localhost sysctl kernel.perf_event_paranoid=0
      oc exec ${worker} -n kube-system -- ssh localhost perf record -g -F 100 -c 6000 -p ${apppid} -o /root/perf.data sleep 60
      oc exec ${worker} -n kube-system -- ssh localhost sysctl kernel.perf_event_paranoid=2
      REPCOUNT=$((REPCOUNT+1))
      oc exec ${worker} -n kube-system -- ssh localhost /root/bin/ireport.sh ${apppid} /root/perf.data | tee perfreport${LABEL}-${REPCOUNT}.txt
      fi
  fi
  echo
done
