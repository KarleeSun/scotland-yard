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

		// TODO
		throw new RuntimeException("Implement me!");


    }



	private final class MyModel implements Model{

	    private MyModel(final GameSetup setup,
                final Player mrX,
                final ImmutableList<Player> detectives){

        }


		private Set<Observer> observers;

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
			for(Observer observer : observers){
//				observer.getUpdate(this);
			}
		}
	}

}