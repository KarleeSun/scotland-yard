package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.List;

public class TreeNode {
    private Move move;
    private int score;
    private int alpha;
    private int beta;
    private TreeNode parent;
    private List<TreeNode> children;
    private static int MIN = -100000;
    private static int MAX = 100000;
    
    public TreeNode(Move move, int score){
        this.move = move;
        this.score = score;
        this.alpha = MIN;
        this.beta = MAX;
    }

    public void addChild(TreeNode child){
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Move move, int score){
        TreeNode child = new TreeNode(move, score);
        this.children.add(child);
    }

    public void setParent(TreeNode parent) {this.parent = parent;}

    public int getScore() {return score;}
    public Move getMove() {return move;}

    public TreeNode getParent() {return parent;}

    public List<TreeNode> getChildren() {return children;}

    public void setAlpha(int alpha) {this.alpha = alpha;}

    public int getAlpha() {return alpha;}

    public void setBeta(int beta) {this.beta = beta;}

    public int getBeta() {return beta;}
}
