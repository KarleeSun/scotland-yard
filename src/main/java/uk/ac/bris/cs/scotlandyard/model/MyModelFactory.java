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

		private MyModel(GameSetup setup,
						Player mrX,
						ImmutableList<Player> detectives){
		this.setup = setup;
		this.mrX = mrX;
		this.detectives = detectives;
		this.gameState = new MyGameStateFactory().build(setup, mrX, detectives);
		this.observers = new HashSet<>();
		}
		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return new Board() {
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
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if(observers.contains(observer)){
				throw new IllegalArgumentException("Observer already registered.");
			}
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
				event = Observer.Event.MOVE_MADE;
				for(Observer observer : observers){
					observer.onModelChanged(getCurrentBoard(),event);
				}
			}
			else{
				event = Observer.Event.GAME_OVER;
				for(Observer observer : observers){
					observer.onModelChanged(getCurrentBoard(),event);
				}
			}
		}
	}
}