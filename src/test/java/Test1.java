import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.Minimax;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;

public class Test1 {
    public Test1() {
        setup();
    }

    private ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> defaultGraph;

    @Test
    public void testForMiniMax() {
        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 138);
        Board.GameState state = new MyGameStateFactory().build(standard24MoveSetup(),
                mrX, red, green, blue, white, yellow);
        Minimax minimax = new Minimax();
        long start = System.currentTimeMillis();
        Minimax.TreeNode root = minimax.tree(state, 3, null);
        System.out.println("Time use: " + (System.currentTimeMillis() - start) + "ms");
        Map<Integer, List<Minimax.TreeNode>> buffer = new HashMap<>();
        countNode(root, buffer, 0);
        for (Map.Entry<Integer, List<Minimax.TreeNode>> layer : buffer.entrySet()) {
            System.out.println("    depth: " + layer.getKey() + " size:" + layer.getValue().size());
        }
        System.out.println("avg distance use:" + minimax.timeList.stream().mapToLong(t -> t).average().orElseThrow() + "ms");
    }

    public void countNode(Minimax.TreeNode node, Map<Integer, List<Minimax.TreeNode>> buffer, int depth) {
        List<Minimax.TreeNode> children = node.getChildren();
        buffer.putIfAbsent(depth, new ArrayList<>());
        buffer.get(depth).addAll(children);
        children.forEach(child -> {
            countNode(child, buffer, depth + 1);
        });
    }

    public void setup() {
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read game graph", e);
        }
    }

    public GameSetup standard24MoveSetup() {
        return new GameSetup(defaultGraph, STANDARD24MOVES);
    }
}
