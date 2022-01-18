# Porting steps

## Source of original benchmark tests: (This is a static clone of the original)
https://github.ibm.com/TRENT/trl-serverless-benchmarks
  - specifically under directory ./benchmark/*
  
## Destination benchmark repo:
https://github.ibm.com/TRENT/knative-serverless-benchmark
  - specifically
    - java under: ./src/main/java/com/ibm/trl/funqy/cloudevent
    - python under: ./src/main/py

## Porting Steps: (WORK IN PROGRESS :-)
- See [Porting/test Status file](teststatus.md) for suggestions on file locations, COS buckets, & other names. Please update as necessary.
- Identify test and files to port (see trl-serverless-benchmarks/benchmark/*
- Port python to ./src/main/py/*.py
  - This should be easy. May not be. 
  - Print test output in csv format to stdout.
  - Possibly copy ./src/main/py/0000.py as a starting point. But,
    see ./src/main/py/compresstest.py for sample of actual working test that uses cloud storage.
  - Might be easiest to hardcode COS bucket names for each set of tests... (See teststatus.md file)
  - There may be some kind of data set up required. May need to create files in cloud storage.
  - pom.xml needs a `<fileSet>` stanza to copy the new `*.py` file into `target/scripts`. (search pom.xml for compresstest.py for an example.)
- Port Java to ./src/main/java/...../funqy/cloudevent/*
  - Create new class file. Return csv string.
    - Possibly copy src/main/java...funqy/cloudevent/J0000.java as starting point, but,
      see ./src/main/java.../funqy/cloudevent/JCompress.java as a sample of a working test that uses cloud storage.
  - Add hook to CloudEventBenchmark.java switch statement. (Until we find a better way...)
    - Look for "0000" switch statement, and copy for your test. ("jpagerank" case shows how to link to an external class.)
- Build JVM case with `make` and `make native ` for GraalVM native build.(Needs local environment setup... I am working on simplifying this... )
- To test, modify src/main/sh/runner.sh so that TEST="yourtestname", and run it after building. (e.g., `make run`)

## Porting tips
- Good to include java libraries in pom.xml
- Framework uses python3. If new python libraries are needed, we need to update docker build files under src/main/docker/*.
- GraalVM native java code may need lots of tender loving care with files under `src/main/resources`. Especially `dynamic-proxy-config.json` and `reflection-config.json`
  - Google search is very useful here. Documentation for GraalVM might help a bit: https://www.graalvm.org/reference-manual/native-image/Reflection/
- Many important points are included in the [readme file](README.md).

# to document
  - more on porting steps
  - document how to run locally...
