package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TreeNode {
    public static int MIN = -100000;
    public static int MAX = 100000;

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
        if (child.getParent() == null) child.setParent(this);
    }

    public void addChild(Move move, int score, int alpha, int beta, int mrXLoc, List<Integer> detectivesLoc,
                         Boolean useDouble, Boolean useSecret) {
        TreeNode child = new TreeNode(move, score, alpha, beta, mrXLoc, detectivesLoc, turnNum,
                useDouble, useSecret, mrXTickets, detectivesTickets);
        this.addChild(child);
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
