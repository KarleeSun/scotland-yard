package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.*;

public class Score {
    //只給mrX打分 也只在mrX的層給分
    //一個問題：隨著距離的變化距離比重會越來越小 但比重應該很大
    public Score() {
    }

    public int giveScore(@Nonnull Board board, Minimax.Info gameData, Move move) { //這個函數就是最終給分的函數
        System.out.println("move: " + move);
        System.out.println("moves: " + move);
        Dijkstra dijkstra = new Dijkstra(board);
        int score;
        if (move instanceof Move.SingleMove) {
            score = distanceScore(gameData, move, dijkstra) * 10 + transportationScore(board, move.source())
                    + guessPossibilityScore(board, move.source(), ((Move.SingleMove) move).ticket)
                    + 10 * edgeScore(move, dijkstra) + transportScore((Move.SingleMove) move);
        } else {
            score = distanceScore(gameData, move, dijkstra) * 10 + transportationScore(board, move.source())
                    + guessPossibilityScore(board, move.source(), ((Move.DoubleMove) move).ticket2)
                    + 10 * edgeScore(move, dijkstra) + transportScore((Move.SingleMove) move);
        }
        System.out.println("score: " + score);
        return score;
    }

    public int distanceScore(Minimax.Info gameData, Move move, Dijkstra dijkstra) { //根據距離給分
        Xbot xbot = new Xbot();
        List<Integer> distance = dijkstra.getDetectivesDistance(getDestination(move), xbot.getLocAsList(gameData.detectives));
        int shortest = distance.get(0);
        int average = (int) distance.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
        System.out.println("distance score: " + (shortest * 10 + average));
        if(shortest < 3) return shortest*10000;
        return shortest * 10 + average;
    }

    public int transportationScore(@Nonnull Board board, int loc) { //move.source()傳位置
        int adjacentNodesNum = board.getSetup().graph.adjacentNodes(loc).size();
        int transportationTypeNum = 0;
        for (Integer i : board.getSetup().graph.adjacentNodes(loc)) {
            transportationTypeNum += board.getSetup().graph.edgeValueOrDefault(loc, i, ImmutableSet.of()).size();
        }
        System.out.println("transportation score: " + (int) (2 * adjacentNodesNum + transportationTypeNum));
        return (int) (2 * adjacentNodesNum + transportationTypeNum); //先这么设置，不合适再改
    }

    public int guessPossibilityScore(@Nonnull Board board, int source, ScotlandYard.Ticket ticket) { //move.source,move.ticket
        int possibleScore = 0;
        Set<Integer> allAdjacentNodes = board.getSetup().graph.adjacentNodes(source);
        if (ticket == ScotlandYard.Ticket.SECRET) possibleScore = allAdjacentNodes.size();
        else {
            for (Integer node : allAdjacentNodes) {
                for (ScotlandYard.Transport t : board.getSetup().graph.edgeValueOrDefault(source, node, ImmutableSet.of())) {
                    if (t.requiredTicket() == ticket) possibleScore += 1;
                }
            }
        }
        System.out.println("possible score: " + 5 * possibleScore);
        return 5 * possibleScore;
    }

    public int edgeScore(Move move, Dijkstra dijkstra) {
        List<Integer> edgeList = List.of(8, 189, 6, 175, 4, 197);
        System.out.println("edge score: " + 5 * dijkstra.getDetectivesDistance(getDestination(move), edgeList).get(0));
        return 5 * dijkstra.getDetectivesDistance(getDestination(move), edgeList).get(0);
    }

    public int transportScore(Move.SingleMove move) {
        return switch (move.ticket) {
            case TAXI -> 20;
            case BUS -> 30;
            case UNDERGROUND -> 50;
            default -> 0;
        };
    }

    public int getDestination(Move move) {
        int distance = move instanceof Move.SingleMove
                ? ((Move.SingleMove) move).destination : ((Move.DoubleMove) move).destination2;
        return distance;
    }
}
