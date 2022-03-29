package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
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
		private Board currentBoard;

		private MyModel(GameSetup setup,
						Player mrX,
						ImmutableList<Player> detectives){
			this.setup = setup;
			this.mrX = mrX;
			this.detectives = detectives;
			this.gameState = new MyGameStateFactory().build(setup, mrX, detectives);
			this.observers = new HashSet<>();
			this.currentBoard = getCurrentBoard();
		}
		@Nonnull
		@Override
		public Board getCurrentBoard() { //get current board information from MyGameStateFactory
			Board currentBoard = new Board() {
				@Nonnull
				@Override
				public GameSetup getSetup() {
					return gameState.getSetup();
				}

				@Nonnull
				@Override
				public ImmutableSet<Piece> getPlayers() {
					return gameState.getPlayers();
				}

				@Nonnull
				@Override
				public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
					return gameState.getDetectiveLocation(detective);
				}

				@Nonnull
				@Override
				public Optional<TicketBoard> getPlayerTickets(Piece piece) {
					return gameState.getPlayerTickets(piece);
				}

				@Nonnull
				@Override
				public ImmutableList<LogEntry> getMrXTravelLog() {
					return gameState.getMrXTravelLog();
				}

				@Nonnull
				@Override
				public ImmutableSet<Piece> getWinner() {
					return gameState.getWinner();
				}

				@Nonnull
				@Override
				public ImmutableSet<Move> getAvailableMoves() {
					return gameState.getAvailableMoves();
				}
			};
			return currentBoard;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) { //register observer
			if(observer == null) throw new NullPointerException("No observer");
			if(observers.contains(observer)){
				throw new IllegalArgumentException("Observer already registered.");
			}
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) { //unregister observer
			if(observer == null) throw new NullPointerException("No observer");
			if(!observers.contains(observer)){
				throw new IllegalArgumentException("Observer has never been registered.");
			}
			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() { //get observers and return an immutable set
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			// Advance the model with move, then notify all observers of what what just happened.
			// you may want to use getWinner() to determine whether to send out Event.MOVE_MADE or Event.GAME_OVER
			Observer.Event event;
			gameState = gameState.advance(move); //update game state after move
			currentBoard = getCurrentBoard();

			for (Observer observer: observers){
				if(gameState.getWinner().isEmpty()) event= Observer.Event.MOVE_MADE;
				else event= Observer.Event.GAME_OVER;
				observer.onModelChanged(currentBoard,event); //notify all the observers
			}
		}
	}
}