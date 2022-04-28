package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.*;
import io.atlassian.fugue.Pair;

import uk.ac.bris.cs.scotlandyard.model.*;


public class Xbot implements Ai {
    private Piece.MrX MRX;

    @Nonnull
    @Override
    public String name() {
        return "Xbot";
    }

    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        MRX = Piece.MrX.MRX;
        Minimax minimax = new Minimax();
        Minimax.Info gameData = new Minimax.Info(getMrXPlayer(board, MRX), getDetectivePlayers(board, getAllDetectives(board)));
        Dijkstra dijkstra = new Dijkstra(board);
        int shortest = dijkstra.getDetectivesDistance(gameData.mrX.location(), getLocAsList(gameData.detectives)).get(0);
        System.out.println("shortest: " + dijkstra.getDetectivesDistance(gameData.mrX.location(), getLocAsList(gameData.detectives)));
        List<Move> moves = board.getAvailableMoves().stream().toList();
        Boolean afterReveal = board.getSetup().moves.get(board.getMrXTravelLog().size());
        Boolean doubleOrSecret = false;
        //use DOUBLE because the nearest detective is one step away from Mr X
        //only choose from DOUBLE MOVE, may or may not use SECRET
        if (shortest <= 1 && gameData.mrX.has(ScotlandYard.Ticket.DOUBLE)) { //situation 1
            System.out.println("situation: double");
            doubleOrSecret = true;
            moves = board.getAvailableMoves().stream().filter(move -> move instanceof Move.DoubleMove).toList();
        }
        //use SECRET because it right after reveal and the nearest detective is one step away from Mr x
        //only choose from SECRET Single move
        else if (gameData.mrX.has(ScotlandYard.Ticket.SECRET) && afterReveal && shortest <= 1) {
            System.out.println("situation: secret");
            doubleOrSecret = true;
            moves.stream().filter(move -> {
                List<ScotlandYard.Ticket> tickets = new ArrayList<>();
                for (ScotlandYard.Ticket ticket : move.tickets())
                    tickets.add(ticket);
                return tickets.contains(ScotlandYard.Ticket.SECRET);
            });
        }
        //choose move if one of the above condition fulfilled
        if (doubleOrSecret) {
            return moveGivesLongestDistance(moves, board);
        }
        //under normal condition, use Minimax
        System.out.println("situation 2");
        Minimax.TreeNode root = minimax.tree(board, 3, gameData);
        Minimax.TreeNode maxScoreNode = root.getChildren().get(0);
        //get the move with the highest score from current available moves
        for (Minimax.TreeNode childNode : root.getChildren()) {
            if (childNode.getScore() > maxScoreNode.getScore()) {
                System.out.println("child node score: " + childNode.getScore());
                System.out.println("max score: " + childNode.getScore());
                maxScoreNode = childNode;
            }
        }
        System.out.println("score: " + maxScoreNode.getScore() + ", move: " + maxScoreNode.getMove());
        System.out.println("best move data--------");
        System.out.println(dijkstra.getDetectivesDistance((((Move.SingleMove) maxScoreNode.getMove()).destination), getLocAsList(gameData.detectives)));
        // the best move in long run might be the worst move in short run
        // if the best move pick is one step away from detectives, choose another move that is the best without think further
        if(dijkstra.getDetectivesDistance((((Move.SingleMove) maxScoreNode.getMove()).destination), getLocAsList(gameData.detectives)).get(0) <= 1) {
            return moveGivesLongestDistance(moves, board);
        }
        return maxScoreNode.getMove();
    }
    //return a move that result the longest distance between detectives
    public Move moveGivesLongestDistance(List<Move> possibleMoves, @Nonnull Board board){
        Minimax.Info gameData = new Minimax.Info(getMrXPlayer(board, MRX),
                getDetectivePlayers(board, getAllDetectives(board)));
        Dijkstra dijkstra = new Dijkstra(board);
        int furthestMoveDistance = 0;
        Move bestMove = null;
        for (Move move : possibleMoves) {
            int currentDistance = dijkstra.getDetectivesDistance(getDestination(move),
                    getLocAsList(gameData.detectives)).get(0);
            if (currentDistance > furthestMoveDistance) {
                furthestMoveDistance = currentDistance;
                bestMove = move;
            }
        }
        return bestMove;
    }

    // get all detective pieces as Piece.Detective
    public List<Piece.Detective> getAllDetectives(@Nonnull Board board) {
        List<Piece> allDetectivePieces = new ArrayList<Piece>();
        allDetectivePieces.addAll(board.getPlayers());
        allDetectivePieces.remove("MRX");
        List<Piece.Detective> detectives = new ArrayList<>();
        for (Piece detective : allDetectivePieces) {
            if (detective.isDetective()) detectives.add((Piece.Detective) detective);
        }
        return detectives;
    }

    public Map<ScotlandYard.Ticket, Integer> getCurrentMrXTickets(@Nonnull Board board) {
        Map<ScotlandYard.Ticket, Integer> mrXTickets = new HashMap<>();
        for (ScotlandYard.Ticket t : ScotlandYard.Ticket.values())
            mrXTickets.put(t, board.getPlayerTickets(MRX).get().getCount(t));
        return mrXTickets;
    }

    public Map<ScotlandYard.Ticket, Integer> getCurrentDetectiveTickets(@Nonnull Board board, Piece.Detective detective) {
        Map<ScotlandYard.Ticket, Integer> detectiveTickets = new HashMap<>();
        for (ScotlandYard.Ticket t : ScotlandYard.Ticket.values())
            detectiveTickets.put(t, board.getPlayerTickets(detective).get().getCount(t));
        return detectiveTickets;
    }

    // get current locations of all detectives as a list
    public List<Integer> getLocAsList(List<Player> detectivesPlayer) {
        List<Integer> detectivesLoc = new ArrayList<>();
        detectivesPlayer.forEach(d -> detectivesLoc.add(d.location()));
        return detectivesLoc;
    }

    // make Mr X a player
    public Player getMrXPlayer(@Nonnull Board board, Piece mrX) {
        return new Player(mrX, ImmutableMap.copyOf(getCurrentMrXTickets(board)),
                board.getAvailableMoves().stream().iterator().next().source());
    }

    // make detectives players in a list
    public List<Player> getDetectivePlayers(@Nonnull Board board, List<Piece.Detective> detectives) {
        List<Player> detectivePlayers = new ArrayList<>();
        detectives.forEach(d ->  detectivePlayers.add(
                new Player(d,
                        ImmutableMap.copyOf(getCurrentDetectiveTickets(board, d)),
                        board.getDetectiveLocation(d).get())));
        return detectivePlayers;
    }

    // get the final destination of either a single move or double move
    public int getDestination(Move move) {
        int distance = move instanceof Move.SingleMove
                ? ((Move.SingleMove) move).destination
                : ((Move.DoubleMove) move).destination2;
        return distance;
    }
}