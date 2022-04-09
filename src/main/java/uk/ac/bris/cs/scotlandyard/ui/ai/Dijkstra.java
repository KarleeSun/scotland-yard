package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import org.w3c.dom.Node;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.*;

public class Dijkstra {
    private int distance[];         /*store distance from source to vertex(index)*/
    private Set<Integer> terminated;        /*store terminated vertices*/
    private PriorityQueue<Node> priorityQueue;
    private int V;      /*total number of vertices*/
    private List<List<Node>> adjacentNodes;         /*store all adjacent nodes of all nodes*/
    private Node source;         /*source node: mrX's location*/
    private List<Integer> destinations;         /*detectivesLocation*/

    public Dijkstra(int mrXLocation, List<Integer> detectivesLocation, Board board) {
        V = board.getSetup().graph.nodes().size();
        distance = new int [V];
        terminated = new HashSet<Integer>();
        priorityQueue = new PriorityQueue<Node>(V, new Node());
        source = new Node(mrXLocation, 0);          /*distance from source to source is 0*/
        destinations = detectivesLocation;

    }

    public List<Integer> getDetectivesDistance() {
        int[] allDistance = dijkstra(board.getSetup(), mrXLocation);
        List<Integer> destinations = detectivesLocation;
        for(int d : detectivesLocation){
            System.out.println("detective distance: " + allDistance[d]);
        }
        return destinations;
    }

    private int transportToDistance(ScotlandYard.Transport t) {
        System.out.println(t.toString());
        switch (t.toString()) {
            case "TAXI":
                return 2;
            case "BUS":
                return 3;
            case "UNDERGROUND":
                return 8;
        }
        return Integer.MAX_VALUE;
    }

    private List<List<Node>> getAllAdjacentNodes(Board board){
        for(Integer vertex : board.getSetup().graph.nodes()){
            board.getSetup().graph.adjacentNodes(vertex).stream().toList();

        }
    }


    //Dijkstra to find the shortest distance between source and destination
    private int[] dijkstraShortestDistance(List<List<Node>> adjacentNodes, int source) {

    }

    //Represents one vertex in the graph with distance to source vertex
    class Node implements Comparator<Node>{
        public int vertex;
        public int weight;

        public Node(int vertex, int weight){
            this.vertex = vertex;
            this.weight = weight;
        }
        @Override
        public int compare(Node n1, Node n2) {          /*min first*/
            if(n1.weight < n2.weight) return -1;
            if(n2.weight < n1.weight) return 1;
            return 0;
        }
    }
}

