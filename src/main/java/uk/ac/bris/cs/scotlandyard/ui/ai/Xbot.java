package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.*;
import io.atlassian.fugue.Pair;

import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.ui.ai.Minimax.MAX;
import static uk.ac.bris.cs.scotlandyard.ui.ai.Minimax.MIN;

/*
    是要给每个选择一个分数
    那就直接重写getAvailableMoves
    用的时候判断 想走的move是不是在getAvailableMoves获得的moves里面
    如果在的话就选择那个move
    然后对现在的局面有一个评分标准
    之后minimax就是看分的
 */

/*
    这个事情是这样的，其实不是给它每一轮的分数，是给一个几轮之后的分数（几轮就是depth）
    然后再倒着找 看导致这个最优结果的是哪一个move
 */

/*
    问题：mrX能用secret和double时候moves里面没有
    predictDetectives排列组合有问题
 */

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
        MRX = Piece.MrX.MRX;
        Minimax minimax = new Minimax();
        Minimax.Info gameData = new Minimax.Info(getMrXPlayer(board,MRX),getDetectivePlayers(board,getAllDetectives(board)));
        Minimax.TreeNode root = minimax.tree(board,3, gameData);
        Minimax.TreeNode maxScoreNode = root.getChildren().get(0);
        for(Minimax.TreeNode ChildNode : root.getChildren()){
            if(ChildNode.getScore() > maxScoreNode.getScore()){
                maxScoreNode = ChildNode;
            }
        }
        return maxScoreNode.getMove();
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

}