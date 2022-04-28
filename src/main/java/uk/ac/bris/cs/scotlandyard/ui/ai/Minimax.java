package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;

public class Minimax {
    public boolean test = false;
    public List<Long> timeList = new ArrayList<>();     /*for test purposes*/

    public class TreeNode {
        private Move move;
        private int score;
        private TreeNode parent;
        private List<TreeNode> children;
        public Board.GameState nodeGameState;
        public int shortestDistanceWithDetective;


        public TreeNode(Move move, int score) {
            this.move = move;
            this.score = score;
            this.children = new ArrayList<>();
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
            if (child.parent == null) child.setParent(this);
        }


        public void setParent(TreeNode parent) {
            this.parent = parent;
            if (!parent.children.contains(this)) parent.addChild(this);
        }

        public Move getMove() {
            return this.move;
        }

        public int getScore() {
            return this.score;
        }


        public TreeNode getParent() {
            return parent;
        }

        public List<TreeNode> getChildren() {
            return children;
        }
    }

    /**
     * create game tree based on current game state
     * pass in for minimax with alpha-beta pruning
     * return the root of game state
     */
    public TreeNode tree(@Nonnull Board board, int depth, Info gameData) {
        TreeNode root = new TreeNode(null, 0);
        root.nodeGameState = (Board.GameState) board;
        createTree(board, root, depth, gameData, root);
        miniMaxAlphaBeta(root, depth, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return root;
    }

    public void createTree(@Nonnull Board board, TreeNode node, int depth, Info gameData, TreeNode root) {
        Score score = new Score();
        Dijkstra dijkstra = new Dijkstra(node.nodeGameState);
        Xbot xbot = new Xbot();
        List<Move> moves = new ArrayList<>(node.nodeGameState.getAvailableMoves().stream().toList());
        //only consider single moves
        moves.removeIf(move -> move instanceof Move.DoubleMove);
        moves.removeIf(move -> {
            List<ScotlandYard.Ticket> tickets = new ArrayList<>();
            for (ScotlandYard.Ticket ticket : move.tickets())
                tickets.add(ticket);
            return tickets.contains(ScotlandYard.Ticket.SECRET);
        });
        //if already recurred to desired depth then return
        if (moves.size() <= 0 || depth <= 0) return;
        for (Move move : moves) {
            long start = System.currentTimeMillis();
            int destination = ((Move.SingleMove) move).destination;
            int distance = dijkstra.getDetectivesDistance(destination, xbot.getLocAsList(gameData.detectives)).get(0);
            TreeNode newNode = new TreeNode(move, 0);
            newNode.nodeGameState = node.nodeGameState;
            node.shortestDistanceWithDetective = distance;
            node.addChild(newNode);
            newNode.setParent(node);
            newNode.score = score.giveScore(board, gameData, move);
            if(test) timeList.add(System.currentTimeMillis() - start);
            createTree(board, newNode, depth - 1, gameData, root);
        }
    }

    // up
    public TreeNode miniMaxAlphaBeta(TreeNode node, int depth, Boolean maximizing, int alpha, int beta) {
        if (node.children.isEmpty()) {return node;} // leaf node
        int v = node.score;
        List<TreeNode> nodes = new ArrayList<>();
        if (maximizing) {
            for (int i = 0; i < node.children.size(); i++) {
                TreeNode currentNode = node.children.get(i);
                if (beta <= alpha)
                    nodePruning(currentNode);
                TreeNode scoreNode = miniMaxAlphaBeta(currentNode, depth + 1, false, alpha, beta);
                v = Math.max(v, scoreNode.shortestDistanceWithDetective);
                alpha = Math.max(v, alpha);
                currentNode.shortestDistanceWithDetective = v;
                currentNode.score = alpha;
                nodes.add(currentNode);
            }
        } else {
            for (int i = 0; i < node.children.size(); i++) {
                TreeNode currentNode = node.children.get(i);
                if (beta <= alpha)
                    nodePruning(currentNode);
                TreeNode scoreNode = miniMaxAlphaBeta(currentNode, depth + 1, true, alpha, beta);
                v = Math.min(v, scoreNode.shortestDistanceWithDetective);
                beta = Math.min(v, beta);
                currentNode.shortestDistanceWithDetective = v;
                currentNode.score = beta;
                nodes.add(currentNode);
            }
        }
        return Collections.max(nodes, Comparator.comparingInt(n -> n.shortestDistanceWithDetective));
    }

    // pruning node by clear its children
    public void nodePruning(TreeNode parentNode) {
        List<TreeNode> childrenNode = parentNode.children;
        childrenNode.forEach(child -> nodePruning(child));
        parentNode.nodeGameState = null;
        parentNode.children.clear();
        parentNode.move = null;
        parentNode.parent = null;
    }


    public static class Info {
        final Player mrX;
        final ImmutableList<Player> detectives;
        public Info(Player mrX, List<Player> detectives) {
            this.mrX = mrX;
            this.detectives = ImmutableList.copyOf(detectives);
        }
    }

}
