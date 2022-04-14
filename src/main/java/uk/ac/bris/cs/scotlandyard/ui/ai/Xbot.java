package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.atlassian.fugue.Pair;

import uk.ac.bris.cs.scotlandyard.model.*;

/*
    是要给每个选择一个分数
    那就直接重写getAvailableMoves
    用的时候判断 想走的move是不是在getAvailableMoves获得的moves里面
    如果在的话就选择那个move
    然后对现在的局面有一个评分标准
    之后minimax就是看分的
 */

public class Xbot implements Ai {
    //成员变量及其初始化
//    private static Board board = b; //这样在这个class就能直接用，不用每个函数都再传入一遍board
    private Piece.MrX MRX = Piece.MrX.MRX; //mrX的piece
    private List<Piece.Detective> detectives; //所有detective的pieces，存在一个list里
    private int mrXLoc; //记录mrX的位置和预测位置
    private List<Integer> detectivesLoc; //记录detectives的位置 按顺序存在一个list里
    private Map<ScotlandYard.Ticket, Integer> mrXTickets; //存的是mrX的票
    private Map<ScotlandYard.Ticket, Integer> detectivesTickets; //存的是detective的票
    private ScotlandYard.Ticket usedTicket; //用了什么票也记一下

    //name of this AI
    @Nonnull @Override
    public String name() {
        return "Xbot";
    }

    //pick the best move for mrX
    @Nonnull @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        detectives = getAllDetectives(board);
        mrXLoc = getCurrentMrXLoc(board);
        detectivesLoc = getCurrentDetectivesLoc(board);
        mrXTickets = getCurrentMrXTickets(board);
        detectivesTickets = getCurrentDetectiveTickets(board);
        List l = new ArrayList<>();
        l.add(4);
        l.add(93);
        Dijkstra d = new Dijkstra(33,l, board);
        System.out.println(d.getDetectivesDistance());
        //测试用，记得删掉--------------------------------------------------------------------
        System.out.println("all available moves: "+ board.getAvailableMoves());

        //---------------------------------------------------------------------------------

        Minimax minimax = new Minimax();



        //new一个map 把Move和对应的分数放进去
//        Map<Move,Integer> moveWithMark = new HashMap<>();
//        for(Move move: board.getAvailableMoves().asList()){
//            moveWithMark.put(move,giveMark(board,move));
//        }
////        System.out.println("moveWithMark: "+ moveWithMark);
//        //比较哪一个分最高，选分最高的那个move
//        List<Map.Entry<Move,Integer>> listMark = new ArrayList<>(moveWithMark.entrySet());
//        Collections.sort(listMark, (o1,o2) -> (o2.getValue() - o1.getValue())); //降序排列
//        Move pickedMove = listMark.get(0).getKey();

//        //测试用
//        System.out.println("listMark: " + listMark);
//        System.out.println("pickedMove: " + pickedMove);

//        return pickedMove;
        return null;
    }

    //get mrX current location
    private int getCurrentMrXLoc(@Nonnull Board board){
        return board.getAvailableMoves().stream().iterator().next().source();
    }

    //get detectives location and return as a list
    private List<Integer> getCurrentDetectivesLoc(@Nonnull Board board){

        //get pieces of detectives
        List<Piece.Detective> detectives = getAllDetectives(board);
        //get detectives locations and store them in a list
        List<Integer> detectiveLocations = new ArrayList<>();
        for (Piece.Detective detectivePiece : detectives) {
            detectiveLocations.add(board.getDetectiveLocation(detectivePiece).get());
        }
        return detectiveLocations;
    }

    private Map<ScotlandYard.Ticket, Integer> getCurrentMrXTickets(@Nonnull Board board){
        Map<ScotlandYard.Ticket, Integer> mrXTickets = new HashMap<>();
        for(ScotlandYard.Ticket t: ScotlandYard.Ticket.values())
            mrXTickets.put(t,board.getPlayerTickets(MRX).get().getCount(t));
        return mrXTickets;
    }

    private Map<ScotlandYard.Ticket, Integer> getCurrentDetectiveTickets(@Nonnull Board board){
        for(Piece.Detective d: detectives){
            for(ScotlandYard.Ticket t: ScotlandYard.Ticket.values())
                mrXTickets.put(t,board.getPlayerTickets(d).get().getCount(t));
        }
        return mrXTickets;
    }

    //得到所有存着detectives的piece
    public List<Piece.Detective> getAllDetectives(@Nonnull Board board){
        List<Piece> allDetectivePieces = new ArrayList<Piece>();
        allDetectivePieces.addAll(board.getPlayers());
        allDetectivePieces.remove("MRX");

        List<Piece.Detective> detectives = new ArrayList<>();
        for(Piece detective : allDetectivePieces){
            if(detective.isDetective()) detectives.add((Piece.Detective)detective);
        }
        return detectives;
    }

    //------------------------------------------------------------------------------------------------------------------

    private Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives,
                                                        Player player, int source) {
        Set<Move.SingleMove> possibleMoves = Sets.newHashSet();
        Set<Integer> destination = setup.graph.adjacentNodes(source);
        for (int d : destination) {
            boolean notOccupied = true;          /* node not occupied by detectives*/
            for (Player detective : detectives) {
                if (detective.location() == d) {
                    notOccupied = false;
                    break;
                }
            }
            //iterate through set containing all possible transportation from source to destination
            if (notOccupied) {
                if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1)){          /* with SECRET MOVES */
                    possibleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, d));
                }
                for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, d, ImmutableSet.of())) {
                    if (player.hasAtLeast(t.requiredTicket(), 1)) {
                        possibleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), d));
