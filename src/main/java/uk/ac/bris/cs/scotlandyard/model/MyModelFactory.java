package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.List;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {
    @Nonnull @Override public Model build(GameSetup setup,
                                          Player mrX,
                                          ImmutableList<Player> detectives) {
//        This class is a factory again, producing via build(...)
//        a game Model which should hold a GameState and Observer list
//        and can be observed by Observers with regard to Events such as MOVE_MADE or GAME_OVER.

        return null;
    }


    private final class MyModel implements Model{
        private GameSetup setup;
        private ImmutableSet<Piece> remaining;
        private ImmutableList<LogEntry> log;
        private Player mrX;
        private List<Player> detectives;
        private ImmutableSet<Move> moves;
        private ImmutableSet<Piece> winner;

        @Nonnull
        @Override
        public Board getCurrentBoard() {
            return null;
        }


    }




}


