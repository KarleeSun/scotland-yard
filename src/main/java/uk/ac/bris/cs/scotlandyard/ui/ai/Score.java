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
    public Score() { }

    public int giveScore(@Nonnull Board board, Minimax.Info gameData, Move move){ //這個函數就是最終給分的函數
        System.out.println("---------------------------");
        Dijkstra dijkstra = new Dijkstra(board);
        int score;
        if(move instanceof Move.SingleMove) {
            score = distanceScore(gameData, move, dijkstra) * 10 + transportationScore(board, move.source())
                    + guessPossibilityScore(board, move.source(), ((Move.SingleMove) move).ticket)
                    + edgeScore(move, dijkstra) + strategyScore(board,move,dijkstra,gameData);
        } else {
            score = distanceScore(gameData, move, dijkstra) * 10 + transportationScore(board, move.source())
                    + guessPossibilityScore(board, move.source(), ((Move.DoubleMove) move).ticket2)
                    + edgeScore(move, dijkstra) + strategyScore(board,move,dijkstra,gameData);
        }
        System.out.println("score: "+score);
        return score;
    }

    public int distanceScore(Minimax.Info gameData, Move move, Dijkstra dijkstra){ //根據距離給分
        Xbot xbot = new Xbot();
        List<Integer> distance = dijkstra.getDetectivesDistance(getDestination(move), xbot.getLocAsList(gameData.detectives));
        int shortest = distance.get(0);
        int average = (int)distance.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
        System.out.println("distance score: "+ (shortest * 8 + average * 2));
        return shortest * 8 + average * 2;
    }

    public int transportationScore (@Nonnull Board board, int loc){ //move.source()傳位置
        int adjacentNodesNum = board.getSetup().graph.adjacentNodes(loc).size();
        int transportationTypeNum = 0;
        for (Integer i: board.getSetup().graph.adjacentNodes(loc)){
            transportationTypeNum += board.getSetup().graph.edgeValueOrDefault(loc,i, ImmutableSet.of()).size();
        }
        System.out.println("transportation score: "+ (int)(2 * adjacentNodesNum + transportationTypeNum));
        return (int)(2 * adjacentNodesNum + transportationTypeNum); //先这么设置，不合适再改
    }

    public int guessPossibilityScore(@Nonnull Board board, int source, ScotlandYard.Ticket ticket){ //move.source,move.ticket
        int possibleScore = 0;
        Set<Integer> allAdjacentNodes = board.getSetup().graph.adjacentNodes(source);
        if(ticket == ScotlandYard.Ticket.SECRET) possibleScore = allAdjacentNodes.size();
        else {
            for(Integer node: allAdjacentNodes){
                for(ScotlandYard.Transport t: board.getSetup().graph.edgeValueOrDefault(source,node,ImmutableSet.of())){
                    if (t.requiredTicket() == ticket) possibleScore += 1;
                }
            }
        }
        System.out.println("possible score: " + 5*possibleScore);
        return 5*possibleScore;
    }

    public int edgeScore(Move move,Dijkstra dijkstra){
        List<Integer> edgeList = List.of(8,189,6,175,4,197);
        System.out.println("edge score: "+ 5*dijkstra.getDetectivesDistance(getDestination(move),edgeList).get(0));
        return 5 * dijkstra.getDetectivesDistance(getDestination(move),edgeList).get(0);
    }

    public int strategyScore(@Nonnull Board board, Move move, Dijkstra dijkstra, Minimax.Info gameData) {
        System.out.println("into s score");
        Xbot xbot = new Xbot();
        int strategyScore = 0;
        if (board.getSetup().moves.get(board.getMrXTravelLog().size() + 1)) {
            if (move instanceof Move.SingleMove && ((Move.SingleMove) move).ticket == ScotlandYard.Ticket.SECRET)
                strategyScore = 20;
            if (move instanceof Move.DoubleMove) {
                strategyScore += 50;
                if (((Move.DoubleMove) move).ticket1 == ScotlandYard.Ticket.SECRET) strategyScore += 10;
                if (((Move.DoubleMove) move).ticket2 == ScotlandYard.Ticket.SECRET) strategyScore += 10;
            }
        } else {
            if((dijkstra.getDetectivesDistance(move.source(),xbot.getLocAsList(gameData.detectives)).get(0)) <= 2)
                return strategyScore;
            if (move instanceof Move.SingleMove && ((Move.SingleMove) move).ticket == ScotlandYard.Ticket.SECRET)
                strategyScore = -20;
            if (move instanceof Move.DoubleMove) {
                strategyScore -= 500;
                if (((Move.DoubleMove) move).ticket1 == ScotlandYard.Ticket.SECRET) strategyScore -= 10;
                if (((Move.DoubleMove) move).ticket2 == ScotlandYard.Ticket.SECRET) strategyScore -= 10;
            }
        }
        System.out.println("strategy score: "+strategyScore);
        return strategyScore;
    }

    public int getDestination(Move move){
        int distance = move instanceof Move.SingleMove
            ? ((Move.SingleMove)move).destination : ((Move.DoubleMove)move).destination2;
        return distance;
    }
}
