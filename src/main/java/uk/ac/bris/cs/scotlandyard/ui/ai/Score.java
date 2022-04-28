package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.*;

public class Score {

    public Score() {}

    // method used to assign the final score for a node
    // due to different weight, factor other than distance score is only considered when the distance score is the same
    public int giveScore(@Nonnull Board board, Minimax.Info gameData, Move move) {
        Dijkstra dijkstra = new Dijkstra(board);
        int score = distanceScore(gameData, move, dijkstra) + transportationScore(board, move.source())
                + guessPossibilityScore(board, move.source(), ((Move.SingleMove) move).ticket)
                + edgeScore(move, dijkstra) + transportScore((Move.SingleMove) move);
        return score;
    }

    // score factor based on distance between detectives.
    public int distanceScore(Minimax.Info gameData, Move move, Dijkstra dijkstra) {
        Xbot xbot = new Xbot();
        List<Integer> distance = dijkstra.getDetectivesDistance(getDestination(move), xbot.getLocAsList(gameData.detectives));
        // average distance between all detective and destination
        int average = (int) distance.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
        // distance between the nearest detective and Mr x
        int nearest = distance.get(0);
        return (nearest * 9 + average)*10;
    }

    // score factor based on possible destination of a vertex and possible transportation can be used for next move
    public int transportationScore(@Nonnull Board board, int loc) {
        int adjacentNodesNum = board.getSetup().graph.adjacentNodes(loc).size();
        int transportationTypeNum = 0;
        for (Integer i : board.getSetup().graph.adjacentNodes(loc)) {
            transportationTypeNum += board.getSetup().graph.edgeValueOrDefault(loc, i, ImmutableSet.of()).size();
        }
        return (2 * adjacentNodesNum + transportationTypeNum);
    }

    // possible move destination detective can guess based on the current location and transportation used
    public int guessPossibilityScore(@Nonnull Board board, int source, ScotlandYard.Ticket ticket) { //move.source,move.ticket
        int possibleScore = 0;
        Set<Integer> allAdjacentNodes = board.getSetup().graph.adjacentNodes(source);
        for (Integer node : allAdjacentNodes) {
            for (ScotlandYard.Transport t : board.getSetup().graph.edgeValueOrDefault(source, node, ImmutableSet.of())) {
                if (t.requiredTicket() == ticket) possibleScore += 1;
            }
        }
        return 5 * possibleScore;
    }

    // avoid Mr X go into corner
    public int edgeScore(Move move, Dijkstra dijkstra) {
        List<Integer> edgeList = List.of(8, 189, 6, 175, 4, 197);
        return 50 * dijkstra.getDetectivesDistance(getDestination(move), edgeList).get(0);
    }

    // transportation score assigned based on the weight of default detective tickets
    public int transportScore(Move.SingleMove move) {
        return switch (move.ticket) {
            case TAXI -> 30;
            case BUS -> 45;
            case UNDERGROUND -> 90;
            default -> 0;
        };
    }

    // helper function that returns the final destination of both single and double move
    public int getDestination(Move move) {
        int distance = move instanceof Move.SingleMove
                ? ((Move.SingleMove) move).destination
                : ((Move.DoubleMove) move).destination2;
        return distance;
    }
}
