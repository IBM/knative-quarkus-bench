# Set up tips for knative quarkus bench


## Installation Tips
- Centos 7, RHEL 8, and Ubuntu have succesfully been used to run these benchmarks.
- Ensure that local machine has access to a cluster
  - Install ibmcloud cli, oc command, mvn, docker or podman, and python3.
  - Install a recent version of GraalVM. It can be useful to also install native-image: ``gu install native-image``
- Install knative support on cluster
    - Install the serverless operator, knative serving component, and the knative eventing component.
        - Reference: https://docs.openshift.com/container-platform/4.7/serverless/install/install-serverless-operator.html

## Related Guides

- Funqy Knative Events Binding for Quarkus Funqy framework -- https://quarkus.io/guides/funqy-knative-events
- knative tutorial --  https://redhat-developer-demos.github.io/knative-tutorial
- Red Hat serverless documentation -- https://access.redhat.com/documentation/en-us/openshift_container_platform/4.7/html-single/serverless/index#serverless-getting-started
- GraalVM Documentation -- https://www.graalvm.org

