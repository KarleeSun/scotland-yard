package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.*;
import io.atlassian.fugue.Pair;

import uk.ac.bris.cs.scotlandyard.model.*;


public class Xbot implements Ai {
    //成员变量及其初始化
    //这些可以考虑改成public 因为每个class都用了其实
    //而且这大概还能再简化吧..看着好丑
//    private static Board board; //这样在这个class就能直接用，不用每个函数都再传入一遍board
    private Piece.MrX MRX; //mrX的piece
    private List<Piece.Detective> detectives; //所有detective的pieces，存在一个list里
    private int mrXLoc; //记录mrX的位置和预测位置 相当于move的destination
    private List<Integer> detectivesLoc; //记录detectives的位置 按顺序存在一个list里 相当于是destination
    private int source; //相当于一个move的source，也许没必要，先写着
    //    private List<Integer> detectiveSources; //同上，存detectives的sources
    private Map<ScotlandYard.Ticket, Integer> mrXTickets; //存的是mrX的票
    private Map<ScotlandYard.Ticket, Integer> detectivesTickets; //存的是detective的票
    private Boolean useDouble; //有没有用double票
    private Boolean useSecret; //有没有用secret票
    private int turnNum; //记录一下轮数
//    private ScotlandYard.Ticket usedTicket; //存那个detective他用的票


    //name of this AI
    @Nonnull
    @Override
    public String name() {
        return "Xbot";
    }

    //pick the best move for mrX
    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        //double 和 secret 單獨處理
        //距離特別近的時候也用double和secret
        //reveal後一輪用double和secret
        //其餘時候用minimax
        //在Dijkstra裡面給不同的交通工具賦不同權重
        //只有离太近的时候用double，afterReveal用Secret
        //before reveal 不能用double
        MRX = Piece.MrX.MRX;
        Minimax minimax = new Minimax();
        Minimax.Info gameData = new Minimax.Info(getMrXPlayer(board,MRX),getDetectivePlayers(board,getAllDetectives(board)));
        Dijkstra dijkstra = new Dijkstra(board);
        Boolean tooClose = (dijkstra.getDetectivesDistance(gameData.mrX.location(),getLocAsList(gameData.detectives)).get(0) <= 1);
        System.out.println("too close? " + tooClose + "shortest distance: " + dijkstra.getDetectivesDistance(gameData.mrX.location(),getLocAsList(gameData.detectives)));
        Boolean hasTicket = gameData.mrX.has(ScotlandYard.Ticket.DOUBLE) || gameData.mrX.has(ScotlandYard.Ticket.SECRET);
        System.out.println("has ticket?" + hasTicket);
        if((tooClose) && hasTicket){
            System.out.println("situation 1");
            List<Move> moves = List.copyOf(board.getAvailableMoves().asList());
            if(tooClose && gameData.mrX.has(ScotlandYard.Ticket.DOUBLE)) {
                moves = board.getAvailableMoves().stream().filter(move -> move instanceof Move.DoubleMove)
                        .filter(m -> (((Move.DoubleMove) m).ticket1 == ScotlandYard.Ticket.SECRET || ((Move.DoubleMove) m).ticket2 == ScotlandYard.Ticket.SECRET)).toList();
            } else if(tooClose && !gameData.mrX.has(ScotlandYard.Ticket.DOUBLE) && gameData.mrX.has(ScotlandYard.Ticket.SECRET)) {
                moves.stream().filter(move -> move instanceof Move.SingleMove);
                moves.stream().filter(move -> {
                    List<ScotlandYard.Ticket> tickets = new ArrayList<>();
                    for (ScotlandYard.Ticket ticket : move.tickets())
                        tickets.add(ticket);
                    return tickets.contains(ScotlandYard.Ticket.SECRET);
                });
            }
            Move bestMove = null;
            int furthestMoveDistance = 0;
            for(Move move: moves){
                int currentDistance = dijkstra.getDetectivesDistance(getDestination(move), getLocAsList(gameData.detectives)).get(0);
                if(currentDistance > furthestMoveDistance){
                    furthestMoveDistance = currentDistance;
                    bestMove = move;
                }
            }
            System.out.println("move: " + bestMove);
            System.out.println(dijkstra.getDetectivesDistance(getDestination(bestMove), getLocAsList(gameData.detectives)));
            System.out.println("best move data--------");
            System.out.println(dijkstra.getDetectivesDistance(getDestination(bestMove), getLocAsList(gameData.detectives)));

            return bestMove;
            
//            Map<Move,Integer> movesWithDistances = new HashMap<>();
//            for(Move move: moves){
//                int distance = dijkstra.getDetectivesDistance(((Move.DoubleMove)move).destination2,getLocAsList(gameData.detectives)).get(0);
//                movesWithDistances.put(move,distance);
//            }
//            return movesWithDistances.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
        } else {
            System.out.println("situation 2");
            Minimax.TreeNode root = minimax.tree(board,3, gameData);
            Minimax.TreeNode maxScoreNode = root.getChildren().get(0);
            for(Minimax.TreeNode childNode : root.getChildren()){
                if(childNode.getScore() > maxScoreNode.getScore()){
                    System.out.println("child node score: " + childNode.getScore());
                    System.out.println("max score: " + childNode.getScore());
                    maxScoreNode = childNode;
                }
            }
            System.out.println("score: " + maxScoreNode.getScore() + ", move: " + maxScoreNode.getMove());
            System.out.println("best move data--------");
            System.out.println(dijkstra.getDetectivesDistance((((Move.SingleMove)maxScoreNode.getMove()).destination), getLocAsList(gameData.detectives)));

            return maxScoreNode.getMove();
        }
    }

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


    public List<Integer> getLocAsList(List<Player> detectivesPlayer){
        List<Integer> detectivesLoc = new ArrayList<>();
        for(Player d: detectivesPlayer){
            detectivesLoc.add(d.location());
        }
        return detectivesLoc;
    }


    public Player getMrXPlayer(@Nonnull Board board, Piece mrX){
        return new Player(mrX, ImmutableMap.copyOf(getCurrentMrXTickets(board)), board.getAvailableMoves().stream().iterator().next().source());
    }

    public List<Player> getDetectivePlayers(@Nonnull Board board, List<Piece.Detective> detectives){
        List<Player> detectivePlayers = new ArrayList<>();
        for(Piece.Detective d : detectives){
            Player detective = new Player(d,ImmutableMap.copyOf(getCurrentDetectiveTickets(board,d)),board.getDetectiveLocation(d).get());
            detectivePlayers.add(detective);
        }
        return detectivePlayers;
    }

    public int getDestination(Move move){
        int distance = move instanceof Move.SingleMove
                ? ((Move.SingleMove)move).destination : ((Move.DoubleMove)move).destination2;
        return distance;
    }

}