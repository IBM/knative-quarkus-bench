package com.ibm.trl.serverlessbench;

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

public class GraphMST {
    static double nanosecInSec = 1_000_000_000.0;

    static Map<String, Integer> size_generators = Map.of("test",  10,
                                                         "small", 10000,
                                                         "large", 100000);

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
