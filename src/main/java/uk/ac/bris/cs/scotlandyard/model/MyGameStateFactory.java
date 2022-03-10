package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Sets;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
    private final class MyGameState implements GameState {
        private GameSetup setup;
        private ImmutableSet<Piece> remaining;
        private ImmutableList<LogEntry> log;
        private Player mrX;
        private List<Player> detectives;
        private ImmutableSet<Move> moves;
        private ImmutableSet<Piece> winner;

        private MyGameState(@Nonnull final GameSetup setup,
                            final ImmutableSet<Piece> remaining,
                            final ImmutableList<LogEntry> log,
                            final Player mrX,
                            final List<Player> detectives){
            this.setup = setup;
            this.remaining = remaining;
            this.log = log;
            this.mrX = mrX;
            this.detectives = detectives;

            if(setup.moves.isEmpty()) throw new IllegalArgumentException("Empty Move.");
            if(detectives.isEmpty()) throw new IllegalArgumentException("Empty Detectives.");
            if(!mrX.isMrX()) throw new IllegalArgumentException("No MrX.");
            if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Empty graph.");

            for (Player d : detectives) {
                if (d.isMrX()) throw new IllegalArgumentException("More than one MrX.");
                if (d.has(Ticket.SECRET) || d.has(Ticket.DOUBLE)) throw new IllegalArgumentException("Illegal tickets.");
                for(Player d2: detectives){
                    if((d!=d2) && (d.location() == d2.location())) throw new IllegalArgumentException("Detectives overlap.");
                }
            }
        }


        @Nonnull
        @Override
        public GameSetup getSetup() {
            return setup;
        }

        @Nonnull
        @Override
        public ImmutableSet<Piece> getPlayers() {
            Set<Piece> players = new HashSet<>();
            for(Player d : detectives){
                players.add(d.piece());
            }
            if(!remaining.isEmpty()) players.add(mrX.piece());
            ImmutableSet<Piece> allPieces = ImmutableSet.copyOf(players);
            return allPieces;
        }

        @Nonnull
        @Override
        public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
            for(Player d : detectives){
                if(detective == d.piece()) return Optional.of(d.location());
            }
            return Optional.empty();

        }

        @Nonnull
        @Override
        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
//            TicketBoard tb = new TicketBoard() {
//                @Override
//                public int getCount(@Nonnull Ticket ticket) {
//                    return .tickets().get(ticket);
//                }
//            };

            if (piece.isMrX()) {
                TicketBoard tb = new TicketBoard() {
                    @Override
                    public int getCount(@Nonnull Ticket ticket) {
                        return mrX.tickets().get(ticket);
                    }
                };
                return Optional.of(tb);
            }
            for (Player d : detectives){
                if (d.piece() == piece) {
                    TicketBoard tb = new TicketBoard() {
                        @Override
                        public int getCount(@Nonnull Ticket ticket) {
                            return d.tickets().get(ticket);
                        }
                    };
                    return Optional.of(tb);
                }
            }
            return Optional.empty();
        }

        @Nonnull
        @Override
        public ImmutableList<LogEntry> getMrXTravelLog() {
            return log;
        }

        @Nonnull
        @Override
        public ImmutableSet<Piece> getWinner() {
            return ImmutableSet.of();
        }

        @Nonnull
        @Override
        public ImmutableSet<Move> getAvailableMoves() {
            ImmutableSet<Move> mv = ImmutableSet.copyOf(moves);
            if (!mv.isEmpty()) return mv;
            return null;
        }

        @Nonnull
        @Override
        public GameState advance(Move move) {
            return null;
        }
    }


    @Nonnull @Override public GameState build(GameSetup setup,
                                              Player mrX,
                                              ImmutableList<Player> detectives) {
        return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

    }



    private static Set<SingleMove> makeSingleMoves(GameSetup setup,
                                                   List<Player> detectives,
                                                   Player player,
                                                   int source) {
        Set<SingleMove> possibleMoves = Sets.newHashSet();
        Boolean NotOccupied = false;
        Boolean haveTicket = false;
        for (int destination : setup.graph.adjacentNodes(source)) {
            for (Player detective : detectives) {
                if (detective.location() == destination) {
                    NotOccupied = true;
                }
            }
            for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
                if (player.has(t.requiredTicket())) {
                    haveTicket = true;
                }
                if (NotOccupied && (haveTicket || player.has(Ticket.SECRET))) {
                    possibleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
                }
            }
        }
        return possibleMoves;
    }

}
