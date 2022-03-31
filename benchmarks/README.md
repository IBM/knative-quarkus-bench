# knative-quarkus-bench/benchmarks Module

This is a placeholder of all benchmark programs in this project.

Following table shows a list of avialable benchmark programs.
Note that the benchmarks that uses object storage needs additional configuration
described in [UsingCloudObjectStorage.md](UsingCloudObjectStorage.md).

|Benchmark Name        |Description                                 | Use Object Storage? |
|:--------------------:|:-------------------------------------------|:-------------------:|
|clock-synchronization |Measure minimum send and receive time of a datagram socket    | Y |
|compress              |Create a ZIP file of the files in a given directory           | Y |
|dna-visualization     |Vizualize DNA data in the Squiggle method                     | Y |
|dynamic-html          |Dynamicall generate HTML using jinjava                        | N |
|graph-bfs             |Traverse a generated graph in the breadth-first order         | N |
|graph-mst             |Compute minimum spanning tree of a generated graph            | N |
|graph-pagerank        |Compute page rank scores of a generated graph                 | N |
|image-recognition     |Image classification using a deep larning model from Model Zoo| Y |
|network               |Repeatedly measure send and receive time of a datagram socket | Y |
|server-reply          |Measure time to read a stream socket                          | N |
|sleep                 |Sleep for a specified seconds                                 | N |
|thumbnailer           |Resize graphic image data                                     | Y |
|uploader              |Download and upload a file from/to cloud object storage       | Y |
|video-processing      |Transcode, extract gif, and add a wartermark to an mp4 movie  | Y |
