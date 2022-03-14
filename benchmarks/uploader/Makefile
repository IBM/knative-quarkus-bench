ifneq (,$(wildcard ${HOME}/.env))
    include ${HOME}/.env
    export
endif

TEST=uploader

# make the jvm version
jvm: buildjvm imagejvm pushjvm

# make the graalvm version
native: buildnative imagenative pushnative

#
# build the jvm images
buildjvm:
	mvn clean install


# build the jvm images with verbose debug output
buildjvmdebug:
	mvn clean install -X -e


# build the graalvm images
buildnative:
	mvn clean install -Pnative



# build the graalvm images with verbose debug output
buildnativedebug:
	mvn clean install -Pnative -X -e


# for jvm images
imagejvm:
	podman build -f src/main/docker/Dockerfile.jvm -t us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-jvm .
	
# for local jvm image e.g., for debugging
imagelocaljvm:
	podman build -f src/main/docker/Dockerfile.jvm -t benchlocal-${TEST}-jvm .


# for native images
imagenative:
	podman build -f src/main/docker/Dockerfile.native -t us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-native .

# use below if using docker
# 	ibmcloud cr login | && 

pushjvm:
	ibmcloud iam oauth-tokens | sed -ne '/IAM token/s/.* //p' | podman login -u iambearer --password-stdin us.icr.io && \
	podman push us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-jvm

pushnative:
	ibmcloud iam oauth-tokens | sed -ne '/IAM token/s/.* //p' | podman login -u iambearer --password-stdin us.icr.io && \
	podman push us.icr.io/trl-quarkus/knative-serverless-benchmark-${TEST}-native

