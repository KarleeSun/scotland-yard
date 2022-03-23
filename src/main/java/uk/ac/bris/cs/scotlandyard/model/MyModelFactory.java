package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.Optional;

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


		@Nonnull
		@Override
		public Board getCurrentBoard() {

		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {

		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {

		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return null;
		}

		@Override
		public void chooseMove(@Nonnull Move move) {

		}
	}


}