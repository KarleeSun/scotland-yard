package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;

/*
    detectivesLoc有大问题
 */


public class Minimax {
    public static int MIN = Integer.MIN_VALUE;
    public static int MAX = Integer.MAX_VALUE;
    public static Piece.MrX mrX = Piece.MrX.MRX;

    public class TreeNode {
        private Move move;
        private int score;
        private int alpha = MIN;
        private int beta = MAX;
        private TreeNode parent;
        private List<TreeNode> children;
        private Boolean useDouble; //用没用double卡（singlemove or doublemove）
        private Boolean useSecret;
        private Info gameData;
        public Board.GameState shit; // 这玩意记得改掉变量名
        public int distance;


        public TreeNode(Move move, int score, Boolean useDouble, Boolean useSecret, Info gameData) {
            this.move = move;
            this.score = score;
            this.alpha = MIN;
            this.beta = MAX;
            this.children = new ArrayList<>();
            this.useDouble = useDouble; //有没有用double卡
            this.useSecret = useSecret; //有没有用secret卡
            this.gameData = gameData;
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
            if (child.parent == null) child.setParent(this);
        }

        public void addChild(Move move, int score, Boolean useDouble, Boolean useSecret, Info gameData) {
            TreeNode child = new TreeNode(move, score, useDouble, useSecret, gameData);
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

    //xPlayer是Player类型的mrX
    //detectivesPlayer: Player类型的detectives
    public TreeNode tree(@Nonnull Board board, int depth, Info gameData) {
        System.out.println("loaded");
        TreeNode root = new TreeNode(null, 0, false, false, gameData);
        root.shit = (Board.GameState) board;
        createTree(root, depth);
        // TreeNode node = miniMaxAlphaBeta(root, depth, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return root;
    }

    public void createTree(TreeNode node, int depth) {
        Dijkstra dijkstra = new Dijkstra(node.shit);
        List<Move> moves = new ArrayList<>(node.shit.getAvailableMoves().stream().toList());
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
            int location = moves.get(0).source();
            double lastDistance = 0;
            if (parent != null)
                if (parent.parent != null)
                    lastDistance = parent.parent.distance;
            for (Move move : moves) {
                int destination = move instanceof Move.SingleMove ? ((Move.SingleMove) move).destination : ((Move.DoubleMove) move).destination2;
                int distance = dijkstra.getDistance(location, destination);
                TreeNode newNode = new TreeNode(move, distance, false, false, null);
                newNode.shit = node.shit;
                node.addChild(newNode);
                newNode.setParent(node);
                createTree(newNode, depth - 1);
            }

        } else {
            for (Move move : moves) {
                List<Integer> possibles = getmrxPossibleLocation(node.shit);
                int distance = getDistance(possibles, move, node.shit);
                TreeNode newNode = new TreeNode(move, distance, false, false, null);
                newNode.shit = node.shit;
                node.addChild(newNode);
                newNode.setParent(node);
                createTree(newNode, depth - 1);
            }
        }
    }

    public List<Integer> getmrxPossibleLocation(Board board) {
        List<Integer> mrXPossibleLocation = new ArrayList<Integer>();
        for (Move m : board.getAvailableMoves()) {
            if (m instanceof Move.SingleMove) mrXPossibleLocation.add(((Move.SingleMove) m).destination);
            else mrXPossibleLocation.add(((Move.DoubleMove) m).destination2);
        }
        return mrXPossibleLocation;
    }

