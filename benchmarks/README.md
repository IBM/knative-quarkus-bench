# knative-quarkus-bench/benchmarks Module

The following table shows the currently available benchmark programs.
Benchmarks that use object storage require additional configuration
described in [UsingCloudObjectStorage.md](UsingCloudObjectStorage.md).

|Benchmark Name        |Description                                 | Uses Object Storage?  |
|:--------------------:|:-------------------------------------------|:---------------------:|
|clock-synchronization |Measure minimum send and receive time of a datagram socket     | Y |
|compress              |Create a ZIP file of the files in a given directory            | Y |
|dna-visualization     |Visualize DNA data in the Squiggle method                      | Y |
|dynamic-html          |Dynamically generate HTML using `jinjava`                      | N |
|graph-bfs             |Traverse a generated graph in breadth-first order              | N |
|graph-mst             |Compute minimum spanning tree of a generated graph             | N |
|graph-pagerank        |Compute page rank scores of a generated graph                  | N |
|helloworld            |A very simple benchmark that creates a string                  | N |
|image-recognition     |Image classification using a deep learning model from Model Zoo| Y |
|network               |Repeatedly measure send and receive time of a datagram socket  | Y |
|server-reply          |Measure time to read a stream socket                           | N |
|sleep                 |Sleep for a specified number of seconds                        | N |
|thumbnailer           |Resize graphic image data                                      | Y |
|uploader              |Download and upload a file from/to cloud object storage        | Y |
|video-processing      |Transcode, extract gif, and add a watermark to an mp4 movie    | Y |
