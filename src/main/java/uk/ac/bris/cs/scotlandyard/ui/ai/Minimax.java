package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        public Move move;
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


        public TreeNode(Move move, int score, int alpha, int beta, int mrXLoc, List<Integer> detectivesLoc, int turnNum,
                         Boolean useDouble, Boolean useSecret, Map<ScotlandYard.Ticket, Integer> mrXTickets,
                         Map<ScotlandYard.Ticket, Integer> detectivesTickets) {
            this.move = move;
            this.score = score;
            this.children = new ArrayList<>();
//            this.source = source; //移动前的位置 不需要其实，因为他的出发位置就是他爹的到达位置
            this.mrXLoc = mrXLoc; //移动后mrX的位置
            this.detectivesLoc = detectivesLoc; //移动后detectives的位置
            this.turnNum = turnNum; //记录一下轮数
            this.useDouble = useDouble; //有没有用double卡
            this.useSecret = useSecret; //有没有用secret卡
            this.mrXTickets = mrXTickets; //存的是mrX的票
            this.detectivesTickets = detectivesTickets; //存的是detective的票
        }

        private void addChild(TreeNode child) {
            this.children.add(child);
            if(!child.parent.equals(this)) child.setParent(this);
        }

        private void addChild(Move move, int score, int alpha, int beta, int mrXLoc, List<Integer> detectivesLoc,
                              Boolean useDouble, Boolean useSecret) {
            TreeNode child = new TreeNode(move, score, alpha, beta, mrXLoc, detectivesLoc, turnNum,
                    useDouble, useSecret, mrXTickets, detectivesTickets);
            this.children.add(child);
            if(!child.parent.equals(this)) child.setParent(this);
        }

        private void setParent(TreeNode parent) {
            this.parent = parent;
            if(!parent.children.contains(this)) parent.addChild(this);
        }

        private Move getMove() { return move; }

        private TreeNode getParent() { return parent; }

        private List<TreeNode> getChildren() { return children; }

    }

    //先要自己构建出一个tree
    //大问题：useDouble和useSecret的更新
    //还有一个事就是doublemove和secret要在轮数结束之前都用完
    //doublemove两轮就只让他用一个secret吧 要用就是其中的第一轮用
    //注意location和牌堆的更新
    //更新：turnNum, mrXLoc, detectivesLoc, mrXTickets, detectivesTickets, useSecret, useDouble

    //好了这就创建好了要用的tree
    public TreeNode tree(@Nonnull Board board, Move move, int score, int depth, int alpha, int beta, int mrXLoc,
                      List<Integer> detectivesLoc, int turnNum, Boolean useDouble, Boolean useSecret,
                      Map<ScotlandYard.Ticket, Integer> mrXTickets,Map<ScotlandYard.Ticket, Integer> detectivesTickets) {
        System.out.println("here: "+mrXTickets);
        Xbot xbot = new Xbot();
        System.out.println("here1: "+mrXTickets);
        TreeNode root = new TreeNode(null, score, MIN,MAX,mrXLoc,detectivesLoc,turnNum,
                useDouble,useSecret, mrXTickets, detectivesTickets);
        System.out.println("here2: "+mrXTickets);
        createTree(board, root, depth, score, alpha, beta, mrXLoc, detectivesLoc,turnNum, useDouble, useSecret, mrXTickets, detectivesTickets);
        System.out.println("here3: "+mrXTickets);
        return root;
    }

    //递归 就循环着创这个树
    //注意记录深度和轮数
    int d = 0;
    private void createTree(@Nonnull Board board, TreeNode node, int depth, int score, int alpha, int beta, int mrXLoc,
                            List<Integer> detectivesLoc, int turnNum, Boolean useDouble, Boolean useSecret,
                            Map<ScotlandYard.Ticket, Integer> mrXTickets, Map<ScotlandYard.Ticket, Integer> detectivesTickets){
        //
        Xbot xbot = new Xbot();
        System.out.println("creatTree: "+mrXTickets);
        System.out.println("hasWinner: "+!xbot.hasWinner(board,mrXLoc,detectivesLoc));
        if(!xbot.hasWinner(board,mrXLoc,detectivesLoc) && d <= depth){ //如果游戏没有结束
            System.out.println("00000000");
            d++;
            for(Move m: xbot.predictMrXMoves(board, mrX,mrXLoc, detectivesLoc,mrXTickets)){ //对于每一个mrX availablemove
                System.out.println("1234567");
                //用一个visitor pattern把singlemove和doublemove分开考虑
                TreeNode node1= m.accept(new Move.Visitor<TreeNode>(){
                    @Override
                    public TreeNode visit(Move.SingleMove singleMove){
                        //在这里更新该更新的东西
                        System.out.println("7654321");
                        TreeNode node1 = new TreeNode(singleMove,score,alpha,beta,mrXLoc,detectivesLoc,turnNum,useDouble,useSecret,mrXTickets,detectivesTickets);
                        System.out.println("node1: "+node1);
                        node1.setParent(node);
                        System.out.println("a");
                        node1.mrXTickets = node1.parent.mrXTickets;
                        System.out.println("b");
                        node1.turnNum += 1;
                        System.out.println("c");
                        node1.mrXLoc = (int)xbot.getMoveInformation(singleMove).get("destination");
                        System.out.println("whywhy");
                        System.out.println("node1: "+node1);
                        ScotlandYard.Ticket usedTicket = (ScotlandYard.Ticket)xbot.getMoveInformation(singleMove).get("ticket");
                        node1.mrXTickets.put(usedTicket, node1.mrXTickets.get(usedTicket)-1);
                        if(usedTicket == ScotlandYard.Ticket.SECRET) node1.useSecret = true;
                        System.out.println("node1: "+node1);
                        return node1;
                    }
                    @Override
                    public TreeNode visit(Move.DoubleMove doubleMove){
                        TreeNode node1 = new TreeNode(doubleMove,score,alpha,beta,mrXLoc,detectivesLoc,turnNum,useDouble,useSecret,mrXTickets,detectivesTickets);
                        node1.turnNum += 2;
                        node1.mrXLoc = (int)xbot.getMoveInformation(doubleMove).get("destination2");
                        ScotlandYard.Ticket ticket1 = (ScotlandYard.Ticket)xbot.getMoveInformation(doubleMove).get("ticket1");
                        ScotlandYard.Ticket ticket2 = (ScotlandYard.Ticket)xbot.getMoveInformation(doubleMove).get("ticket2");
                        node1.mrXTickets.put(ticket1,node1.mrXTickets.get(ticket1)-1);
                        node1.mrXTickets.put(ticket2,node1.mrXTickets.get(ticket2)-1);
                        if(ticket1 == ScotlandYard.Ticket.SECRET || ticket2 == ScotlandYard.Ticket.SECRET) node1.useSecret = true;
                        node1.useDouble = true;
                        return node1;
                    }
                });
                System.out.println("33333333");
                List<List<Map<Integer, ScotlandYard.Ticket>>> allPossibleDetectivesLoc = xbot.predictDetectiveMoves(board,mrXLoc,detectivesLoc);
                for(List<Map<Integer,ScotlandYard.Ticket>> possibleDetectivesLoc : allPossibleDetectivesLoc){ //对于一组可行的detetctives move
                    TreeNode node2 = new TreeNode(null,score,alpha,beta,mrXLoc,detectivesLoc,turnNum,useDouble,useSecret,mrXTickets,detectivesTickets);
                    node1.addChild(node2);
                    System.out.println("444444");
                    List<Integer> dLocs = new ArrayList<>();
                    List<ScotlandYard.Ticket> usedTicketList = new ArrayList<>();
                    for(Map<Integer,ScotlandYard.Ticket> map1 : possibleDetectivesLoc){
                        dLocs.add(map1.keySet().stream().toList().get(0));
                        usedTicketList.add(map1.values().stream().toList().get(0));
                    }
                    node2.detectivesLoc.addAll(dLocs);
                    for(ScotlandYard.Ticket t: usedTicketList){
                        node2.detectivesTickets.put(t,detectivesTickets.get(t)-1);
                        node2.mrXTickets.put(t,mrXTickets.get(t)+1);
                    }
                    System.out.println("5555555");
                    createTree(board,node2,d,score,alpha,beta,mrXLoc,detectivesLoc,turnNum,false,false,mrXTickets,detectivesTickets);
                }
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
                alpha = Math.max(alpha, bestScoreNode.score);
                if(beta <= alpha)
                    break;
            }
            return bestScoreNode;
        }
        else{
            bestScoreNode.score = MAX;
            for(int i = 0; i < node.parent.children.size(); i++){
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, true, alpha, beta);
                if(bestScoreNode.score >= scoreNode.score){
                    bestScoreNode = scoreNode;
                }
                beta = Math.min(beta, bestScoreNode.score);
                if(beta <= alpha)
                    break;
            }
        }
        return bestScoreNode;
    }

}

