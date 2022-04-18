package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        private int alpha;
        private int beta;
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
            this.alpha = MIN;
            this.beta = MAX;
            this.children = new ArrayList<>();
//            this.source = source; //移动前的位置 不需要其实，因为他的出发位置就是他爹的到达位置
            this.mrXLoc = mrXLoc; //移动后mrX的位置
            this.detectivesLoc = detectivesLoc; //移动后detectives的位置
            this.turnNum = turnNum; //记录一下轮数
            this.useDouble = useDouble; //有没有用double卡
            this.useSecret = useSecret; //有没有用secret卡
            this.mrXTickets = mrXTickets; //存的是mrX的票
            this.detectivesTickets = detectivesTickets; //存的是detective的票
//            this.parent = new TreeNode(null,0,MIN,MAX,0,null,0,false,false,null,null);
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
            if (child.parent == null) child.setParent(this);
        }

        public void addChild(Move move, int score, int alpha, int beta, int mrXLoc, List<Integer> detectivesLoc,
                             Boolean useDouble, Boolean useSecret) {
            TreeNode child = new TreeNode(move, score, alpha, beta, mrXLoc, detectivesLoc, turnNum,
                    useDouble, useSecret, mrXTickets, detectivesTickets);
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

    //先要自己构建出一个tree
    //大问题：useDouble和useSecret的更新
    //还有一个事就是doublemove和secret要在轮数结束之前都用完
    //doublemove两轮就只让他用一个secret吧 要用就是其中的第一轮用
    //注意location和牌堆的更新
    //更新：turnNum, mrXLoc, detectivesLoc, mrXTickets, detectivesTickets, useSecret, useDouble

    //好了这就创建好了要用的tree
    public TreeNode tree(@Nonnull Board board, Move move, int score, int depth, int alpha, int beta, int mrXLoc,
                         List<Integer> detectivesLoc, int turnNum, Boolean useDouble, Boolean useSecret,
                         Map<ScotlandYard.Ticket, Integer> mrXTickets, Map<ScotlandYard.Ticket, Integer> detectivesTickets) {
        System.out.println("here: " + detectivesTickets);
        System.out.println("mrXTickets: " + mrXTickets);
        Xbot xbot = new Xbot();
        System.out.println("here1: " + detectivesTickets);
        TreeNode root = new TreeNode(null, score, MIN, MAX, mrXLoc, detectivesLoc, turnNum,
                useDouble, useSecret, mrXTickets, detectivesTickets);
        System.out.println("here2: " + detectivesTickets);
        createTree(board, root, depth, score, alpha, beta, mrXLoc, detectivesLoc, turnNum, useDouble, useSecret, mrXTickets, detectivesTickets);
        System.out.println("here3: " + detectivesTickets);
        return root;
    }

    //递归 就循环着创这个树
    //注意记录深度和轮数
    int d = 0;

    private void createTree(@Nonnull Board board, TreeNode node, int depth, int score, int alpha, int beta, int mrXLoc,
                            List<Integer> detectivesLoc, int turnNum, Boolean useDouble, Boolean useSecret,
                            Map<ScotlandYard.Ticket, Integer> mrXTickets, Map<ScotlandYard.Ticket, Integer> detectivesTickets) {
        if (d < depth && detectivesTickets.values().stream().filter(m -> m < 0).toList().isEmpty()) {
            Xbot xbot = new Xbot();
            System.out.println("depth d: " + d);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("creatTree: " + mrXTickets);
            System.out.println("hasWinner: " + !xbot.hasWinner(board, mrXLoc, detectivesLoc));
            if (!xbot.hasWinner(board, mrXLoc, detectivesLoc) && d < depth) { //如果游戏没有结束
                System.out.println("00000000");
                d++;
                for (Move m : xbot.predictMrXMoves(board, mrX, mrXLoc, detectivesLoc, mrXTickets)) { //对于每一个mrX availablemove
                    System.out.println("1234567");
                    //用一个visitor pattern把singlemove和doublemove分开考虑
                    TreeNode node1 = m.accept(new Move.Visitor<TreeNode>() {
                        @Override
                        public TreeNode visit(Move.SingleMove singleMove) {
                            //在这里更新该更新的东西
                            System.out.println("7654321");
                            TreeNode node1 = new TreeNode(singleMove, score, alpha, beta, mrXLoc, detectivesLoc, turnNum, useDouble, useSecret, mrXTickets, detectivesTickets);

                            System.out.println("node1: " + node1);
                            node1.setParent(node);
                            System.out.println("a");
                            node1.mrXTickets = node1.parent.mrXTickets;
                            System.out.println("b");
                            node1.turnNum += 1;
                            System.out.println("singleMOve here: " + singleMove);
                            System.out.println("getMoveInformation: " + xbot.getMoveInformation(singleMove));
                            node1.mrXLoc = (int) xbot.getMoveInformation(singleMove).get("destination");
                            System.out.println("whywhy");
                            System.out.println("node1: " + node1);
                            ScotlandYard.Ticket usedTicket = (ScotlandYard.Ticket) xbot.getMoveInformation(singleMove).get("ticket");
                            node1.mrXTickets.put(usedTicket, node1.mrXTickets.get(usedTicket) - 1);
                            if (usedTicket == ScotlandYard.Ticket.SECRET) node1.useSecret = true;
                            Score score1 = new Score(mrXLoc,node1.mrXLoc,node1.detectivesLoc,false,node1.useSecret,node1.mrXTickets,node1.detectivesTickets,usedTicket);
                            node1.score = score1.giveScore(board,turnNum, mrXLoc,detectivesLoc);
                            System.out.println("node1: " + node1);
                            return node1;
                        }

                        @Override
                        public TreeNode visit(Move.DoubleMove doubleMove) {
                            TreeNode node1 = new TreeNode(doubleMove, score, alpha, beta, mrXLoc, detectivesLoc, turnNum, useDouble, useSecret, mrXTickets, detectivesTickets);
                            node1.turnNum += 2;
                            node1.mrXLoc = (int) xbot.getMoveInformation(doubleMove).get("destination2");
                            ScotlandYard.Ticket ticket1 = (ScotlandYard.Ticket) xbot.getMoveInformation(doubleMove).get("ticket1");
                            ScotlandYard.Ticket ticket2 = (ScotlandYard.Ticket) xbot.getMoveInformation(doubleMove).get("ticket2");
                            node1.mrXTickets.put(ticket1, node1.mrXTickets.get(ticket1) - 1);
                            node1.mrXTickets.put(ticket2, node1.mrXTickets.get(ticket2) - 1);
                            if (ticket1 == ScotlandYard.Ticket.SECRET || ticket2 == ScotlandYard.Ticket.SECRET)
                                node1.useSecret = true;
                            node1.useDouble = true;
                            Score score1 = new Score(mrXLoc,node1.mrXLoc,node1.detectivesLoc, true,node1.useSecret,node1.mrXTickets,node1.detectivesTickets,ticket1);
                            node1.score = score1.giveScore(board,turnNum, mrXLoc,detectivesLoc);
                            return node1;
                        }
                    });
                    System.out.println("33333333");
                    Map<ScotlandYard.Ticket, Integer> detectivesTicketsCopy = detectivesTickets;
                    List<List<Map<Integer, ScotlandYard.Ticket>>> allPossibleDetectivesLoc = xbot.predictDetectiveMoves(board, mrXLoc, detectivesLoc, detectivesTickets);
                    Map<ScotlandYard.Ticket, Integer> mrXTicketsCopy = mrXTickets;
                    for (List<Map<Integer, ScotlandYard.Ticket>> possibleDetectivesLoc : allPossibleDetectivesLoc) { //对于一组可行的detetctives move
                        detectivesTicketsCopy = detectivesTickets;
                        TreeNode node2 = new TreeNode(null, score, alpha, beta, mrXLoc, detectivesLoc, turnNum, useDouble, useSecret, mrXTicketsCopy, detectivesTicketsCopy);
                        node1.addChild(node2);
                        System.out.println("444444");
                        List<Integer> dLocs = new ArrayList<>();
                        List<ScotlandYard.Ticket> usedTicketList = new ArrayList<>();
                        detectivesLoc.clear();
                        node2.detectivesLoc.clear();
                        for (Map<Integer, ScotlandYard.Ticket> map1 : possibleDetectivesLoc) {
                            System.out.println("map1: " + map1);
                            dLocs.add(Map.copyOf(map1).keySet().stream().toList().get(0));
                            usedTicketList.add(Map.copyOf(map1).values().stream().toList().get(0));
                        }
                        node2.detectivesLoc.addAll(dLocs);
                        System.out.println("node2 detectivesLoc" + detectivesLoc);
                        System.out.println("detectivesLoc: " + detectivesLoc);
                        for (ScotlandYard.Ticket t : usedTicketList) {
                            node2.detectivesTickets.put(t, detectivesTickets.get(t) - 1);
                            node2.mrXTickets.put(t, mrXTickets.get(t) + 1);
                        }
                        Score score2 = new Score(node1.mrXLoc, node2.mrXLoc, node2.detectivesLoc, node1.useDouble,node1.useSecret,node2.mrXTickets,node2.detectivesTickets,null);
                        System.out.println("5555555");
//                        if (d < depth && !xbot.hasWinner(board, mrXLoc, node2.detectivesLoc) && !detectivesLoc.isEmpty()) {
//                            createTree(board, node2, depth, score, alpha, beta, node1.mrXLoc, node2.detectivesLoc, turnNum, false, false, mrXTickets, detectivesTickets);
//                        }
                    }
                }
                d--;
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

}
