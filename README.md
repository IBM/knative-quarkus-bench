# knative-serverless-benchmark Project

# TRL knative-serverless-benchmark instructions

## First Time Cluster Installation Tips
- Assuming local machine has access to IBM Cloud cluster, ibmcloud cli (including cr) , oc command, mvn, docker or podman, python3, and stern. (Developed on CentOS 7.)
  - For centos 7 use yum to install glib-static and libstdc++-static.
  - Install GraalVM. Be sure to install native-image: ``gu install native-image``
    - For CentOS 7, GraalVM CE Version 20 is recommended.
    - For RHEL 7, GraalVM CE Version 21 is recommended.
  - Install python cloud object storage library: `pip3 install ibm-cos-sdk`
- Create IBM Cloud storage. See notes below.
- Install knative support on cluster
    - Follow these instructions (if you are using openshift 4.8. Version 4.6 functionality verified at one point.)
    - Install the serverless operator:
      - https://docs.openshift.com/container-platform/4.8/serverless/admin_guide/install-serverless-operator.html
    - Install knative serving
      - https://docs.openshift.com/container-platform/4.8/serverless/admin_guide/installing-knative-serving.html#installing-knative-serving
    - Install knative eventing
      - https://docs.openshift.com/container-platform/4.8/serverless/admin_guide/installing-knative-eventing.html#installing-knative-eventing

Cluster configuration
```shell script
export NS="yournamespace" # e.g., export NS=knative-serverless-benchmark
ibmcloud login -sso
(and oc cli login command to enable oc/kubectl cli)
ibmcloud target -g hpc # or other group as necessary
ibmcloud cr namespace-add trl-quarkus
ibmcloud cr region # us south seems to work. DO NOT USE GLOBAL!!!
ibmcloud cr login
oc new-project ${NS}
oc policy add-role-to-user system:image-builder $(oc whoami)
oc policy add-role-to-user edit $(oc whoami)
oc adm policy add-cluster-role-to-user cluster-admin $(oc whoami)
oc get secret all-icr-io --namespace=default -o yaml | sed -e 's/namespace: .*/namespace: '${NS}'/' -e 's/"namespace":".*"/"namespace":"'${NS}'"/' | oc --namespace=${NS} apply -f -
```
First build and run the images for the base JVM version.
```shell script
cd git/knative-serverless-benchmark
make jvm
make run
```
Control-c to terminate runner.sh.
 

## Repeat Build Tips
First, perform login if not already done:
```shell script
ibmcloud login -sso
(and oc cli login command to enable oc/kubectl)
ibmcloud cr login
```
Then just use `make jvm` and `make run`.

Note that when images are changed, cluster objects using those images need to be deleted and recreated to reflect changes.
This is done automatically in the Makefile.

## Using Native (GraalVM)
Simply run `make native` and then use `make run` like with the base JVM case.

## Test Run Tip
You can specify a specific test to run with the make command: e.g., `make test=jtestpagerank run`

## IBM Cloud Storage
- Getting started with IBM Cloud Object Storage -- https://cloud.ibm.com/docs/cloud-object-storage?topic=cloud-object-storage-getting-started-cloud-object-storage
  - Need to set up access to two buckets and obtain api key access information...
  - Currently using trl-knative-benchmark-bucket-1 and trl-knative-benchmark-bucket-2.
