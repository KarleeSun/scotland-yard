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
        Dijkstra dijkstra = new Dijkstra(board);
        if(move instanceof Move.SingleMove) {
            return distanceScore(gameData, move, dijkstra) * 5 + transportationScore(board, move.source())
                    + guessPossibilityScore(board, move.source(), ((Move.SingleMove) move).ticket)
                    + edgeScore(move, dijkstra) + areaScore(move,gameData);
        } else {
            return distanceScore(gameData, move, dijkstra) * 5 + transportationScore(board, move.source())
                    + guessPossibilityScore(board, move.source(), ((Move.DoubleMove) move).ticket2)
                    + edgeScore(move, dijkstra) + areaScore(move,gameData);
        }
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
        return possibleNode;
    }

    public int edgeScore(Move move,Dijkstra dijkstra){
        List<Integer> edgeList = List.of(8,189,6,175,4,197);
        return dijkstra.getDetectivesDistance(getDestination(move),edgeList).get(0);
    }

    public int areaScore(Move move, Minimax.Info gameData){
        final int[] NWarea = {1,2,3,8,9,10,11,12,18,19,20,21,22,23,31,32,33,34,35,36,
                43,44,45,46,47,48,57,58,59,60,61,62,73,74,75,76,77,78,79,92,93,94,95,96,97,98};
        final int[] NEarea = {4,5,6,7,13,14,15,16,17,24,25,26,27,28,29,30,37,38,39,40,41,
                42,50,51,52,53,54,55,56,68,69,70,71,72,86,87,88,89,90,91,104,105,106,107};
        final int[] Marea = {49,63,64,65,66,67,80,81,82,83,84,85,99,100,101,102,103,110,
                111,112,113,114,115,125,126,127,130,131,132,133,139,140,154};
        final int[] SWarea = {109,120,121,122,123,124,137,138,144,145,146,147,148,149,150,151,152,
                153,155,163,164,165,166,167,168,176,177,178,179,180,181,182,183,184,189,190,191,192,193,194,195,196,197};
        final int[] SEarea = {108,116,117,118,119,128,129,134,135,136,141,142,142,143,156,157,158,159,160,161,162,
                169,170,171,172,173,174,175,185,186,187,188,198,199};
        final int[][] areas = {NWarea, NEarea, Marea, SWarea,SEarea};

        Xbot xbot = new Xbot();
        int[] detectiveDensity = {0,0,0,0,0};
        List<Integer> detectiveLocs = xbot.getLocAsList(gameData.detectives);
        int x =0;
        for(Integer loc : detectiveLocs){
            int i = 0;
            for(int[] area : areas) {
                if (Arrays.binarySearch(area, loc) > 0) detectiveDensity[i]++;
                if (Arrays.binarySearch(area, getDestination(move))>0) x = i;
                i++;
            }
        }
        if(detectiveDensity[x] <= gameData.detectives.size()/2) return 5;
        else return 0;
    }

    public int getDestination(Move move){
        int distance = move instanceof Move.SingleMove
            ? ((Move.SingleMove)move).destination : ((Move.DoubleMove)move).destination2;
        return distance;
    }
}
