package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;

public class Minimax {
    public boolean test = false;
    public List<Long> timeList = new ArrayList<>();     /*for test purposes*/

    public class TreeNode {
        private Move move; //move in each node is unique, and it is the basis of the node
        private int score; //store score of a current move in the node
        private TreeNode parent; //store parent of the node
        private List<TreeNode> children; //store all the children of the node
        public Board.GameState nodeGameState; //current gameState of the board
        public int shortestDistanceWithDetective; //distance from mrX to its nearest detective

        public TreeNode(Move move, int score) { //construction method
            this.move = move;
            this.score = score;
            this.children = new ArrayList<>();
        }

        public void addChild(TreeNode child) { //add child to this node
            this.children.add(child);
            if (child.parent == null) child.setParent(this);
        }

        public void setParent(TreeNode parent) { //set parent to this node
            this.parent = parent;
            if (!parent.children.contains(this)) parent.addChild(this);
        }

        public Move getMove() { //get node.move
            return this.move;
        }

        public int getScore() { //get node.score
            return this.score;
        }

        public TreeNode getParent() { //get node.parent
            return parent;
        }

        public List<TreeNode> getChildren() { //get all the children as a list
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
        createTree(board, root, depth, gameData);
        miniMaxAlphaBeta(root, depth, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return root;
    }

    public void createTree(@Nonnull Board board, TreeNode node, int depth, Info gameData) {
        Score score = new Score();
        Dijkstra dijkstra = new Dijkstra(node.nodeGameState);
        Xbot xbot = new Xbot();
        //get all current mrX available moves
        List<Move> moves = new ArrayList<>(node.nodeGameState.getAvailableMoves().stream().toList());
        //only consider single moves
        moves.removeIf(move -> move instanceof Move.DoubleMove);
        //remove single moves with SECRET ticket
        moves.removeIf(move -> {
            List<ScotlandYard.Ticket> tickets = new ArrayList<>();
            for (ScotlandYard.Ticket ticket : move.tickets())
                tickets.add(ticket);
            return tickets.contains(ScotlandYard.Ticket.SECRET);
        });
        //if already recurred to desired depth then return
        if (moves.size() <= 0 || depth <= 0) return;
        //create a tree node for every move
        for (Move move : moves) {
            long start = System.currentTimeMillis(); //used for test running time
            int destination = ((Move.SingleMove) move).destination;
            int distance = dijkstra.getDetectivesDistance(destination, xbot.getLocAsList(gameData.detectives)).get(0);
            TreeNode newNode = new TreeNode(move, 0);
            newNode.nodeGameState = node.nodeGameState;
            //update node information
            node.shortestDistanceWithDetective = distance;
            node.addChild(newNode);
            //update newNode information
            newNode.setParent(node);
            newNode.score = score.giveScore(board, gameData, move); //store the score of this move in the node
            if(test) timeList.add(System.currentTimeMillis() - start);
            //use recursion to create the deeper layers of this tree
            createTree(board, newNode, depth - 1, gameData);
        }
    }


    /**
     * use minimax algorithm with alpha-beta pruning to find the node with the highest score
     * return the node with the best mrX available move
     * pass the conclusion to Xbot pickMove() method
     */
    public TreeNode miniMaxAlphaBeta(TreeNode node, int depth, Boolean maximizing, int alpha, int beta) {
        if (node.children.isEmpty()) {return node;} // leaf node
        int v = node.score; //store the score of this node into the node
        List<TreeNode> nodes = new ArrayList<>(); //to store a nodes of this layer
        if (maximizing) { //if it is the layer moving by mr X
            for (int i = 0; i < node.children.size(); i++) {
                //use depth first search(DFS) method searching from the top
                TreeNode currentNode = node.children.get(i);
                //decide whether to prune the brunch
                if (beta <= alpha)
                    nodePruning(currentNode);
                //get the node with larger score and pass its information to its parent
                TreeNode scoreNode = miniMaxAlphaBeta(currentNode, depth + 1, false, alpha, beta);
                v = Math.max(v, scoreNode.shortestDistanceWithDetective);
                alpha = Math.max(v, alpha); //update alpha
                currentNode.shortestDistanceWithDetective = v;
                currentNode.score = alpha; //update score in current node to pass score to the upper layer
                nodes.add(currentNode); //add this node to nodes
            }
            //choose the node with maximum score to return
            return Collections.max(nodes, Comparator.comparingInt(n -> n.shortestDistanceWithDetective));
        } else { // if it is the layer moving by detectives
            for (int i = 0; i < node.children.size(); i++) {
                TreeNode currentNode = node.children.get(i);
                //decide whether to prune the brunch
                if (beta <= alpha)
                    nodePruning(currentNode);
                TreeNode scoreNode = miniMaxAlphaBeta(currentNode, depth + 1, true, alpha, beta);
                v = Math.min(v, scoreNode.shortestDistanceWithDetective);
                beta = Math.min(v, beta);
                currentNode.shortestDistanceWithDetective = v;
                currentNode.score = beta;
                nodes.add(currentNode);
            }
            //choose the node with minimum score to return
            return Collections.min(nodes, Comparator.comparingInt(n -> n.shortestDistanceWithDetective));
        }
    }

    // pruning node by clearing all of its children
    public void nodePruning(TreeNode parentNode) {
        List<TreeNode> childrenNode = parentNode.children;
        childrenNode.forEach(child -> nodePruning(child));
        parentNode.nodeGameState = null;
        parentNode.children.clear();
        parentNode.move = null;
        parentNode.parent = null;
    }

    // create a new class to store the update player information
    public static class Info {
        final Player mrX;
        final ImmutableList<Player> detectives;
        public Info(Player mrX, List<Player> detectives) {
            this.mrX = mrX;
            this.detectives = ImmutableList.copyOf(detectives);
        }
    }
}
