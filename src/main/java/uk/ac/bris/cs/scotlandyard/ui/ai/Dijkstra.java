package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.*;

public class Dijkstra {
    private final Board board;
    private final int[] distance;                         /*store distance from source to vertex(index)*/
    private final Set<Integer> terminated;                /*store terminated vertices*/
    private final PriorityQueue<Node> priorityQueue;
    private final int V;                                  /*total number of vertices*/
    private final List<List<Node>> adjacentNodes;         /*store all adjacent nodes of all nodes*/
    private final Node source;                            /*source node: mrX's location*/
    private final List<Integer> destinations;             /*detectivesLocation*/

    public Dijkstra(int mrXLocation, List<Integer> detectivesLocation, Board board) {
        this.board = board;
        V = board.getSetup().graph.nodes().size() + 1;        /*array starts from 0*/
        terminated = new HashSet<Integer>();
        priorityQueue = new PriorityQueue<Node>(V, new Node());         /*order according to Node weight*/
        adjacentNodes = getAllAdjacentNodes(board);                     /*graph vertex start from 1, fill 0 with empty list*/
        source = new Node(mrXLocation, 0);          /*distance from source to source is 0*/
        destinations = detectivesLocation;
        distance = dijkstraShortestDistance(source.vertex);
    }

    public Dijkstra(int mrXLocation, int detectiveLocation, Board board) {
        this.board = board;
        V = board.getSetup().graph.nodes().size() + 1;        /*array starts from 0*/
        terminated = new HashSet<Integer>();
        priorityQueue = new PriorityQueue<Node>(V, new Node());         /*order according to Node weight*/
        adjacentNodes = getAllAdjacentNodes(board);                     /*graph vertex start from 1, fill 0 with empty list*/
        source = new Node(mrXLocation, 0);          /*distance from source to source is 0*/
        destinations = List.of(detectiveLocation);
        distance = dijkstraShortestDistance(source.vertex);
    }

    //return: a list of weights from mr X to individual detectives, ordered from lowest to largest
    public List<Integer> getDetectivesDistance() {
        List<Integer> detectiveDistances = new ArrayList();
        for (Integer d : destinations) {
            detectiveDistances.add(distance[d]);
        }
        detectiveDistances.sort(Comparator.naturalOrder());
        return detectiveDistances;
    }

    public int getDistance(){
        return distance[destinations.get(0)];
    }

    //convert transportation to distance according the number of according ticket left from detectives
    private int transportToDistance(@Nonnull Board board, ScotlandYard.Transport t) {
        Xbot xbot = new Xbot();
        List<Piece.Detective> detectives = new ArrayList<>(xbot.getAllDetectives(board));
        int[] ticketsCount = new int[]{0, 0, 0};
        for (Piece.Detective detective : detectives) {
            ticketsCount[0] += board.getPlayerTickets(detective).get().getCount(ScotlandYard.Ticket.TAXI);
            ticketsCount[1] += board.getPlayerTickets(detective).get().getCount(ScotlandYard.Ticket.BUS);
            ticketsCount[2] += board.getPlayerTickets(detective).get().getCount(ScotlandYard.Ticket.UNDERGROUND);
        }
        return switch (t.toString()) {
            case "TAXI" -> 100 / ticketsCount[0];
            case "BUS" -> 100 / ticketsCount[1];
            case "UNDERGROUND" -> 100 / ticketsCount[2];
            default -> 1000000;
        };
    }

    private List<List<Node>> getAllAdjacentNodes(Board board) {
        List<List<Node>> allAdjacentNodes = new ArrayList<>();
        allAdjacentNodes.add(List.of(new Node(-1, -1)));
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = board.getSetup().graph;
        //iterate through all vertex in the graph
        for (Integer vertex : graph.nodes()) {
            List<Node> adjOfOneVertex = new ArrayList<>();
            //iterate through all adjacent vertex of a vertex
            for (Integer adjVertex : graph.adjacentNodes(vertex)) {
                List<Integer> transportWeights = new ArrayList<>();
                //iterate through all possible transportation from vertex to adjVertex and convert to weight
                for (ScotlandYard.Transport t : graph.edgeValueOrDefault(vertex, adjVertex, ImmutableSet.of())) {
                    transportWeights.add(transportToDistance(board, t));
                }
                transportWeights.sort(Comparator.naturalOrder());
                adjOfOneVertex.add(new Node(adjVertex, transportWeights.get(0)));       /*make new node for adj vertex*/
            }
            allAdjacentNodes.add(adjOfOneVertex);
        }
        return allAdjacentNodes;
    }

    //Dijkstra to find the shortest distance between source and destination
    private int[] dijkstraShortestDistance(int source) {
        int[] sourceToVertexDistance = new int[V];
        adjacentNodes.addAll(getAllAdjacentNodes(board));  /*make an adjacency list of all vertex in the graph*/
        for (int i = 0; i < V; i++) sourceToVertexDistance[i] = 1000000;        /*assign initial value*/
        sourceToVertexDistance[source] = 0;
        priorityQueue.add(new Node(source, 0));         /*add source to priority queue*/
        while (terminated.size() != V - 1) {          /*terminated stores from 0*/
            Node w = priorityQueue.poll();          /*poll the first element in priority queue*/
            System.out.println("PQ:" + priorityQueue.size());
            if (terminated.contains(w.vertex)) continue;         /*skip if already terminated shorter distance*/
            terminated.add(w.vertex);
            processAdjacentVertices(w, sourceToVertexDistance);         /*process adjacent vertices*/
        }
        return sourceToVertexDistance;
    }

    private void processAdjacentVertices(Node w, int[] sourceToVertexDistance) {
        System.out.println("w: " + w);
        System.out.println("adjacent nodes: " + adjacentNodes.get(w.vertex).toString());
        for (Node v : adjacentNodes.get(w.vertex)) {      /*iterate through all adjacent nodes of 1 vertex*/
            System.out.println("v: " + v);
            if (!terminated.contains(v.vertex)) {
                int alternativeDistance = sourceToVertexDistance[w.vertex] + v.weight;        /*(source -> w) + (w -> v)*/
                if (alternativeDistance < sourceToVertexDistance[v.vertex]) {
                    sourceToVertexDistance[v.vertex] = alternativeDistance;       /*update distance if nearer*/
                    System.out.println("update distance: " + alternativeDistance);
                }
                priorityQueue.add(new Node(v.vertex, alternativeDistance));         /*add new node to priority queue*/
            }
        }
    }

    //Represents one vertex in the graph with distance to source vertex
    class Node implements Comparator<Node> {
        public int vertex;
        public int weight;

        public Node() {
        }

        public Node(int vertex, int weight) {
            this.vertex = vertex;
            this.weight = weight;
        }

        @Override
        public int compare(Node n1, Node n2) {          /*min first*/
            return Integer.compare(n1.weight, n2.weight);
        }
    }
}
