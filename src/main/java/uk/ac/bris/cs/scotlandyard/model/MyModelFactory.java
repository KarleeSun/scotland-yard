package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {
	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives){
		return new MyModel(setup, mrX, detectives);
	}
	private final class MyModel implements Model{
		private GameSetup setup;
		private Player mrX;
		private ImmutableList<Player> detectives;
		private Board.GameState gameState;
		private Set<Observer> observers;

		private MyModel(GameSetup setup,
						Player mrX,
						ImmutableList<Player> detectives){
		this.setup = setup;
		this.mrX = mrX;
		this.detectives = detectives;
		this.gameState = new MyGameStateFactory().build(setup, mrX, detectives);
		}
		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return null;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			Observer.Event event;
			if(gameState.getWinner().isEmpty()){
				for(Observer observer : observers){
				event = Observer.Event.MOVE_MADE;
				}
			}
			else{
				for(Observer observer : observers){
					event = Observer.Event.GAME_OVER;
				}
			}

		}

	}

}