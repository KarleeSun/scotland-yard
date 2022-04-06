package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;


import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class Xbot implements Ai {


	@Nonnull @Override public String name() { return "Xbot"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		List<Integer> l = new ArrayList<Integer>();
		l.add(84);
		l.add(26);

		DijkstraMinHeap mp = new DijkstraMinHeap(1, l , board);
		mp.getDetectivesDistance();
		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
	}







	//判断这个位置detective能不能一步走到
//	public Move oneMoveNotReachMrX(@Nonnull Board board){
//        var moves = board.getAvailableMoves().asList();
//		return null;
//    }

}
