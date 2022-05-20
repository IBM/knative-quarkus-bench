package com.ibm.trl.knativebench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jgrapht.Graph;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.util.SupplierUtil;

import io.quarkus.funqy.Funq;

public class GraphBFS {
    static double nanosecInSec = 1_000_000_000.0;

    static Map<String, Integer> size_generators = Map.of("test",   10,
                                                         "tiny",   100,
                                                         "small",  1000,
                                                         "medium", 10000,
                                                         "large",  100000);
    @Inject
    Logger log;
    
    private int graphSize(String size) {
        int graphSize = 10;  // default size is "test"

        if(size != null) {
            Integer gs = size_generators.get(size);
            if(gs != null) {
                graphSize = gs.intValue();
            } else if(size.length() > 0) {
                graphSize = Integer.parseUnsignedInt(size);
            }
        }

        return graphSize;
    }

    private Graph<Integer, DefaultEdge> genGraph(int size, RetValType<?, ?> retVal) {
        Graph<Integer, DefaultEdge> inputGraph = GraphTypeBuilder.<Integer, DefaultEdge>undirected()
                                                                   .allowingMultipleEdges(true)
                                                                   .edgeClass(DefaultEdge.class)
                                                                   .vertexSupplier(SupplierUtil.createIntegerSupplier())
                                                                   .buildGraph();

        BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator = 
                new BarabasiAlbertGraphGenerator<>(10, 1, size);

        long graph_generating_begin = System.nanoTime();
        generator.generateGraph(inputGraph);
        long graph_generating_end= System.nanoTime();

        retVal.measurement.put("graph_generating_time", (graph_generating_end - graph_generating_begin)/nanosecInSec); 

        return inputGraph;
    }
    
    @Funq("graph-bfs")
    public RetValType<String, ArrayList<Integer>> graph_bfs(FunInput input) {
        String  size = input.size;
        boolean debug = Boolean.parseBoolean(input.debug);
        
        log.info(String.format("size=%s, debug=%b", size, debug));

        RetValType<String, ArrayList<Integer>> retVal = new RetValType<>();

        int graphSize = graphSize(size);

        Graph<Integer, DefaultEdge> inputGraph = genGraph(graphSize, retVal);

        var verticies = new ArrayList<Integer>(graphSize);
        var layers    = new ArrayList<Integer>(graphSize);
        var parents   = new Integer[graphSize];

        long process_begin = System.nanoTime();
        BreadthFirstIterator<Integer, DefaultEdge> it = new BreadthFirstIterator<>(inputGraph);

        int numVisited = 0;
        int lastDepth = -1;
        
        for( ; it.hasNext(); numVisited++) {
            Integer v = it.next();
            int depth = it.getDepth(v);

            verticies.add(v);
            parents[v.intValue()] = it.getParent(v);

            if(depth != lastDepth) {
                layers.add(numVisited);
            }
            lastDepth = depth;
        }
        layers.add(numVisited);
        
        long process_end= System.nanoTime();

        layers.removeIf((i) -> i == null);

        if(debug) {
            retVal.result = Map.of("verticies", verticies, "layers", layers, "parents", new ArrayList<Integer>(Arrays.asList(parents)));
        }

        retVal.measurement.put("compute_time", (process_end - process_begin)/nanosecInSec);
        
        log.info("retVal.measurement="+retVal.measurement.toString());

        return retVal;
    }

    public static class FunInput {
        public String size;
        public String debug;
    }

    public static class RetValType<V, U> {
        Map<V, U> result;
        Map<String, Double> measurement;

        public Map<V, U> getResult()           { return result; }
        public void      setResult(Map<V, U>v) { result = v; }

        public Map<String, Double> getMeasurement()                     { return measurement; }
        public void                setMeasurement(Map<String, Double>v) { measurement = v; }

        RetValType() {
            measurement = new HashMap<String, Double>();
        }
    }
}
