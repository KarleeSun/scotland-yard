package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.List;

public class Minimax {
    private class TreeNode {
        private Move move;
        private int score;
        private int alpha;
        private int beta;
        private TreeNode parent;
        private List<TreeNode> children;
        private static int MIN = -100000;
        private static int MAX = 100000;
        private int source;
        private int mrXLoc;
        private List<Integer> detectivesLoc;
        private ScotlandYard.Ticket usedTicket;
        private Boolean useDouble; //用没用double卡（singlemove or doublemove）
        private Boolean useSecret;


        private TreeNode(Move move, int score, int source, int mrXLoc, List<Integer> detectivesLoc,
                         ScotlandYard.Ticket usedTicket, Boolean useDouble, Boolean useSecret) {
            this.move = move;
            this.score = score;
            this.alpha = MIN;
            this.beta = MAX;
            this.source = source; //移动前的位置
            this.mrXLoc = mrXLoc; //移动后mrX的位置
            this.detectivesLoc = detectivesLoc; //移动后detectives的位置
            this.usedTicket = usedTicket; //用的票
            this.useDouble = useDouble; //有没有用double卡
            this.useSecret = useSecret; //有没有用secret卡
        }

        private void addChild(TreeNode child) {
            child.setParent(this);
            this.children.add(child);
        }

        private void addChild(Move move, int score) {
            TreeNode child = new TreeNode(move, score, source, mrXLoc, detectivesLoc, usedTicket, useDouble,useSecret);
            this.children.add(child);
        }

        private void setParent(TreeNode parent) { this.parent = parent; }

        private void remove(TreeNode node) { node.parent.children.remove(node); }

        private int getScore() { return score; }

        private Move getMove() { return move; }

        private TreeNode getParent() { return parent; }

        private List<TreeNode> getChildren() { return children; }

        private void setAlpha(int alpha) { this.alpha = alpha; }

        private int getAlpha() { return alpha; }

        private void setBeta(int beta) { this.beta = beta; }

        private int getBeta() { return beta; }
    }

    public Move bestMove() {
//        TreeNode root = new TreeNode(null,0,);

        return null;
    }
}