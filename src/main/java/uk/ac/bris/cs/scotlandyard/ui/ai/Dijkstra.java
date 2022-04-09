package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.*;

public class Dijkstra {
    private int mrXLocation;
    private List<Integer> detectivesLocation;
    private Board board;

    public Dijkstra(int mrXLocation, List<Integer> detectivesLocation, Board board) {
        this.mrXLocation = mrXLocation;
        this.detectivesLocation = detectivesLocation;
        this.board = board;
    }

    public List<Integer> getDetectivesDistance() {
        System.out.println("进到函数里面了");
        //这就开始有问题
        int[] allDistance = dijkstra(board.getSetup(), mrXLocation);
        System.out.println("1");
        List<Integer> destinations = detectivesLocation;
        System.out.println("2");
        destinations.stream().map(x -> allDistance[x]).sorted();
        System.out.println("3");
        System.out.println(destinations);
        System.out.println("4");
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

    private int transportToDistance(@Nonnull Board board, ScotlandYard.Transport t) {
        Xbot xbot = new Xbot();
        List<Piece.Detective> detectives = new ArrayList<>(xbot.getAllDetectives(board));
        int[] ticketsCount = new int[]{0,0,0};
        for (Piece.Detective detective: detectives){
            ticketsCount[0] += board.getPlayerTickets(detective).get().getCount(ScotlandYard.Ticket.TAXI);
            ticketsCount[1] += board.getPlayerTickets(detective).get().getCount(ScotlandYard.Ticket.BUS);
            ticketsCount[2] += board.getPlayerTickets(detective).get().getCount(ScotlandYard.Ticket.UNDERGROUND);
        }

        switch (t.toString()) {
            case "TAXI":
                return 100/ticketsCount[0];
            case "BUS":
                return 100/ticketsCount[1];
            case "UNDERGROUND":
                return 100/ticketsCount[2];
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

        System.out.println("1111111");

        while (priorityQueue.size() > 0) {
            node current = priorityQueue.poll();
            System.out.println("pq.size: " + priorityQueue.size());

            for (int adjacent : setup.graph.adjacentNodes(current.getVertex())) {
                Set<ScotlandYard.Transport> transports = setup.graph.edgeValueOrDefault(current.getVertex(), adjacent, ImmutableSet.of());
                //distance from current node to one of its adjacent node
                int shortestRoute = Integer.MAX_VALUE;
                for(ScotlandYard.Transport t : transports){
                    int currentRoute = transportToDistance(board, t);
                    if(currentRoute < shortestRoute)
                        shortestRoute = currentRoute;
                }
                node adjacentNode = new node(adjacent, shortestRoute);
                //update distance
                if(distance[current.getVertex()] + adjacentNode.getDistance() < distance[adjacentNode.getVertex()]){
                    distance[adjacentNode.getVertex()] = adjacentNode.getDistance() + distance[current.getVertex()];
                    if(priorityQueue.contains(adjacentNode)) priorityQueue.remove(adjacentNode);
                    priorityQueue.add(new node(adjacentNode.getVertex(), distance[adjacentNode.getVertex()]));
                }
            }
        }

        System.out.println("distance:" + distance);
        return distance;
    }


}