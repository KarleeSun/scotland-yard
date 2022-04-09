package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;

import uk.ac.bris.cs.scotlandyard.model.*;


public class Xbot implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "Xbot";
    }


    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        List l1 = new ArrayList<>();
        l1.add(25);
        l1.add(52);
        Dijkstra d1 = new Dijkstra(67, l1, board);
        System.out.println("detective distances: " + d1.getDetectivesDistance());
        //new一个map 把Move和对应的分数放进去
        Map<Move,Integer> moveWithMark = new HashMap<>();
        for(Move move: board.getAvailableMoves().asList()){
            moveWithMark.put(move,giveMark(board,move));
        }
//        System.out.println("moveWithMark: "+ moveWithMark);
        //比较哪一个分最高，选分最高的那个move
        List<Map.Entry<Move,Integer>> listMark = new ArrayList<>(moveWithMark.entrySet());
        Collections.sort(listMark, (o1,o2) -> (o2.getValue() - o1.getValue())); //降序排列
        Move pickedMove = listMark.get(0).getKey();

//        //测试用
//        System.out.println("listMark: " + listMark);
//        System.out.println("pickedMove: " + pickedMove);

        return pickedMove;
    }


    private Boolean IsThisMrXTurn(@Nonnull Board board){
        if(board.getAvailableMoves().iterator().next().commencedBy().isMrX())
            return true;
        else return false;
    }

    //get all available moves and return as a list
    private ImmutableList<Move> getMoves(@Nonnull Board board) {
        return board.getAvailableMoves().asList();
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


    //decide which move will be farthest from the nearest detective(s)
    //and cast the distance into a mark
    private int distanceMark(@Nonnull Board board) {

        /* -------------------------------------------------------------------
            when is mrX's turn, get mrX's location from moves
            get detective locations from board
         */

        //get mrX location
        int mrXLoc = board.getAvailableMoves().stream().iterator().next().source();

        System.out.println("mrX loc: "+mrXLoc);

        //get pieces of detectives
        List<Piece.Detective> detectives = getAllDetectives(board);

        //get detectives locations and store them in a list
        List<Integer> detectiveLocations = new ArrayList<>();

        for (Piece.Detective detectivePiece : detectives) {
            detectiveLocations.add(board.getDetectiveLocation(detectivePiece).get());
        }

        System.out.println("detective locs: "+detectiveLocations);


        /* ----------------------------------------------------------
            if it's detectives' turn, get mrX location from log
            get detective locations from moves
         */

//        DijkstraMinHeap dp = new DijkstraMinHeap(mrXLoc,detectiveLocations,board);
//        System.out.println("dixx return result: " + dp.getDetectivesDistance());

        return 0;
    }

    //交通工具和相邻点个数的评分
    private int transportationScore(@Nonnull Board board){
//        if(!IsThisMrXTurn(board))
//            return 0;
        ImmutableList<Move> moves = getMoves(board);
        for(Move move: moves){

        }
        return 0;
    }

    //用这种交通方式能到几个点
    private int guessPossibilityScore(@Nonnull Board board, GameSetup setup){
        int mrXLoc = board.getAvailableMoves().stream().iterator().next().source();
//        ScotlandYard.Ticket ticket = board.getAvailableMoves().stream().iterator().next().tickets().iterator().next();
        return 0;
    }


    /*
    o available moves和detectives的距离
    o 这个点有几种交通工具 都是哪些
    o 和这个点相连的点有几个
    detectives还剩什么票
    o 用这种交通方式能到几个点（可能点个数）
    reveal
     */
    private int giveMark(@Nonnull Board board, Move move){
        //当reveal前一轮和后一轮时候改变权重
        //判断离reveal的轮数

        return 0;
    }

}
