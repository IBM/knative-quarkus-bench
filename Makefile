ifneq (,$(wildcard ${HOME}/.env))
    include ${HOME}/.env
    export
endif

# make the jvm version
jvm: build image push delete apply

# make the graalvm version
native: buildnative imagenative push delete apply

#
# build the jvm images
build:
	mvn clean install

# build the jvm images with verbose debug output
builddebug:
	mvn clean install -X -e

# build the graalvm images
buildnative:
	mvn clean install -Pnative -Djavax.net.ssl.trustStore=/opt/graalvm-ce-java11-20.3.4/lib/security/cacerts && \
	cp /opt/graalvm-ce-java11-21.3.0/lib/security/cacerts target


# build the graalvm images with verbose debug output
buildnativedebug:
	mvn clean install -Pnative -X -e && \
	cp /opt/graalvm-ce-java11-21.3.0/lib/security/cacerts target

# for jvm images
image:
	podman build -f src/main/docker/Dockerfile.jvm -t us.icr.io/trl-quarkus/knative-serverless-benchmark${IMAGESFX} .

# for native images
imagenative:
	podman build -f src/main/docker/Dockerfile.native -t us.icr.io/trl-quarkus/knative-serverless-benchmark${IMAGESFX} .

# works for both jvm and native
# use below if using docker
# 	ibmcloud cr login | && 

push:
	ibmcloud iam oauth-tokens | sed -ne '/IAM token/s/.* //p' | podman login -u iambearer --password-stdin us.icr.io && \
	podman push us.icr.io/trl-quarkus/knative-serverless-benchmark${IMAGESFX}

# works for both jvm and native
delete:
	oc delete -n ${NS} -f src/main/k8s/funqy-service.yaml || \
	oc delete -n ${NS} -f src/main/k8s/funqy-trigger.yaml || \
	oc delete -n ${NS} -f src/main/k8s/broker.yaml || \
	sleep 1

# works for both jvm and native
apply:
	oc apply -n ${NS} -f src/main/k8s/curler.yaml && \
	oc apply -n ${NS} -f src/main/k8s/broker.yaml && \
	sleep 5 && \
	oc apply -n ${NS} -f src/main/k8s/funqy-service.yaml && \
	oc apply -n ${NS} -f src/main/k8s/funqy-trigger.yaml && \
	sleep 10 

# if you do "make test=nameoftest eventrun" then make will run nameoftest!
eventrun:
	./src/main/sh/runner.sh $(test) $(test)
#
# if you do "make test=nameoftest servicerun" then make will run nameoftest!
servicerun:
	./src/main/sh/svcrunner.sh $(test) $(test)

experiment1:
	./src/main/sh/runner.sh jbdownloadlong1 jdownloadlong 1

experiment2:
	./src/main/sh/tester.sh

measure:
	./src/main/sh/measure.sh

