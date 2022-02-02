package com.ibm.trl.funqyTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.util.SupplierUtil;

import io.quarkus.funqy.Funq;

public class MyFunctions {
    static double nanosecInSec = 1_000_000_000.0;

    static Map<String, Integer> size_generators = Map.of("test",  10,
                                                         "small", 10000,
                                                         "large", 100000);

    @Funq("Hello")
    public String helloOnly() {
        return "Hello!";
    }

    @Funq
    public String hello(String msg) {
        return "Hello " + msg;
    }

    private int graphSize(String size) {
        int graphSize = 10;  // default size is "test"

        if(size != null) {
            Integer gs = size_generators.get(size);
            if(gs != null) {
                graphSize = gs.intValue();
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
    
    @Funq
    public RetValType<Integer, Double> pagerank(String size) {
        RetValType<Integer, Double> retVal = new RetValType<>();

        Graph<Integer, DefaultEdge> inputGraph = genGraph(graphSize(size), retVal);

        PageRank<Integer, DefaultEdge> algo = new PageRank<>(inputGraph);

        long process_begin = System.nanoTime();
        Map<Integer, Double> score = algo.getScores();
        long process_end= System.nanoTime();

        retVal.result = score;
        retVal.measurement.put("compute_time", (process_end - process_begin)/nanosecInSec); 

        return retVal;
    }

    @Funq
    public RetValType<String, ArrayList<String>> mst(String size) {
        RetValType<String, ArrayList<String>> retVal = new RetValType<>();

        int graphSize = graphSize(size);

        Graph<Integer, DefaultEdge> inputGraph = genGraph(graphSize, retVal);

        SpanningTreeAlgorithm<DefaultEdge> algo = new PrimMinimumSpanningTree<>(inputGraph);

        long process_begin = System.nanoTime();
        SpanningTreeAlgorithm.SpanningTree<DefaultEdge> mst = algo.getSpanningTree();
        long process_end= System.nanoTime();

        ArrayList<String> mstList = new ArrayList<>(graphSize);
        for(Iterator<DefaultEdge> it = mst.iterator(); it.hasNext(); mstList.add(it.next().toString()));
        retVal.result = Map.of("mst", mstList);
        retVal.measurement.put("compute_time", (process_end - process_begin)/nanosecInSec); 

        return retVal;
    }

    @Funq
    public RetValType<String, ArrayList<Integer>> bfs(String size) {
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
        
        retVal.result = Map.of("verticies", verticies, "layers", layers, "parents", new ArrayList<Integer>(Arrays.asList(parents)));
        retVal.measurement.put("compute_time", (process_end - process_begin)/nanosecInSec); 

        return retVal;
    }

    public static class RetValType<V, U> {
        public Map<V, U> result;
        public Map<String, Double> measurement;

        RetValType() {
            measurement = new HashMap<String, Double>();
        }
    }
}
