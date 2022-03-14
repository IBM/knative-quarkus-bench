ifneq (,$(wildcard ${HOME}/.env))
    include ${HOME}/.env
    export
endif

TEST?=compress
TYPE?=jvm
MVNARGS?=

# make the jvm version -- default
jvm: build image push

# make the native version
native: TYPE=native
native: MVNARGS="-Pnative"
native: build image push

#
build:
	mvn clean install $(MVNARGS)


# build the jvm images with verbose debug output
buildjvmdebug:
	mvn clean install -X -e


# build the native images with verbose debug output
buildnativedebug:
	mvn clean install -Pnative -X -e


image:
	podman build -f src/main/docker/Dockerfile.${TYPE} -t us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-${TYPE} .
	
# for local image e.g., for debugging
imagelocal:
	podman build -f src/main/docker/Dockerfile.${TYPE} -t benchlocal-${TEST}-${TYPE} .



# use below if using docker
# 	ibmcloud cr login | && 

push:
	ibmcloud iam oauth-tokens | sed -ne '/IAM token/s/.* //p' | podman login -u iambearer --password-stdin us.icr.io && \
	podman push us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-${TYPE}