    public int getDistance(List<Integer> possibles, Move move, Board board) {
        Dijkstra dijkstra = new Dijkstra(board);
        List<Integer> distanceChange = new ArrayList<>();
        for (Integer possible : possibles) {
            int before = dijkstra.getDistance(possible, move.source());
            int after = dijkstra.getDistance(possible, ((Move.SingleMove) move).destination);
            distanceChange.add(after - before);
        }
        return (int) distanceChange.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
    }

//    public void createTree(@Nonnull Board board, TreeNode node, int depth, Info gameData) {
//        Xbot xbot = new Xbot();
//        depth--;
//        int i = 0;
//        int j = 0;
//        ImmutableSet<Move> allPossibleMoves = board.getAvailableMoves();
//        Set<Move> moves;
//        Set<Move> singleMoves = new HashSet<>();
//        Set<Move> doubleMoves = new HashSet<>();
//        for (Move move : allPossibleMoves) {
//            if (!(Boolean) xbot.getMoveInformation(move).get("useDouble")) singleMoves.add(move);
//            else doubleMoves.add(move);
//        }
//        if (!useDoubleHere(board, gameData)) moves = singleMoves;
//        else moves = doubleMoves;
//        for (Move m : moves) { //对于mrX的每一个move
//            i++;
//            int destination = (int) xbot.getMoveInformation(m).get("destination");
//            Player updateMrX = gameData.mrX.at(destination).use(m.tickets());
//            Info newGameData = new Info(updateMrX, gameData.detectives.stream().toList());
//            Score score = new Score();
////            int s = score.giveScore(board, newGameData.mrX.location(), xbot.getLocAsList(newGameData.detectives), m.source(), m.tickets().iterator().next());
//            Boolean useDouble = (Boolean) xbot.getMoveInformation(m).get("useDouble");
//            Boolean useSecret = (Boolean) xbot.getMoveInformation(m).get("useSecret");
//            TreeNode mrXNode = new TreeNode(m, -1, useDouble, useSecret, newGameData);
//            System.out.println("or here?");
//            mrXNode.setParent(node);
//
//            System.out.println("here??");
//            List<List<Player>> allUpdatedDetectives = new ArrayList<>();
//            for (Player detective : newGameData.detectives) { //对于每一个detective
//                System.out.println("00000");
//                List<Player> updatedOneDetective = new ArrayList<>();
//                List<Move.SingleMove> oneDetectiveMove = xbot.makeSingleMoves(board.getSetup(), newGameData.detectives, detective, detective.location());
//                for (Move.SingleMove singleMove : oneDetectiveMove) {
//                    Dijkstra before = new Dijkstra(newGameData.mrX.location(), singleMove.source(), board);
//                    Dijkstra after = new Dijkstra(newGameData.mrX.location(), singleMove.destination, board);
//                    if (after.getDistance() < before.getDistance()) {
//                        System.out.println("11111");
//                        Player updateDetective = detective.at(singleMove.destination).use(singleMove.ticket).give(singleMove.ticket);
//                        updatedOneDetective.add(updateDetective);
//                    }
//                }
//                allUpdatedDetectives.add(updatedOneDetective);
//            }
//            System.out.println("22222");
//            System.out.println("allUpdatedDetectives: " + allUpdatedDetectives);
//            List<List<Player>> result = Lists.cartesianProduct(ImmutableList.copyOf(allUpdatedDetectives));
//            System.out.println("result: " + result);
//            System.out.println("result.size: " + result.size());
//            for (List<Player> detectivesMoveOnce : result) {
//                System.out.println("loop here?");
//                j++;
//                System.out.println("detective loop num: " + j);
//                Info updateGameData = new Info(newGameData.mrX, detectivesMoveOnce);
//                Score score1 = new Score();
//                int s1 = score1.giveScore(board, updateGameData.mrX.location(), xbot.getLocAsList(updateGameData.detectives), m.source(), m.tickets().iterator().next());
//                TreeNode detectivesNode = new TreeNode(mrXNode.move, s1, useDouble, useSecret, updateGameData);
//                detectivesNode.setParent(mrXNode);
//                System.out.println("depth: " + depth);
////                    createTree(board,detectivesNode,depth,updateGameData);
//            }
//        }
//
//    }

//    public Boolean useDoubleHere(@Nonnull Board board, Info gameData) {
//        int turnNum = board.getMrXTravelLog().size();
//        if (turnNum > 0 && board.getSetup().moves.get(turnNum)) return true;
//        Xbot xbot = new Xbot();
//        Dijkstra dijkstra = new Dijkstra(gameData.mrX.location(), xbot.getLocAsList(gameData.detectives), board);
//        if (dijkstra.getDetectivesDistance().get(0) < 2) return true;
//        return false;
//    }


    //最终把最好的move传给root存到root的move里
    public TreeNode miniMaxAlphaBeta(TreeNode node, int depth, Boolean maximizing, int alpha, int beta) {
        if (node.children.isEmpty()) {
            return node;
        }
        int v = node.score;
        if (maximizing) {
            for (int i = 0; i < node.children.size(); i++) {
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, false, alpha, beta);
                v = Math.max(v, scoreNode.score);
                alpha = Math.max(v, alpha);
                node.score = v;
                if (beta <= alpha)
                    break;
            }
        } else {
            for (int i = 0; i < node.children.size(); i++) {
                TreeNode scoreNode = miniMaxAlphaBeta(node.children.get(i), depth + 1, true, alpha, beta);
                v = Math.min(v, scoreNode.score);
                beta = Math.min(v, beta);
                node.score = v;
                if (beta <= alpha)
                    break;
            }
        }
        return node;
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
