package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.sun.source.tree.Tree;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class Minimax {
    private static int MIN = -100000;
    private static int MAX = 100000;
    private int mrXLoc;
    private List<Integer> detectivesLoc;
    private int turnNum; //记录轮数，记得每次走完更新
    private Boolean useDouble; //用没用double卡（singlemove or doublemove）
    private Boolean useSecret;
    private Map<ScotlandYard.Ticket, Integer> mrXTickets;
    private Map<ScotlandYard.Ticket, Integer> detectivesTickets;
    //对于每一个结点他就存了这些值，可以设置成如果不传入就是默认，或者就传入吧也累不死
    //刚开始创建这个node时候就可以传初始值，然后在node里面计算时候按需更改
    private class TreeNode {
        private Move move;
        private int score;
        private TreeNode parent;
        private List<TreeNode> children;
        private int mrXLoc;
        private List<Integer> detectivesLoc;
        private int turnNum; //记录轮数，记得每次走完更新
        private Boolean useDouble; //用没用double卡（singlemove or doublemove）
        private Boolean useSecret;
        private Map<ScotlandYard.Ticket, Integer> mrXTickets;
        private Map<ScotlandYard.Ticket, Integer> detectivesTickets;


        private TreeNode(Move move, int score, int alpha, int beta, int mrXLoc, List<Integer> detectivesLoc, int turnNum,
                         Boolean useDouble, Boolean useSecret, Map<ScotlandYard.Ticket, Integer> mrXTickets,
                         Map<ScotlandYard.Ticket, Integer> detectivesTickets) {
            this.move = move;
            this.score = score;
//            this.source = source; //移动前的位置 不需要其实，因为他的出发位置就是他爹的到达位置
            this.mrXLoc = mrXLoc; //移动后mrX的位置
            this.detectivesLoc = detectivesLoc; //移动后detectives的位置
            this.turnNum = turnNum; //记录一下轮数
            this.useDouble = useDouble; //有没有用double卡
            this.useSecret = useSecret; //有没有用secret卡
            this.mrXTickets = mrXTickets; //存的是mrX的票
            this.detectivesTickets = detectivesTickets; //存的是detective的票
        }

        public void addChild(TreeNode child) {
            child.setParent(this);
            this.children.add(child);
        }

        public void addChild(Move move, int score, int alpha, int beta, int mrXLoc, List<Integer> detectivesLoc,
                              Boolean useDouble, Boolean useSecret) {
            TreeNode child = new TreeNode(move, score, alpha, beta, mrXLoc, detectivesLoc, turnNum,
                    useDouble, useSecret, mrXTickets, detectivesTickets);
            this.children.add(child);
        }

        public void setParent(TreeNode parent) { this.parent = parent; }

        public void remove(TreeNode node) { node.parent.children.remove(node); }

        public Move getMove() { return move; }

        public TreeNode getParent() { return parent; }

        public List<TreeNode> getChildren() { return children; }

        public TreeNode getLastNode(TreeNode node){
            if(!node.children.isEmpty()){
                getLastNode(node.children.get(0));
            }
            return node.children.get(0);
        }
    }

    //先要自己构建出一个tree
    //大问题：useDouble和useSecret的更新
    //还有一个事就是doublemove和secret要在轮数结束之前都用完
    //doublemove两轮就只让他用一个secret吧 要用就是其中的第一轮用
    //注意location和牌堆的更新
    //更新：turnNum, mrXLoc, detectivesLoc, mrXTickets, detectivesTickets, useSecret, useDouble

    //好了这就创建好了要用的tree
    private void tree(@Nonnull Board board, Move move, int score, int depth, int alpha, int beta, int mrXLoc,
                      List<Integer> detectivesLoc, int turnNum, Boolean useDouble, Boolean useSecret,
                      Map<ScotlandYard.Ticket, Integer> mrXTickets,Map<ScotlandYard.Ticket, Integer> detectivesTickets) {
        Xbot xbot = new Xbot();
        TreeNode root = new TreeNode(null, score MIN,MAX,mrXLoc,detectivesLoc,turnNum,
                useDouble,useSecret, mrXTickets, detectivesTickets);
        createTree(board, root, depth, score, alpha, beta, mrXLoc, detectivesLoc, useDouble,useSecret, mrXTickets, detectivesTickets);
    }

    //递归 就循环着创这个树
    //注意记录深度和轮数
    private void createTree(@Nonnull Board board, TreeNode node, int depth, int score, int alpha, int beta, int mrXLoc,
                            List<Integer> detectivesLoc, int turnNum, Boolean useDouble, Boolean useSecret,
                            Map<ScotlandYard.Ticket, Integer> mrXTickets, Map<ScotlandYard.Ticket, Integer> detectivesTickets){
        //
        Xbot xbot = new Xbot();
        if(!xbot.hasWinner(board,mrXLoc,detectivesLoc)){
            for(Move m: xbot.predictMrXMoves(board, mrXLoc, detectivesLoc)){
                //用一个visitor pattern把singlemove和doublemove分开考虑
                Boolean  getMoveSuccess= m.accept(new Move.Visitor<Boolean>(){
                    Boolean getMoveSuccess;
                    @Override
                    public Boolean visit(Move.SingleMove singleMove){
                        //在这里更新该更新的东西
                        turnNum ++;


                        return getMoveSuccess;
                    }
                    @Override
                    public Boolean visit(Move.DoubleMove doubleMove){
                        return getMoveSuccess;
                    }
                });
            }

        }
    }
    //最终把最好的move传给root存到root的move里
    public TreeNode miniMaxAlphaBeta(TreeNode node, int depth, Boolean maximizing, int alpha, int beta){
        if(node.children.isEmpty()){
            return node;
        }
        TreeNode bestScoreNode = node;
        if(maximizing){
            bestScoreNode.score = MIN;
            for(int i = 0; i < node.parent.children.size(); i ++){
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, false, alpha, beta);
                if(bestScoreNode.score <= scoreNode.score){
                    bestScoreNode = scoreNode;
                }
                alpha = Math.max(alpha, bestScoreNode.score));
                if(beta <= alpha)
                    break;
            }
            return bestScoreNode;
        }
        else{
            bestScoreNode.score = MAX;
            for(int i = 0; i < node.parent.children.size(); i ++){
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, true, alpha, beta);
                if(bestScoreNode.score >= scoreNode.score){
                    bestScoreNode = scoreNode
                }
                beta = Math.min(beta, bestScoreNode.score);
                if(beta <= alpha)
                    break;
            }
        }
        return bestScoreNode;
    }
}

//        //这用不用把single和double分开
//        //还是分开吧，也累不死
//        for(Move m: xbot.predictMrXMoves(board, mrXLoc, detectivesLoc)){
//            //在这里更新该更新的东西
//            mrXLoc = (int)xbot.getMoveInformation(m).get("destination");
//            //更新一下票
//
//            TreeNode mrXAvailableMoves = new TreeNode(m, MIN, MAX, mrXLoc,detectivesLoc,
//                    (Boolean) xbot.getMoveInformation(m).get("isDoubleMove"), (Boolean) xbot.getMoveInformation(m).get("useSecret"));
//            root.addChild(mrXAvailableMoves);
//            xbot.predictDetectiveMoves(board,mrXLoc,detectivesLoc);
//
//        }