- IBM COS SDK for Python Documentation -- https://ibm.github.io/ibm-cos-sdk-python/index.html
- IBM COS SDK Python code examples -- https://cloud.ibm.com/docs/cloud-object-storage?topic=cloud-object-storage-python
- IBM COS SDK Documentation for Java -- https://cloud.ibm.com/docs/cloud-object-storage?topic=cloud-object-storage-java
- Set up authentication secrets
  - To run on local machine set environment variables in `/home/${USER}/.env` (Possibly good to also create `${HOME}/.bluemix/cos_credentials`...)
  - Environment variables are:
    - COS_ENDPOINT       Default is "https://s3.direct.us-south.cloud-object-storage.appdomain.cloud".
    - COS_APIKEY	 "apikey" field from .bluemix/cos_credentials
    - COS_INSTANCE_CRN   "resource_instance_id" field from .bluemix/cos_credentiuals
    - COS_IN_BUCKET      Default is "trl-knative-benchmark-bucket-1".
    - COS_OUT_BUCKET     Default is "trl-knative-benchmark-bucket-2".
    - NS                 Desired namespace. (e.g., knative-serverless-benchmark)
    - IMAGESFX           An optional suffix to add to image name. Used for parallel development/execution/etc.

  - To run on cluster copy src/main/k8s/cos-secrets-template.yaml to src/main/k8s/cos-secrets-actual.yaml, edit it to contain actual values (encoded in base64!), then run "oc apply -f src/main/k8s/cos-secrets-actual.yaml"
    - Tip: This is a good command to convert to base64: "echo -n somevalue | base64"
- Sample data can be created with src/main/sh/makedata.sh, and then copied to cloud storage with src/main/sh/compressloaddata.py.

- At the moment, it is easiest for each developer to use their own cluster.
- Additionally, using unique containter images is required to develop and run multiple images simultaneously (e.g., multiple teammates working in parallel) set IMAGESFX in `/home/${USER}/.env` to some unique string such as `-osaka`, `-smith`, `-mytest123`, etc. Then edit `src/main/k8s/funqy-service.yaml` to point to the same image. e.g., `knative-serverless-benchmark-osaka`


## Related Guides

- Funqy Knative Events Binding for Quarkus Funqy framework -- https://quarkus.io/guides/funqy-knative-events
- knative tutorial --  https://redhat-developer-demos.github.io/knative-tutorial
- Red Hat serverless documentation -- https://access.redhat.com/documentation/en-us/openshift_container_platform/4.6/html-single/serverless/index#serverless-getting-started
- GraalVM Documentation for native images work -- https://www.graalvm.org

## Shell scripts
- src/main/sh/entrypoint.sh -- Not used anymore. Used to be base case container entry point.
- src/main/sh/ireport.sh -- Run on container. Displays perf report.
- src/main/sh/makedata.sh -- Creates temporary data to load to cloud storage.
- src/main/sh/measure.sh -- Performs perf record and perf report.
- src/main/sh/runner.sh -- Runs tests on cluster.
- src/main/sh/slayer.sh -- Terminates stern command as necessary.

## Other local documentation
- [Porting Guide](porting.md)
- [Porting/test Status file](teststatus.md)

## ongoing todo items include
- much better documentation
- scafolding for easier collabvoration
- make paths in teststatus.md clickable...
- replace massive switch statement in CloudEventBenchmark.java with
  individual functions that use @Funq("Testname") specification. Replace
    string argument with a test modifier?
- write/convert other benchmarks
- efficient output and visualziation of results -- thoughts...
  - (Actually looks like this is being replaced with json...)
  - tests return single line of csv. harness outputs "###<<<CSVLINE>>>"
  - When tests over outputs a single line: "%%%OVERANDOUT%%%"
  - include test name? header...
- much better isolation to allow multiple developers at same time
- eliminate the massive switch statement
- allow for an optional argument with "testname:argument" parameter syntax in addition to "testname".
- handle json output for both python and java.....
- perform performance measurement
- update this todo list
- document ffpmeg requirements
- document possible pytorch requirements




## Quarkus Boilerplate documentation:

This section written by Quarkus and included for historical purposes...

This project uses Quarkus, website: https://quarkus.io/ .

### Running the application in dev mode

You can run your application in dev mode that enables live coding: `./mvnw compile quarkus:dev`

> Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

### Packaging and running the application

The application can be packaged with: `./mvnw package`

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
It is not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

To build an _über-jar_, execute: `./mvnw package -Dquarkus.package.type=uber-jar`

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

### Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
`./mvnw package -Pnative -Dquarkus.native.container-build=true`

To learn more about building native executables, consult https://quarkus.io/guides/maven-tooling.html.
