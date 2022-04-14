package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Minimax {
    private static Board board;

    // Initial values of
    // Alpha and Beta
    private final int MAX = 100000;
    private final int MIN = -100000;

    public int minimax(int position, int depth, int value, int alpha, int beta, Boolean maximizingPlayer){
        //如果depth=0或gameover，这里怎么判断game有没有over
        if (depth == 0 ){
            return value;
        }

        if(maximizingPlayer) {
            int maxEvaluation = MIN;
            for (int i = depth; i > 0; i--) {
                int val = minimax(depth - 1, value, alpha, beta, false);
                best = Math.max(best, val);
                alpha = Math.max(alpha, best);

                // Alpha Beta Pruning
                if (beta <= alpha)
                    break;
            }
            return maxEvaluation;
        } else {
            int minEvaluation = MAX;
        }
        return 0;
    }
}

//    // Returns optimal value for
//    // current player (Initially called
//    // for root and maximizer)
//    public int minimax(int depth, int nodeIndex,
//                       Boolean maximizingPlayer,
//                       int[] values, int alpha,
//                       int beta)
//    {
//        // Terminating condition. i.e
//        // leaf node is reached
//        if (depth == 3)
//            return values[nodeIndex];
//
//        if (maximizingPlayer)
//        {
//            int best = MIN;
//
//            // Recur for left and
//            // right children
//            for (int i = 0; i < 2; i++)
//            {
//                int val = minimax(depth + 1, nodeIndex * 2 + i,
//                        false, values, alpha, beta);
//                best = Math.max(best, val);
//                alpha = Math.max(alpha, best);
//
//                // Alpha Beta Pruning
//                if (beta <= alpha)
//                    break;
//            }
//            return best;
//        }
//        else
//        {
//            int best = MAX;
//
//            // Recur for left and
//            // right children
//            for (int i = 0; i < 2; i++)
//            {
//
//                int val = minimax(depth + 1, nodeIndex * 2 + i,
//                        true, values, alpha, beta);
//                best = Math.min(best, val);
//                beta = Math.min(beta, best);
//
//                // Alpha Beta Pruning
//                if (beta <= alpha)
//                    break;
//            }
//            return best;
//        }
//    }

}
