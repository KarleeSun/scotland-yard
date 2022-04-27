package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;

public class Minimax {
    public static int MIN = Integer.MIN_VALUE;
    public static int MAX = Integer.MAX_VALUE;
    public static Piece.MrX mrX = Piece.MrX.MRX;

    public class TreeNode {
        private Move move;
        private int score;
        public int alpha = MIN;
        public int beta = MAX;
        private TreeNode parent;
        private List<TreeNode> children;
        private Info gameData;
        public Board.GameState nodeGameState; // 这玩意记得改掉变量名
        public int shortestDistanceWithDetective;


        public TreeNode(Move move, int score,Info gameData) {
            this.move = move;
            this.score = score;
            this.alpha = MIN;
            this.beta = MAX;
            this.children = new ArrayList<>();
            this.gameData = gameData;
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
            if (child.parent == null) child.setParent(this);
        }

//        public void addChild(Move move, int score, Info gameData) {
//            TreeNode child = new TreeNode(move, score,gameData);
//            this.children.add(child);
//            if (!child.parent.equals(this)) child.setParent(this);
//        }

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


    //xPlayer是Player类型的mrX
    //detectivesPlayer: Player类型的detectives
    public TreeNode tree(@Nonnull Board board, int depth, Info gameData) {
        System.out.println("loaded");
        TreeNode root = new TreeNode(null, 0, gameData);
        root.nodeGameState = (Board.GameState) board;
        createTree(board, root, depth, gameData);
        miniMaxAlphaBeta(root, depth, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return root;
    }

    public List<Long> timeList = new ArrayList<>();

    public void createTree(@Nonnull Board board, TreeNode node, int depth, Info gameData) {
        Score score = new Score();
        Dijkstra dijkstra = new Dijkstra(node.nodeGameState);
        Xbot xbot = new Xbot();
        List<Move> moves = new ArrayList<>(node.nodeGameState.getAvailableMoves().stream().toList());
//        moves.removeIf(move -> move instanceof Move.DoubleMove);
        moves.removeIf(move -> {
            List<ScotlandYard.Ticket> tickets = new ArrayList<>();
            for (ScotlandYard.Ticket ticket : move.tickets())
                tickets.add(ticket);
            return tickets.contains(ScotlandYard.Ticket.SECRET);
        });
        TreeNode parent = node.getParent();
        if (moves.size() <= 0 || depth <= 0)
            return;
        if (moves.get(0).commencedBy().isMrX()) {
            for (Move move : moves) {
                int destination = move instanceof Move.SingleMove ? ((Move.SingleMove) move).destination : ((Move.DoubleMove) move).destination2;
                long start = System.currentTimeMillis();
                int distance = dijkstra.getDetectivesDistance(destination, xbot.getLocAsList(gameData.detectives)).get(0);
                TreeNode newNode = new TreeNode(move, score.giveScore(board,gameData,move), null);
                newNode.nodeGameState = node.nodeGameState;
                node.shortestDistanceWithDetective = distance;
                node.addChild(newNode);
                newNode.setParent(node);
                timeList.add(System.currentTimeMillis() - start);
                createTree(board, newNode, depth - 1, gameData);
            }

        } else {
            for (Move move : moves) {
                long start = System.currentTimeMillis();
                List<Integer> possibles = getMrxPossibleLocation(node.nodeGameState);
                int distance = dijkstra.getDistance(gameData.mrX.location(), ((Move.SingleMove) move).destination);
//                int distance = getDistance(possibles, move, node.nodeGameState);
                TreeNode newNode = new TreeNode(move, distance, null);
                newNode.nodeGameState = node.nodeGameState;
                node.shortestDistanceWithDetective = distance;
                node.addChild(newNode);
                newNode.setParent(node);
                timeList.add(System.currentTimeMillis() - start);
                createTree(board, newNode, depth - 1, gameData);
            }
        }
    }

    public List<Integer> getMrxPossibleLocation(Board board) { //得到mrX所有可能在的位置
        List<Integer> mrXPossibleLocation = new ArrayList<Integer>();
        for (Move m : board.getAvailableMoves()) {
            if (m instanceof Move.SingleMove) mrXPossibleLocation.add(((Move.SingleMove) m).destination);
            else mrXPossibleLocation.add(((Move.DoubleMove) m).destination2);
        }
        return mrXPossibleLocation;
    }


    //最终把最好的move传给root存到root的move里
    public TreeNode miniMaxAlphaBeta(TreeNode node, int depth, Boolean maximizing, int alpha, int beta) {
        if (node.children.isEmpty()) {
            return node;
        }
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
                currentNode.alpha = alpha;
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
                currentNode.beta = beta;
                nodes.add(currentNode);
            }
        }
        return Collections.max(nodes, Comparator.comparingInt(n -> n.shortestDistanceWithDetective));
    }

    public void nodePruning(TreeNode parentNode) {
        List<TreeNode> childrenNode = parentNode.children;
        for (TreeNode child : childrenNode) {
            nodePruning(child);
        }
        parentNode.nodeGameState = null;
        parentNode.children.clear();
        parentNode.gameData = null;
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
