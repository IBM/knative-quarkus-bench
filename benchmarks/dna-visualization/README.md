# Dna-visualization Project

This is a Quarkus port of 504.dna-visualization from
[SeBS: Serverless Benchmark Suite](https://github.com/spcl/serverless-benchmarks).

This application reads a DNA sequence from an input file and transforms it to a sequence of
two-dimensional vectors using
[the Squiggle method](https://squiggle.readthedocs.io/en/latest/methods.html).

Note that the input any character other than `A`, `T`, `C`, `G` is treated as invalid and is
translated to a horizontal segment (i.e., a sequence of two `(0.5, 0)` vectors.)


## Preparing Input Data

This application reads a DNA sequence file in FASTA format as input.
This program uses [JFASTA](http://jfasta.sourceforge.net/) to read a DNA sequence from
a FASTA file, so the file format should be acceptable by JFASTA.

The input files we tested are:
* [The sample FASTA file published by EHT Z&uuml;rich team](https://github.com/spcl/serverless-benchmarks-data/blob/6a17a460f289e166abb47ea6298fb939e80e8beb/500.scientific/504.dna-visualisation/bacillus_subtilis.fasta)
* [DNA sequence of Methanocaldococcus jannaschii](https://ftp.ncbi.nlm.nih.gov/genomes/all/GCA/000/091/665/GCA_000091665.1_ASM9166v1/GCA_000091665.1_ASM9166v1_genomic.fna.gz)
  (ungzip after download)

The input file is assumed to be stored in Cloud Object Storage (COS).
COS environment variable configuration is described in
[benchmarks/UsingCloudObjectStorage.md](../UsingCloudObjectStorage.md).

(FYI) Methanocaldococcus jannaschii is the first archaeon to be genome sequenced completely.
Its data size is less than a half of the sample data published by ETH Z&uuml;rich team.

## Building and Running the Application

Build the project as described in [this README](../../README.md).

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

This application receives the following parameters from POST data in JSON format:

|Name         |Value                         |Required?|Default|Default is customizable?|
|:-----------:|:------------------------------------|:-:|:------|:----------------------:|
|input_bucket |COS bucket to download input files     |Y|(None) |Y|
|output_bucket|COS bucket to upload output files      |Y|(None) |Y|
|key          |COS object key of the input/output file|Y|(None) |Y|
|debug        |Flag if output is uploaded to COS      |N|false  |N|
For example:
```shell
curl http://localhost:8080/dna-visualization \
     -X POST \
     -H 'Content-Type: application/json' \
     -d '{"input_bucket":"MyInputBucket", \
          "output_bucket":"MyOutputBucket", \
          "key":"FASTAfiles/NewSequence.fasta"}
```
downloads a FASTA file `FASTAfiles/NewSequence.fasta` from a COS bucket `MyInputBucket`,
transforms the DNA sequence to a two-dimensional plot using the Squiggle method, but
does not upload the output to a COS bucket.

Note that uploading a file to COS can take much longer time than downloading a file and
transforming the DNA sequence. Therefore, skipping uploading the file is recommended for
evaluation of performance.

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
     -d '{"input_bucket":"MyInputBucket", \
          "output_bucket":"MyOutputBucket", \
          "key":"FASTAfiles/NewSequence.fasta", \
          "debug":"true"}'
```
This request downloads a FASTA file `FASTAfiles/NewSequence.fasta` from a COS bucket
`MyInputBucket`, transforms the DNA sequence to a two-dimensional plot using the Squiggle method,
and uploads the output to a COS bucket `MyOutputBucket` with the same object key because
`debug` parameter is set to `true`.

## Customizing the Default Value of Input Parameters

The default values of the three parameters can be customized by using environment variables
or by modifying `resources/application.properties`.  The following table describes this.
|Name         |Environment Variable              |Key in `application.properties`   |
|:-----------:|:---------------------------------|:---------------------------------|
|input_bucket |KNATIVEBENCH_DNA_VIS_INPUT_BUCKET |knativebench.dna-vis.input_bucket |
|output_bucket|KNATIVEBENCH_DNA_VIS_OUTPUT_BUCKET|knativebench.dna-vis.output_bucket|
|key          |KNATIVEBENCH_DNA_VIS_KEY          |knativebench.dna-vis.key          |
