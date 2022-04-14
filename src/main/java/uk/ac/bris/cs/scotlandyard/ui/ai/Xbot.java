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

        //测试用，记得删掉--------------------------------------------------------------------
        System.out.println("all available moves: "+ board.getAvailableMoves());
        List dL = new ArrayList<>();
        dL.add(140);
        dL.add(5);
        Dijkstra d = new Dijkstra(42, dL, board);
        System.out.println(d.getDetectivesDistance());

        //---------------------------------------------------------------------------------

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

    //get mrX current location
    private int getMrXLoc(@Nonnull Board board){
        return board.getAvailableMoves().stream().iterator().next().source();
    }

    //decide which move will be farthest from the nearest detective(s)
    //and cast the distance into a mark
    private int distanceMark(@Nonnull Board board, Move move) {
        /* -------------------------------------------------------------------
            when is mrX's turn, get mrX's location from moves
            get detective locations from board
         */

        int distanceMark = 0;
        //get pieces of detectives
        List<Piece.Detective> detectives = getAllDetectives(board);
        //get detectives locations and store them in a list
        List<Integer> detectiveLocations = new ArrayList<>();
        for (Piece.Detective detectivePiece : detectives) {
            detectiveLocations.add(board.getDetectiveLocation(detectivePiece).get());
        }

        if((Boolean) getMoveInformation(move).get("isSingleMove")){ //singlemove的情况
            Dijkstra dijkstra = new Dijkstra((int)getMoveInformation(move).get("destination"),detectiveLocations,board);
            List<Integer> distance = dijkstra.getDetectivesDistance();
            for(Integer d: distance){
                distanceMark *= 10;
                distanceMark += d;
            }
            distanceMark /= (Math.pow(10,distance.size()));
            return distanceMark;
        }
        else{ //doublemove的情况

        }

        /* ----------------------------------------------------------
            if it's detectives' turn, get mrX location from log
            get detective locations from moves
         */


        return 0;
    }

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



    //到这个点的交通方式数量和相邻点个数的评分
    private int transportationScore (@Nonnull Board board, Move move){
        if((Boolean) getMoveInformation(move).get("isSingleMove")){ //singlemove的情况
            int destination = (int) getMoveInformation(move).get("destination");
            int adjacentNodeNum = board.getSetup().graph.adjacentNodes(destination).size();
            int transportTypeNum = board.getSetup().graph.edgeValueOrDefault(move.source(),destination,ImmutableSet.of()).size();
            return 9*adjacentNodeNum + transportTypeNum; //这个权重先这么设置
        }
        else{ //doublemove的情况

        }
        return 0;
    }

    //用这种交通方式能到几个点
    private int guessPossibilityScore(@Nonnull Board board, GameSetup setup, Move move){
        int possibleNode = 0;
        if((Boolean) getMoveInformation(move).get("isSingleMove")) { //singlemove的情况
            ScotlandYard.Ticket ticket = (ScotlandYard.Ticket) getMoveInformation(move).get("ticket");
            Set<Integer> allAdjacentNodes = board.getSetup().graph.adjacentNodes(getMrXLoc(board));
            if(ticket == ScotlandYard.Ticket.SECRET) possibleNode = allAdjacentNodes.size();
            else {
                //这个之后能改了改改
                for(Integer node: allAdjacentNodes){
                    for(ScotlandYard.Transport t: board.getSetup().graph.edgeValueOrDefault(move.source(),node,ImmutableSet.of())){
                        if (t.requiredTicket() == ticket) possibleNode++;
                    }
                }
            }
        }
        else { //doublemove的情况

        }
        return possibleNode;
    }


    /*
    o available moves和detectives的距离
    o 这个点有几种交通工具 都是哪些
    o 和这个点相连的点有几个
    o detectives还剩什么票
    o 用这种交通方式能到几个点（可能点个数）
    reveal
     */
    /*
        多数情况可能doublemove都比singlemove要好，但是double卡是有限的
        所以在离得远的时候就不用double卡 离得近时候（近到一定程度，再用double）

        给的都应该是destination，即可能到的位置 而不是mrX现在的位置
        visitor pattern的作用是知道用的是singlemove还是doublemove 这样就可以get所需要的位置

        先只处理single，即：在普通的时候只用singleMove，只有在特殊的时候才用doublemove

     */

    private int giveMark(@Nonnull Board board, Move move){
        //当reveal前一轮和后一轮时候改变权重
        //reveal前一轮 能到的点评分权重增加
        //reveal后一轮是交通方式 或者用secret卡
        //判断离reveal的轮数


        return 0;
    }

}
