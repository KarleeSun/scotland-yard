package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
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
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {

        // returns a random move, replace with your own implementation
        var moves = getMoves(board);
        distanceMark(board);
        System.out.println("------");
        return moves.get(new Random().nextInt(moves.size()));

//        return null;
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
        List<Piece> allDetectivePieces = new ArrayList<Piece>();
        allDetectivePieces.addAll(board.getPlayers());
        allDetectivePieces.remove("MRX");

        List<Piece.Detective> detectives = new ArrayList<>();
        for(Piece detective : allDetectivePieces){
            if(detective.isDetective()) detectives.add((Piece.Detective)detective);
        }

        //要删掉的东西
        System.out.println("pieces: " + allDetectivePieces);
        System.out.println("detectives: "+detectives);

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

        DijkstraMinHeap dp = new DijkstraMinHeap(mrXLoc,detectiveLocations,board);
        System.out.println("dixx return result: " + dp.getDetectivesDistance());

        return 0;
    }

    //交通工具和相邻点个数的评分
    private static int transportationScore(@Nonnull Board board){
        var moves = getMoves(board);
    private int transportationScore(@Nonnull Board board){
        if(!IsThisMrXTurn(board))
            return 0;
        ImmutableList<Move> moves = getMoves(board);
        for(Move move: moves){
            move.
        }
        return 0;
    }

    /*
    available moves和detectives的距离
    这个点有几种交通工具 都是哪些
    detectives还剩什么票
    和这个点相连的点有几个
    用这种交通方式能到几个点（可能点个数）
    reveal
     */
    private int giveMark(@Nonnull Board board){

        return 0;
    }

}


