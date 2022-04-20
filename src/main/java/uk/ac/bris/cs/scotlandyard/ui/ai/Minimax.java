package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;

/*
    detectivesLoc有大问题
 */

public class Minimax {
    public static int MIN = -100000;
    public static int MAX = 100000;
    public static Piece.MrX mrX = Piece.MrX.MRX;
    private int mrXLoc;
    private List<Integer> detectivesLoc;
    private int turnNum; //记录轮数，记得每次走完更新
    private Boolean useDouble; //用没用double卡（singlemove or doublemove）
    private Boolean useSecret;
    private Map<ScotlandYard.Ticket, Integer> mrXTickets;
    private Map<ScotlandYard.Ticket, Integer> detectivesTickets;

    //对于每一个结点他就存了这些值，可以设置成如果不传入就是默认，或者就传入吧也累不死
    //刚开始创建这个node时候就可以传初始值，然后在node里面计算时候按需更改
    public class TreeNode {
        private Move move;
        private int score;
        private int alpha = MIN;
        private int beta = MAX;
        private TreeNode parent;
        private List<TreeNode> children;
        //        private int turnNum; //记录轮数，记得每次走完更新
        private Boolean useDouble; //用没用double卡（singlemove or doublemove）
        private Boolean useSecret;
        private GameData gameData;


        public TreeNode(Move move, int score, Boolean useDouble, Boolean useSecret, GameData gameData) {
            this.move = move;
            this.score = score;
            this.alpha = MIN;
            this.beta = MAX;
            this.children = new ArrayList<>();
//            this.source = source; //移动前的位置 不需要其实，因为他的出发位置就是他爹的到达位置
//            this.turnNum = turnNum; //记录一下轮数
            this.useDouble = useDouble; //有没有用double卡
            this.useSecret = useSecret; //有没有用secret卡
            this.gameData = gameData;
//            this.parent = new TreeNode(null,0,MIN,MAX,0,null,0,false,false,null,null);
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
            if (child.parent == null) child.setParent(this);
        }

        public void addChild(Move move, int score, Boolean useDouble, Boolean useSecret, GameData gameData) {
            TreeNode child = new TreeNode(move, score, useDouble, useSecret, gameData);
            this.children.add(child);
            if (!child.parent.equals(this)) child.setParent(this);
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
            if (!parent.children.contains(this)) parent.addChild(this);
        }

        public Move getMove() {
            return move;
        }

        public int getScore() {
            return score;
        }

        public int getBeta() {
            return beta;
        }

        public int getAlpha() {
            return alpha;
        }

        public TreeNode getParent() {
            return parent;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

    }

    //xPlayer是Player类型的mrX
    //detectivesPlayer: Player类型的detectives
    public TreeNode tree(@Nonnull Board board, int depth, GameData gameData) {
        Map mrXTickets = new HashMap();
        for (ScotlandYard.Ticket t : ScotlandYard.Ticket.values()) {
            mrXTickets.put(t, board.getPlayerTickets(mrX).get().getCount(t));
        }
        TreeNode root = new TreeNode(null, 0, false, false, gameData);
        int depthHere = depth;
        createTree(board,root,depthHere,gameData);
        return root;
    }

    public void createTree(@Nonnull Board board, TreeNode node, int depth, GameData gameData) {
        Xbot xbot = new Xbot();
        if(depth > 0){
            depth -= 1 ;
            for (Move m : board.getAvailableMoves()) {
                Player updateMrX = gameData.mrX.at((int) xbot.getMoveInformation(m).get("destination")).use(m.tickets());
                GameData newGameData = new GameData(updateMrX, gameData.detectives.stream().toList());
                Score score = new Score();
                int s = score.giveScore(board, newGameData.mrX.location(), xbot.getLocAsList(newGameData.detectives),m.source(),m.tickets().iterator().next());
                TreeNode mrXNode = new TreeNode(m, s, (Boolean) xbot.getMoveInformation(m).get("useDouble"), (Boolean) xbot.getMoveInformation(m).get("useSecret"), newGameData);
                mrXNode.setParent(node);

                List<List<Player>> allUpdatedDetectives = new ArrayList<>();
                List<Player> updatedOneDetective = new ArrayList<>();
                for(Player detective : newGameData.detectives){ //对于每一个detective
                    List<Move.SingleMove> oneDetectiveMove = xbot.makeSingleMoves(board.getSetup(),newGameData.detectives,detective,detective.location());
                    for(Move.SingleMove singleMove: oneDetectiveMove){
                        Player updateDetective = detective.at(singleMove.destination).use(singleMove.ticket).give(singleMove.ticket);
                        updatedOneDetective.add(updateDetective);
                    }
                    allUpdatedDetectives.add(updatedOneDetective);
                }
                List<List<Player>> result = Lists.cartesianProduct(allUpdatedDetectives);
                for(List<Player> detectivesMoveOnce: result){
                    GameData updateGameData = new GameData(newGameData.mrX,detectivesMoveOnce);
                    Score score1 = new Score();
                    int s1 = score1.giveScore(board, updateGameData.mrX.location(), xbot.getLocAsList(updateGameData.detectives),m.source(),m.tickets().iterator().next());
                    TreeNode detectivesNode = new TreeNode(mrXNode.move,s1,(Boolean) xbot.getMoveInformation(m).get("useDouble"), (Boolean) xbot.getMoveInformation(m).get("useSecret"),updateGameData);
                    detectivesNode.setParent(mrXNode);
                    createTree(board,detectivesNode,depth,updateGameData);
                }
            }
        }
    }




    //最终把最好的move传给root存到root的move里
    public TreeNode miniMaxAlphaBeta(TreeNode node, int depth, Boolean maximizing, int alpha, int beta) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!node : " + node.getMove());
        if (node.children.isEmpty()) {
            return node;
        }
        TreeNode bestScoreNode = node;
        if (maximizing) {
            bestScoreNode.score = MIN;
            for (int i = 0; i < node.children.size(); i++) {
                System.out.println("size: " + node.children.size());
                System.out.println("i: " + i);
                System.out.println("children: " + node.children.get(i));
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, false, alpha, beta);
                if (bestScoreNode.score <= scoreNode.score) {
                    bestScoreNode = scoreNode;
                }
                alpha = Math.max(alpha, bestScoreNode.score);
                node.alpha = alpha;
                System.out.println("alpha: " + node.getAlpha());
                System.out.println("beta: " + node.getBeta());
                System.out.println("score: " + node.getScore());
                if (beta <= alpha)
                    break;
                System.out.println("best move: " + bestScoreNode.getMove());
            }
            return bestScoreNode;
        } else {
            bestScoreNode.score = MAX;
            for (int i = 0; i < node.children.size(); i++) {
                System.out.println("size: " + node.children.size());
                System.out.println("i: " + i);
                System.out.println("children: " + node.children.get(i));
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, true, alpha, beta);
                if (bestScoreNode.score >= scoreNode.score) {
                    bestScoreNode = scoreNode;
                }
                beta = Math.min(beta, bestScoreNode.score);
                node.beta = beta;
                System.out.println("alpha: " + node.getAlpha());
                System.out.println("beta: " + node.getBeta());
                System.out.println("score: " + node.getScore());

                if (beta <= alpha)
                    break;
                System.out.println("best move: " + bestScoreNode.getMove());
            }
        }
        return bestScoreNode;
    }


    public static class GameData {
        final Player mrX;
        final ImmutableList<Player> detectives;

        public GameData(Player mrX, List<Player> detectives) {
            this.mrX = mrX;
            this.detectives = ImmutableList.copyOf(detectives);
        }
    }

}
