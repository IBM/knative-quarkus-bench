package com.ibm.trl.funqy.cloudevent;

import io.quarkus.funqy.Funq;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import com.ibm.trl.funqy.scientific.SeBS_Graph;

public class CloudEventBenchmark {
	private static final Logger log = Logger.getLogger(CloudEventBenchmark.class);
	private RunPython runner = new RunPython();

	// experimental at the moment. does not work.
	@Funq("jcompress")
	public String jcompress(String argument) {
		String retval="";
		try {
			retval = jRunner("JCompress",new JCompress(),"large");
		} catch (Exception e) {
			log.info(retval = "Error with object construction: " + e);
		}
		return (retval);
	}

	// experimental at the moment. does not work.
	@Funq("pcompress")
	public String pcompress(String argument) {
		String retval="";
		retval = pRunner("./scripts/compresstest.py","large");
		return (retval);
	}

	@Funq
	public String cloudeventbenchmark(Benchmark input) {
		String retval = "...NoReturnValueSet...";
		long benchmarkStartTime = System.nanoTime();
		log.info("Received request to run benchmark: " + input.getName() + " at " + new java.util.Date());
//		log.info("CWD is: " + Path.of("").toAbsolutePath().toString());
		switch (input.getName()) {
			case "p0000":
				retval = pRunner("./scripts/0000.py","large");
				break;
			case "j0000":
				try {
					retval = jRunner("J0000",new J0000(),"large");
				} catch (Exception e) {
					log.info(retval = "Error with object construction: " + e);
				}
				break;
			case "all":
				retval="See log output for extensive data.";
				log.info("###Test,download time(s),download size(b),compute time(s),upload size(b),upload time(s)");
				for (int i = 1; i <= 1; i++) {
					log.info("Running python compression benchmark iteration " + i + ".");
					try {
						List<String> results = runner.runScript("./scripts/compresstest.py");
						for (String item : results) {
							log.info(item);
						}
					} catch (Exception e) {
						log.info(retval=retval+="Error running python compression benchmark iternation " + i + ". " + e);
					}

					log.info("Running java compression benchmark iteration " + i + ".");
					try {
						JCompress benchmark = new JCompress();
						String csvLine = benchmark.runTest();
						log.info("###" + csvLine);
					} catch (Exception e) {
						log.info(retval=retval+"Error running java compression benchmark iteration " + i + ". " + e);
						e.printStackTrace(System.out);
					}
				}
				break;
			case "test":
				log.info(retval="Bench test!");
				break;
			case "pcompress":
				retval = pRunner("./scripts/compresstest.py","large");
				break;
			case "pcompresslong":
				retval="See log output for extensive data.";
				log.info("Running python compression long benchmark.");
				log.info("###Test,download time(s),download size(b),compute time(s),upload size(b),upload time(s)");
				for (int i = 1; i <= 50; i++) {
					try {
						List<String> results = runner.runScript("./scripts/compresstest.py");
						for (String item : results) {
							log.info(item);
						}
					} catch (Exception e) {
						log.info("Error running python compression long benchmark." + e);
					}
				}
				break;
			case "psmallcompress":
				retval = pRunner("./scripts/compresstest.py","small");
				break;
			case "pmediumcompress":
				retval = pRunner("./scripts/compresstest.py","medium");
				break;
			case "jcompresslong":
				retval="See log output for extensive data.";
				log.info("Running java compression long benchmark.");
				log.info("###Test,download time(s),download size(b),compute time(s),upload size(b),upload time(s)");
				for (int i = 1; i <= 100; i++) {
					try {
						JCompress benchmark = new JCompress();
						String csvLine = benchmark.runTest();
						log.info("###" + csvLine);
					} catch (Exception e) {
						log.info("Error running java compression long benchmark." + e);
						e.printStackTrace(System.out);
					}
				}
				break;
			case "jdownloadlong":
				retval="See log output for extensive data.";
				log.info("Running java download long benchmark.");
				log.info("###Test,download time(s),download size(b)");
				for (int i = 1; i <= 100; i++) {
					try {
						JDownload benchmark = new JDownload();
						String csvLine = benchmark.runTest();
						log.info("###" + csvLine);
					} catch (Exception e) {
						log.info("Error running java download long benchmark." + e);
						e.printStackTrace(System.out);
					}
				}
				break;
			case "jcompress":
				try {
					retval = jRunner("JCompress",new JCompress(),"large");
				} catch (Exception e) {
					log.info(retval = "Error with object construction: " + e);
				}
				break;
			case "jmediumcompress":
				try {
					retval = jRunner("JCompress",new JCompress(),"medium");
				} catch (Exception e) {
					log.info(retval = "Error with object construction: " + e);
				}
				break;
			case "jsmallcompress":
				try {
					retval = jRunner("JCompress",new JCompress(),"small");
				} catch (Exception e) {
					log.info(retval = "Error with object construction: " + e);
				}
				break;
			case "p110":
				retval = pRunner("./scripts/110.py","1000");
				break;
			case "j110":
				try {
					retval = jRunner("J110",new J110(),"1000");
				} catch (Exception e) {
					log.info(retval = "Error with object construction: " + e);
				}
				break;
			case "p120":
				retval = pRunner("./scripts/120.py","large");
				break;
			case "j120":
				try {
					retval = jRunner("J120",new J120(),"large");
				} catch (Exception e) {
					log.info(retval = "Error with object construction: " + e);
				}
				break;
			case "jpagerank":
				retval=graphBenchRunner("pagerank", "test");
				break;
			case "jtestpagerank":
				retval=graphBenchRunner("pagerank", "test");
				break;
			case "jsmallpagerank":
				retval=graphBenchRunner("pagerank", "small");
				break;
			case "jlargepagerank":
				retval=graphBenchRunner("pagerank", "large");
				break;
			case "jmst":
				retval=graphBenchRunner("mst", "small");
				break;
			case "jtestmst":
				retval=graphBenchRunner("mst", "test");
				break;
			case "jsmallmst":
				retval=graphBenchRunner("mst", "small");
				break;
			case "jlargemst":
				retval=graphBenchRunner("mst", "large");
				break;
            case "jbfs":
                retval=graphBenchRunner("bfs", "small");
                break;
            case "jtestbfs":
                retval=graphBenchRunner("bfs", "test");
                break;
            case "jsmallbfs":
                retval=graphBenchRunner("bfs", "small");
                break;
            case "jlargebfs":
                retval=graphBenchRunner("bfs", "large");
                break;
			case "one":
				log.info(retval="Running benchmark one.");
				break;
			case "two":
				log.info(retval="Running benchmark two.");
				break;
			default:
				log.info(retval="Unsupported benchmark.");
				break;
		}
		long benchmarkStopTime = System.nanoTime();
		double benchmarkTime = (benchmarkStopTime - benchmarkStartTime) / 1000000000.0;
		log.info("%%%OVERANDOUT%%% for " + input.getName() + " at " + new java.util.Date() + ". Runtime was "
				+ String.format("%.01f", benchmarkTime / 60) + " minutes.");
		return (retval+","+String.format("%.03f",benchmarkTime));
	}

