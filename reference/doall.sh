#!/bin/bash

# SEQ="1 2 4 8 16"
SEQ="1"

TESTS="jsmallcompress psmallcompress"
# TESTS="jmediumcompress pmediumcompress"

make 

if  [[ ${?} -eq 0 ]]
then
  for i in ${SEQ}
    do
    for t in ${TESTS}
      do
      ./runner.sh b${t:0:1}${i} ${t} ${i}
      done
    done

  echo

  make native
  if  [[ ${?} -eq 0 ]]
  then
    for i in ${SEQ}
      do
      for t in ${TESTS}
        do
        ./runner.sh n${t:0:1}${i} ${t} ${i}
        done
      done
  fi ## if native compiled
fi ## if base compiled

