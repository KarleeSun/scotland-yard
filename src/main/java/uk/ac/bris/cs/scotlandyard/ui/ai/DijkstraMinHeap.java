package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.*;

public class DijkstraMinHeap {
    private int mrXLocation;
    private List<Integer> detectivesLocation;
    private Board board;

    public DijkstraMinHeap(int mrXLocation, List<Integer> detectivesLocation, Board board) {
        this.mrXLocation = mrXLocation;
        this.detectivesLocation = detectivesLocation;
        this.board = board;
    }

    public List<Integer> getDetectivesDistance() {
        int[] allDistance = dijkstra(board.getSetup(), mrXLocation);
        List<Integer> destinations = detectivesLocation;
        destinations.stream().map(x -> allDistance[x]).sorted();
        System.out.println(destinations);
        return destinations;
    }

    private class node {
        private int vertex, distance;

        node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        public int getVertex() {return vertex;}
        public int getDistance() {return distance;}
    }

    private int transportToDistance(ScotlandYard.Transport t) {
        switch (t.toString()) {
            case "Taxi":
                return 2;
            case "Buse":
                return 3;
            case "Underground":
                return 8;
        }
        return Integer.MAX_VALUE;
    }

    //Dijkstra to find the shortest distance between source and destination
    public int[] dijkstra(GameSetup setup, int source) {
        final int V = setup.graph.nodes().size() + 1;
        int[] distance = new int[V];
        for (int i = 0; i < V; i++) {
            distance[i] = Integer.MAX_VALUE;
        }
        PriorityQueue<node> priorityQueue = new PriorityQueue<node>((v1, v2) -> v1.getDistance() - v2.getDistance());
        priorityQueue.add(new node(mrXLocation, 0));

        while (priorityQueue.size() > 0) {
            node current = priorityQueue.poll();

            for (int adjacent : setup.graph.adjacentNodes(current.getVertex())) {
                Set<ScotlandYard.Transport> transports = setup.graph.edgeValueOrDefault(current.getVertex(), adjacent, ImmutableSet.of());
                //distance from current node to one of its adjacent node
                int shortestRoute = Integer.MAX_VALUE;
                for(ScotlandYard.Transport t : transports){
                    int currentRoute = transportToDistance(t);
                    if(currentRoute < shortestRoute)
                        shortestRoute = currentRoute;
                }
                node adjacentNode = new node(adjacent, shortestRoute);
                //update distance
                if(distance[current.getVertex()] + adjacentNode.getDistance() < distance[adjacentNode.getVertex()]){
                    distance[adjacentNode.getVertex()] = adjacentNode.getDistance() + distance[current.getVertex()];
                    priorityQueue.add(new node(adjacentNode.getVertex(), distance[adjacentNode.getVertex()]));
                }
            }
        }
        return distance;
    }


}
