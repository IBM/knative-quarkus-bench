ifneq (,$(wildcard ${HOME}/.env))
    include ${HOME}/.env
    export
endif

TEST?=uploader

# make the jvm version
jvm: TYPE=jvm
jvm: MVNARGS=
jvm: build image push

# make the native version
native: TYPE=native
native: MVNARGS="-Pnative"
native: build image push

#
# build the jvm images
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
	
# for local jvm image e.g., for debugging
imagelocaljvm:
	podman build -f src/main/docker/Dockerfile.jvm -t benchlocal-${TEST}-jvm .


# use below if using docker
# 	ibmcloud cr login | && 

push:
	ibmcloud iam oauth-tokens | sed -ne '/IAM token/s/.* //p' | podman login -u iambearer --password-stdin us.icr.io && \
	podman push us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-${TYPE}
