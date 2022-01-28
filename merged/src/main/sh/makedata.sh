#!/bin/bash

BASE=/tmp/knative-tmp

rm -rf ${BASE}

echo writing sample data to directories under ${BASE}

L=40000
SD=small
D=${BASE}/${SD}
mkdir -p ${D}
yes "The answer is yes..." | head -${L} >${D}/yes.txt
yes "The answer is no!!!!" | head -${L} >${D}/no.txt
yes "The answer is maybe." | head -${L} >${D}/maybe.txt

L=400000
SD=medium
D=${BASE}/${SD}
mkdir -p ${D}
yes "Well, the answer is still yes..." | head -${L} >${D}/yes.txt
yes "Well, the answer is still no!!!!" | head -${L} >${D}/no.txt
yes "Well, the answer is still maybe." | head -${L} >${D}/maybe.txt

L=4000000
SD=large
D=${BASE}/${SD}
mkdir -p ${D}
yes "Well, as I mentioned before, the answer is yes..." | head -${L} >${D}/yes.txt
yes "Well, as I mentioned before, the answer is no!!!!" | head -${L} >${D}/no.txt
yes "Well, as I mentioned before, the answer is maybe." | head -${L} >${D}/maybe.txt

