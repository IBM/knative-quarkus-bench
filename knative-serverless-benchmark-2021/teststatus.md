## tests and status

| Testname      | Original Pathname (in trl-serverless-benchmarks) |  New Pathname (in knative-serverless-benchmark) | Status |
| -----------   | ----------------- |  ------------ | ------ |
| pcompress | https://github.ibm.com/TRENT/trl-serverless-benchmarks/blob/master/benchmarks/300.utilities/311.compression/python/function.py | https://github.ibm.com/trl-quarkus/knative-serverless-benchmark/blob/master/src/main/py/compressloaddata.py | done|
| jcompress | https://github.ibm.com/TRENT/trl-serverless-benchmarks/blob/master/benchmarks/300.utilities/311.compression/python/function.py | https://github.ibm.com/trl-quarkus/knative-serverless-benchmark/blob/master/src/main/java/com/ibm/trl/funqy/cloudevent/JCompress.java | done |
| p010sleep | benchmarks/000.microbenchmarks/010.sleep/python/function.py | src/main/py/010/p010sleep.py | Thank you Tanabe-san! |
| j010sleep | benchmarks/000.microbenchmarks/010.sleep/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J010sleep.java | Thank you Tanabe-san! |
| p020net   | benchmarks/000.microbenchmarks/020.network-benchmark/python/function.py | src/main/py/020/p020net.py | Thank you Tanabe-san! |
| j020net  | benchmarks/000.microbenchmarks/020.network-benchmark/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J020net.java | Thank you Tanabe-san! |
| p030clock | benchmarks/000.microbenchmarks/030.clock-synchronization/python/function.py | src/main/py/030/p030clock.py | Thank you Tanabe-san! |
| j030clock | benchmarks/000.microbenchmarks/030.clock-synchronization/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J030clock.java | Thank you Tanabe-san! |
| p040serv  | benchmarks/000.microbenchmarks/040.server-reply/python/function.py | src/main/py/040/p040serv.py | Thank you Tanabe-san! |
| j040serv  | benchmarks/000.microbenchmarks/040.server-reply/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J040serv.java | Thank you Tanabe-san! |
| p110html  | benchmarks/100.webapps/110.dynamic-html/python/function.py | src/main/py/110/p110.py | done |
| j110html  | benchmarks/100.webapps/110.dynamic-html/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J110.java | done |
| p120web   | benchmarks/100.webapps/120.uploader/python/function.py | src/main/py/120/p120web.py | done |
| j120web   | benchmarks/100.webapps/120.uploader/python/function.py |src/main/java/com/ibm/trl/funqy/cloudevent/J120web.java | done |
| p210thumb | benchmarks/200.multimedia/210.thumbnailer/python/function.py | src/main/py/210/p210thumb.py | Thank you Horie-san! |
| j210thumb | benchmarks/200.multimedia/210.thumbnailer/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J210thumb.java | Thank you Horie-san! |
| p220vid   | benchmarks/200.multimedia/220.video-processsing/python/function.py | src/main/py/220/p220vid.py | Thank you Horie-san! |
| j220vid   | benchmarks/200.multimedia/220.video-processsing/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J220vid.java | Thank you Horie-san! |
| p411image | benchmarks/400.inference/411.image-recognition/python/function.py | src/main/py/411/p411image.py | Thank you Horie-san! |
| j411image | benchmarks/400.inference/411.image-recognition/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J411image.java | Thank you Horie-san! |
| p501pr    | benchmarks/500.scientific/501.graph-pagerank/python/function.py | src/main/py/501/p501pr.py | Thank you Ogata-san! |
| j501pr    | benchmarks/500.scientific/501.graph-pagerank/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J501pr.java | Thank you Ogata-san! |
| p502mst   | benchmarks/500.scientific/502.graph-mst/python/function.py | src/main/py/502/p502mst.py | Thank you Ogata-san! |
| j502mst   | benchmarks/500.scientific/502.graph-mst/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J502mst.java | Thank you Ogata-san! |
| p503bfs   | benchmarks/500.scientific/503.graph-bfs/python/function.py | src/main/py/503/p503bfs.py | Thank you Ogata-san! |
| j503bfs   | benchmarks/500.scientific/503.graph-bfs/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J503bfs.java | Thank you Ogata-san! |
| p504dna   | benchmarks/500.scientific/504.dna-visualization/python/function.py | src/main/py/504/p504dna.py | Thank you Ogata-san! |
| j504dna   | benchmarks/500.scientific/504.dna-visualization/python/function.py | src/main/java/com/ibm/trl/funqy/cloudevent/J504dna.java | Thank you Ogata-san! |


## References
- [README](README.md)
- [Porting Guide](porting.md)

