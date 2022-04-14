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

    
}
