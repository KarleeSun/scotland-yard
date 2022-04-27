package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class Score {
    //只給mrX打分 也只在mrX的層給分
    //並且只能用於singleMove
    //一個問題：隨著距離的變化距離比重會越來越小 但比重應該很大
    public Score() { }

    public int giveScore(@Nonnull Board board, Minimax.Info gameData, Move move){ //這個函數就是最終給分的函數
        Dijkstra dijkstra = new Dijkstra(board);
        return distanceScore(gameData,move,dijkstra)*5 + transportationScore(board,move.source())
                + guessPossibilityScore(board,move.source(),((Move.SingleMove)move).ticket) + edgeScore(move, dijkstra);
    }

    public int distanceScore(Minimax.Info gameData, Move move, Dijkstra dijkstra){ //根據距離給分
        Xbot xbot = new Xbot();
        List<Integer> distance = dijkstra.getDetectivesDistance(getDestination(move), xbot.getLocAsList(gameData.detectives));
        int shortest = distance.get(0);
        int average = (int)distance.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
        return (int)(shortest * 8 + average * 2);
    }

    public int transportationScore (@Nonnull Board board, int loc){ //move.source()傳位置
        int adjacentNodesNum = board.getSetup().graph.adjacentNodes(loc).size();
        int transportationTypeNum = 0;
        for (Integer i: board.getSetup().graph.adjacentNodes(loc)){
            transportationTypeNum += board.getSetup().graph.edgeValueOrDefault(loc,i, ImmutableSet.of()).size();
        }
        //测试用
        System.out.println("transportationScore: "+ (2 * adjacentNodesNum + transportationTypeNum));
        return 2 * adjacentNodesNum + transportationTypeNum; //先这么设置，不合适再改
    }

    public int guessPossibilityScore(@Nonnull Board board, int source, ScotlandYard.Ticket ticket){ //move.source,move.ticket
        int possibleNode = 0;
        Set<Integer> allAdjacentNodes = board.getSetup().graph.adjacentNodes(source);
        if(ticket == ScotlandYard.Ticket.SECRET) possibleNode = allAdjacentNodes.size();
        else {
            for(Integer node: allAdjacentNodes){
                for(ScotlandYard.Transport t: board.getSetup().graph.edgeValueOrDefault(source,node,ImmutableSet.of())){
                    if (t.requiredTicket() == ticket) possibleNode++;
                }
            }
        }
        //测试用
        System.out.println("guessPossibilityScore: "+ possibleNode);
        return possibleNode;
    }

    public int edgeScore(Move move,Dijkstra dijkstra){
        List<Integer> edgeList = List.of(8,189,6,175);
        return dijkstra.getDetectivesDistance(getDestination(move),edgeList).get(0);
    }

    public int getDestination(Move move){
        int distance = move instanceof Move.SingleMove
            ? ((Move.SingleMove)move).destination : ((Move.DoubleMove)move).destination2;
        return distance;
    }
}