        private String jRunner(String name, BenchmarkTest benchmark, String size) {
		String retval="";
		log.info("Running java "+size+" "+name+" benchmark.");
		try {
			String csvLine = benchmark.runTest(size);
			log.info("###" + csvLine);
			retval="###" + csvLine;
		} catch (Exception e) {
			log.info(retval="Error running java "+size+" "+name+" benchmark." + e);
		}
		return (retval);
        }

        private String pRunner(String name, String size) {
		String retval="";
		log.info("Running python "+size+" "+name+" benchmark.");
		try {
			List<String> results = runner.runScript(name,size);
			for (String item : results) {
				log.info(item);
				retval=retval+item;
			}
		} catch (Exception e) {
			log.info(retval = "Error running python "+size+" "+name+" benchmark." + e);
		}
		return (retval);
        }

	private String graphBenchRunner(String name, String size) {
		String retval = "";
		log.info("Running java " + (size == null ? "" : size + " ") + name + " benchmark.");
		try {
			SeBS_Graph benchmark = new SeBS_Graph();
			Map<String, Double> res;
			switch (name) {
				case "pagerank":
					res = benchmark.pagerank(size).getMeasurement();
					break;
				case "mst":
					res = benchmark.mst(size).getMeasurement();
					break;
                case "bfs":
                    res = benchmark.bfs(size).getMeasurement();
                    break;
				default:
					res = new HashMap<>();
					break;
			}
			log.info("size of map returned from " + name + ": " + res.size());
			for (var entry : res.entrySet()) {
				log.info(entry.getKey() + "/" + entry.getValue());
				retval=retval+entry.getKey() + "/" + entry.getValue()+ " ";
			}
		} catch (Exception e) {
			log.info(retval = "Error running java "+name+" benchmark." + e);
			e.printStackTrace(System.out);
		}
		return (retval);
	}
}
