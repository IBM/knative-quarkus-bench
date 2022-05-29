# Dna-visualization Project

This project is a Quarkus port of 504.dna-visualization of
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This application reads a DNA sequnece from an input file and transform it to a sequence of
two dimension vectors using
[the Squiggle method](https://squiggle.readthedocs.io/en/latest/methods.html).

Note that the input any charactor other than `A`, `T`, `C`, `G` is treated as invalid and
translated to a horizontal segment (i.e., a sequence of two `(0.5, 0)` vectors.)


## Preparing Input Data

This application reads a DNA sequence file in FASTA format as an input.
This program uses [JFASTA](http://jfasta.sourceforge.net/) to read a DNA sequence from
a FASTA file, so the file format should be acceptable by JFASTA.

The input files we tested are:
* [The sample FASTA file published by EHT Z&uuml;rich team](https://github.com/spcl/serverless-benchmarks-data/blob/6a17a460f289e166abb47ea6298fb939e80e8beb/500.scientific/504.dna-visualisation/bacillus_subtilis.fasta)
* Euryarchaeotoes DNA sequence

The input file is assumed to be stored in an cloud object storage (COS).
You need to set up environment variables as described in
[UsingCloudObjectStorage.md](../UsingCloudObjectStorage.md).


## Building and Running the Application

Build project as described in [the README at the top level](../../README.md) if it has not.

Then, the application can run as a local HTTP server.
To run the stand-alone Java version:
```shell
java -jar target/quarkus-app/quarkus-run.jar
```
To run the stand-alone native version:
```shell
target/dna-visualization-1.0.0-SNAPSHOT-runner
```


## Sending a Request to the Application

This application receives following parameters from POST data in JSON format:

|Name         |Value                         |Required?|Default|Default is customizable?|
|:-----------:|:-----------------------------------|:-:|:------|:----------------------:|
|input_bucket |COS bucket to download input files    |Y|(None) |Y|
|output_bucket|COS bucket to upload output files     |Y|(None) |Y|
|key          |COS objet key of the input/output file|Y|(None) |Y|
|debug        |Flag if output is uploaded to COS     |N|false  |N|

For example:
```shell
curl http://localhost:8080/dna-visualization \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{"input_buket":"MyInputBucket", \
          "output_buket":"MyOutputBucket", \
          "key":"FASTAfiles/NewSequence.fasta", \
	  "debug":"true"}'
```
downloads a FASTA file `FASTAfiles/NewSequence.fasta` from a COS bucket `MyInputBucket`,
transforms the DNA sequence to a two-demensional plot using Squiggle method, and
uploads the output to a COS bucket `MyOutputBucket` with the same object key.

Note that uploading a file to COS can take much longer time than downloading a file and
transforming the DNA sequence. Therefore, skiping uploading the file is recommended for
evaluation of transforming performance.

To send a request to a Knative eventing service,
```shell
curl http://<broker-endpoint>:<port>/ \
     -v \
     -X POST \
     -H 'Ce-Id: 1234' \
     -H 'Ce-Source: curl' \
     -H 'Ce-Specversion: 1.0' \
     -H 'Ce-Type: dna-visualization' \
     -H 'Content-Type: application/json' \
     -d '{"input_buket":"MyInputBucket", \
          "output_buket":"MyOutputBucket", \
          "key":"FASTAfiles/NewSequence.fasta", \
	  "debug":"true"}'
```


## Customizing the Default Value of Input Parameters

The default values of the three parameters can be customized by using environment variables
or by modifying `resources/application.properties`.  Folloing table describes their names
to change the defaults:
|Name         |Environment Variable              |Key in `application.properties`   |
|:-----------:|:---------------------------------|:---------------------------------|
|input_bucket |KNATIVEBENCH_DNA_VIS_INPUT_BUCKET |knativebench.dna-vis.input_bucket |
|output_bucket|KNATIVEBENCH_DNA_VIS_OUTPUT_BUCKET|knativebench.dna-vis.output_bucket|
|key          |KNATIVEBENCH_DNA_VIS_KEY          |knativebench.dna-vis.key          |
