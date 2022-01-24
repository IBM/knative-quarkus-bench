#!/bin/bash

if [[ -e ~/.env  ]]
then
  . ~/.env
fi

if [[ -z "${NS}" ]]
then
  echo "Please set environment variable NS to the namespace."
  exit 1
fi

APP="knative-serverless-benchmark-v1-deployment"

while [[ true ]]
do
  # verify that previous pods are terminated
  count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
  while [[ ${count} -gt 0 ]]
  do
    sleep 10
    count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
    echo -n "."
  done
  sleep 5
  # just be sure!!!!!
  count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
  while [[ ${count} -gt 0 ]]
  do
    sleep 10
    count=$(oc get pods -n ${NS} | grep ${APP} | wc -l)
    echo -n "."
  done

  killall stern
  echo
  echo "stern slayed at " $(date)
  sleep 30  # should be enough time for next test to start

  # just in case, wait if make is running
  count=$(ps -eaf | grep make | grep -v grep | wc -l)  
  if [[ ${count} -gt 0 ]]
  then
    echo "Waiting for make to finish."
  fi
  while [[ ${count} -gt 0 ]]
    do
    sleep 30
    echo -n "-"
    count=$(ps -eaf | grep make | grep -v grep | wc -l)  
    if [[ ${count} -eq 0 ]]
      then
      sleep 180 ## let things progress
      fi
    done
done