//                        if(player.isMrX()) mrXLoc = d;
//                        if(player.isDetective()) detectivesLoc
                    }
                }
            }
        }
        return possibleMoves;
    }
    private Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup,
                                                        List<Player> detectives,
                                                        Player player,
                                                        int source) {
        if(!player.hasAtLeast(ScotlandYard.Ticket.DOUBLE,1)) return new HashSet<>();
        Set<Move.DoubleMove> doubleAvailableMoves = new HashSet<>();
        //store all available first move by invoke makeSingleMove method
        List<Move.SingleMove> firstAvailableMoves = new ArrayList<>(makeSingleMoves(setup, detectives, player, source));
        //iterate through all possible single moves and store its corresponding second move
        for (Move.SingleMove firstMove : firstAvailableMoves){
            /* store second available moves*/
            List<Move.SingleMove> secondAvailableMoves = new ArrayList<>(
                    makeSingleMoves(setup, detectives, player, firstMove.destination));
            //iterate through all possible second move for a particular first move and create new double move and store it
            ScotlandYard.Ticket ticketUsed = firstMove.ticket;
            Integer ticketLeft = player.tickets().get(ticketUsed);
            if (!secondAvailableMoves.isEmpty()) {
                for (Move.SingleMove secondMove : secondAvailableMoves) {
                    if (!(secondMove.ticket == ticketUsed) || ticketLeft >= 2) {
                        doubleAvailableMoves.add( new Move.DoubleMove(player.piece(), firstMove.source(),
                                firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
                    }
                }
            }
        }
        return doubleAvailableMoves;
    }
    //------------------------------------------------------------------------------------------------------------------


    //给一个位置（source）返回它所有可能的move
    //其实可以更新mrX和detectives的位置
    //对于每一个位置 找他的adjacencyNodes 然后判断它有没有票
    //在普通轮double票就特别贵 在detectives离得特别近时候、还有reveal之后，double票就变得便宜
    private List<Integer> predictDetectiveMoves(@Nonnull Board board,int mrXLoc, List<Integer> detectivesLoc){
        List<Integer> updatedDetectivesLoc = new ArrayList<>();
        //然后每个detective都走到了距离-1的地方
        List<Integer> detectiveLoc = new ArrayList<>();
        for(Integer dLoc: detectivesLoc){
            Dijkstra dj = new Dijkstra(mrXLoc, dLoc, board);
            int distance = dj.getDistance();
            for (int d: board.getSetup().graph.adjacentNodes(dLoc)){
                Dijkstra dk = new Dijkstra(mrXLoc, d, board);
                if(!detectivesLoc.contains(d) && dk.getDistance() == distance-1){
                    //这写的大概有一点问题
                    for(ScotlandYard.Transport t : board.getSetup().graph.edgeValueOrDefault(dLoc,d,ImmutableSet.of())){
                        if(detectivesTickets.get(t.requiredTicket()) >= 1){
                            detectivesTickets.remove(t.requiredTicket());
                            mrXTickets.put(t.requiredTicket(), mrXTickets.get(t.requiredTicket())+1);
                            continue;
                        }
                    }
                    updatedDetectivesLoc.add(d);
                    continue;
                }
            }
        }
        return updatedDetectivesLoc;
    }


    /*
        多数情况可能doublemove都比singlemove要好，但是double卡是有限的
        所以在离得远的时候就不用double卡 离得近时候（近到一定程度，再用double）

        给的都应该是destination，即可能到的位置 而不是mrX现在的位置
        visitor pattern的作用是知道用的是singlemove还是doublemove 这样就可以get所需要的位置

        先只处理single，即：在普通的时候只用singleMove，只有在特殊的时候才用doublemove

     */

    //这个函数用来得到我想得到的全部信息
    private Map<String, Object> getMoveInformation(Move move){
        Map<String,Object> moveInfoMap = move.accept(new Move.Visitor<Map<String,Object>>(){
            Map<String, Object> moveInfo;
            @Override
            public Map<String,Object> visit(Move.SingleMove singleMove){
                //moveInfo = Map.of()可以将多个元素一次性添加进去
                moveInfo.put("isSingleMove",true);
                moveInfo.put("piece",singleMove.commencedBy());
                moveInfo.put("source",singleMove.source());
                moveInfo.put("ticket",singleMove.ticket);
                moveInfo.put("destination",singleMove.destination);
                return moveInfo;
            }
            @Override
            public Map<String,Object> visit(Move.DoubleMove doubleMove){
                moveInfo.put("isSingleMove",false);
                moveInfo.put("piece",doubleMove.commencedBy());
                moveInfo.put("source",doubleMove.source());
                moveInfo.put("ticket1",doubleMove.ticket1);
                moveInfo.put("destination1",doubleMove.destination1);
                moveInfo.put("ticket2",doubleMove.ticket2);
                moveInfo.put("destination2",doubleMove.destination2);
                return moveInfo;
            }
        });
        return null;
    }

    //把mrX现在所有的available moves和其对应分数存入一个map
    private Map<Move,Integer> movesWithScore(@Nonnull Board board, Move move){
        Map<Move,Integer> moveWithScore = new HashMap<>();
        ImmutableSet<Move> moves = board.getAvailableMoves();
        for(Move availableMove: moves){
            moveWithScore.put(availableMove, 0);
        }
        return moveWithScore;
    }
}
