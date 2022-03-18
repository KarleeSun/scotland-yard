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

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.REVEAL_MOVES;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	@Nonnull
	@Override
	public GameState build(GameSetup setup,
						   Player mrX,
						   ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}

	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log,
							final Player mrX,
							final List<Player> detectives) {
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
            this.moves = getAvailableMoves();
			//error checking
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Empty Move.");
			if (detectives.isEmpty()) throw new IllegalArgumentException("Empty Detectives.");
			if(!mrX.isMrX()) throw new IllegalArgumentException("No MrX.");
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Empty Graph.");
			for (Player d : detectives) {
				if (d.isMrX()) throw new IllegalArgumentException("More than one MrX.");
				if(d.has(Ticket.SECRET)) throw new IllegalArgumentException("Detective Should Not Has Secret Ticket.");
				if(d.has(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective Should Not Has Double Ticket.");
				for(Player d2 : detectives){
					if((d != d2) && (d.location() == d2.location()))
						throw new IllegalArgumentException("Duplicate Location.");
				}
			}
		}
		
		public Player getPlayer(Piece piece){
			if (piece.isMrX()) return mrX;
			for (Player d : detectives) {
			if (d.piece() == piece) return d;
		}
			return null;
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
			return ImmutableSet.copyOf(players);
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for(Player d : detectives){
				if(detective == d.piece()) return Optional.of(d.location());
			}
			return Optional.empty();
		}

        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
            abstract class PlayerTickets implements TicketBoard {

            }
            //if piece is Mr.X
            if (piece.isMrX()) {
                TicketBoard ticketBoard = new PlayerTickets() {
                    @Override
                    public int getCount(@Nonnull Ticket ticket) {
                        return mrX.tickets().getOrDefault(Objects.requireNonNull(ticket), 0);
                    }
                };
                return Optional.of(ticketBoard);
            }
            //check this piece represents which detective
            for (Player d : detectives) {
                if (d.piece() == piece) {
                    TicketBoard tb = new PlayerTickets() {
                        @Override
                        public int getCount(@Nonnull Ticket ticket) {
                            return d.tickets().getOrDefault(Objects.requireNonNull(ticket), 0);
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
            System.out.println("winner");
            Set<Piece> pieceOfDetectives = new HashSet<>();
            for(Player d: detectives){
                pieceOfDetectives.add(d.piece());
            }
//            Set<Piece> pieceOfDetectives = Set.copyOf(getPlayers());
//            pieceOfDetectives.remove(mrX.piece());
            System.out.println("piece of detectives: "+pieceOfDetectives);
            Set<Piece> winner = new HashSet<>();
            Piece currentPiece = remaining.iterator().next();
            Boolean haveAvailableMoves = false;

            //the situation that detectives win
            if (currentPiece.isMrX() && getAvailableMoves().isEmpty()) {
                winner.addAll(pieceOfDetectives);
                System.out.println("winner: "+ winner);
                return ImmutableSet.copyOf(winner);
            }

            for (Player d : detectives) {
                if (d.location() == mrX.location()) {
                    winner.addAll(pieceOfDetectives);
                    System.out.println("winner: "+ winner);
                    return ImmutableSet.copyOf(winner);
                }
            }

            //the situation that mrX wins
            if (log.size() == moves.size()) {
                winner.add(mrX.piece());
                System.out.println("winner: "+ winner);
                return ImmutableSet.copyOf(winner);
            }

            if (currentPiece.isDetective()) {
                for (Player d : detectives) {
                    if (!getAvailableMoves().isEmpty()) {
                        haveAvailableMoves = true;
                    }
                }
                if (!haveAvailableMoves) {
                    winner.addAll(pieceOfDetectives);
                    System.out.println("winner: "+ winner);
                    return ImmutableSet.copyOf(winner);
                }
            }

            System.out.println("winner: "+ winner);
            return ImmutableSet.of();
        }

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
            Set<Move> availableMoves = new HashSet<>();
            ////check if there is still round left for moves
//            if (setup.moves.size()-log.size() < 1) {
//                ImmutableSet<Move> emptyMove = ImmutableSet.copyOf(availableMoves);
//                return emptyMove;
//            }
            //set mr X as default current player
            Player currentPlayer = mrX;
            Piece currentPiece;
            currentPiece = remaining.iterator().next();
            if (currentPiece.isMrX() && (setup.moves.size()-log.size() > 1)) {
                availableMoves.addAll(makeDoubleMoves(setup, detectives, currentPlayer, currentPlayer.location()));
            } else {
                for (Player detective : detectives) {
                    if (detective.piece() == currentPiece)
                        currentPlayer = detective;
                }
            }
            availableMoves.addAll(makeSingleMoves(setup, detectives, currentPlayer, currentPlayer.location()));
            ImmutableSet<Move> moves = ImmutableSet.copyOf(availableMoves);
            return moves;
        }

		private static Set<SingleMove> makeSingleMoves(GameSetup setup,
													   List<Player> detectives,
													   Player player, //this player indicates the one who moves in this game state
													   int source) {
            Set<SingleMove> possibleMoves = Sets.newHashSet();
            Set<Integer> destination = new HashSet<>();
            destination = setup.graph.adjacentNodes(source);
//            System.out.println("possible destinations: "+ destination);
            //for each single destination, decide whether the point is occupied and whether the player has required ticket
            for (int d : destination) {
                Boolean notOccupied = true;
                for (Player detective : detectives) {
                    if (detective.location() == d) {
                        notOccupied = false;
                    }
                }
                //iterate through set containing all possible transportation from source to destination d
                for (Transport t : setup.graph.edgeValueOrDefault(source, d, ImmutableSet.of())) {
//                    System.out.println("not occupied: "+notOccupied);
                    if (notOccupied) { //if there is no detective on this destination and player has proper ticket
                        if (player.hasAtLeast(t.requiredTicket(), 1)) {
                            possibleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), d));
                        }
//                        System.out.println("ticket has enough: "+ player.hasAtLeast(t.requiredTicket(), 1));
                        if (player.hasAtLeast(Ticket.SECRET, 1)){
                            possibleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, d));
                        }
                    }
//                    System.out.println("transport: " + t + ", current possible d: "+d+", possibleMoves:" + possibleMoves);
                }

            }
            return possibleMoves;
        }

        private static Set<DoubleMove> makeDoubleMoves(GameSetup setup,
                                                       List<Player> detectives,
                                                       Player player,
                                                       int source) {
            if(!player.hasAtLeast(Ticket.DOUBLE,1))
                return new HashSet<>();
            Set<DoubleMove> doubleAvailableMoves = new HashSet<>(); //store available double moves
            List<SingleMove> firstAvailableMoves = new ArrayList<>(); //store first available moves
            //store all available first move by invoke makeSingleMove method
            firstAvailableMoves.addAll(makeSingleMoves(setup,detectives,player, player.location()));
//            System.out.println("first available moves:" + firstAvailableMoves);
            //iterate through all possible single moves and store its corresponding second move
            for (SingleMove firstMove : firstAvailableMoves){
//                System.out.println("first move:" +firstMove);
                List<SingleMove> secondAvailableMoves = new ArrayList<>(); //store second available moves
                secondAvailableMoves.addAll(makeSingleMoves(setup,detectives,player, firstMove.destination));
//                System.out.println("second available move:" +secondAvailableMoves);
                //iterate through all possible second move for a particular first move and create new double move and
                //store it
                //
                //Check if still have tickets for second move
                Ticket ticketUsed = firstMove.ticket;
                Integer ticketLeft = player.tickets().get(ticketUsed);
                if(!secondAvailableMoves.isEmpty()) {
                    for (SingleMove secondMove : secondAvailableMoves) {
//                        System.out.println("first move:" +firstMove + "second move:" + secondMove);

                        if (!(secondMove.ticket == ticketUsed) || ticketLeft>=2) {
                            doubleAvailableMoves.add(
                                new DoubleMove(
                                    player.piece(),
                                    firstMove.source(),
                                    firstMove.ticket,
                                    firstMove.destination,
                                    secondMove.ticket,
                                    secondMove.destination
                                )
                            );
                        }
                    }
                }
            }
            return doubleAvailableMoves;
        }
		@Nonnull
        @Override
        public GameState advance(Move move){
            System.out.println("move :" + move);
            System.out.println("moves:" + moves);
            if(!remaining.iterator().next().equals(move.commencedBy())){
                return new MyGameState(setup,remaining,log,mrX,detectives);
            }
            if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
            if(!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
            //implements:
            // 1. add move to log if it's mr X's move
            // 2. update tickets state(include giving to mrX
            // 3. update player's position
            List<LogEntry> updatedLog = new ArrayList<>();
            updatedLog.addAll(log);
            Player updatedNewPlayer = move.accept(new Visitor<Player>() {
                @Override
                public Player visit(SingleMove move) {
                    Piece currentPiece = move.commencedBy();
                    Ticket ticketUsed = move.ticket;
                    Player currentPlayer = mrX;
                    //if current player is detective, give ticket to mr X
                    for (Player detective : detectives) {
                        if (detective.piece() == currentPiece) {
                            Map<ScotlandYard.Ticket, Integer> mrXTickets = Map.copyOf(mrX.tickets());
                            mrXTickets.replace(ticketUsed, mrX.tickets().get(ticketUsed) + 1);
                            currentPlayer = detective;
                        }
                    }
                    //if it's mrX's move, add move to the log (determine whether its reveal or hidden)
                    if(currentPlayer.isMrX()){
                        updatedLog.add(REVEAL_MOVES.contains(log.size()+1)
                                ? LogEntry.reveal(ticketUsed, move.destination)
                                : LogEntry.hidden(ticketUsed));
                    }
                    //Update current player's tickets
                    Map<ScotlandYard.Ticket, Integer> updatedMap = new HashMap<>();
                    updatedMap.putAll(currentPlayer.tickets());
                    updatedMap.replace(ticketUsed, currentPlayer.tickets().get(ticketUsed) - 1);
                    ImmutableMap<ScotlandYard.Ticket, Integer> immutableUpdatedMap = ImmutableMap.copyOf(updatedMap);
                    return new Player(currentPiece, immutableUpdatedMap, move.destination);
                }
                //implements:
                // 1. add move to log
                // 2. update tickets state
                // 3. update mr X's position
                @Override
                public Player visit(DoubleMove move) {
                    updatedLog.add(REVEAL_MOVES.contains(log.size()+1)
                            ? LogEntry.reveal(move.ticket1, move.destination1)
                            : LogEntry.hidden(move.ticket1));
                    updatedLog.add(REVEAL_MOVES.contains(log.size()+2)
                            ? LogEntry.reveal(move.ticket2, move.destination2)
                            : LogEntry.hidden(move.ticket2));
                    //update ticket state
                    Map<ScotlandYard.Ticket, Integer> updatedMap = Map.copyOf(mrX.tickets());
                    updatedMap.replace(move.ticket1, mrX.tickets().get(move.ticket1) - 1);
                    updatedMap.replace(move.ticket2, mrX.tickets().get(move.ticket2) - 1);
                    ImmutableMap<ScotlandYard.Ticket, Integer> immutableUpdatedMap = ImmutableMap.copyOf(updatedMap);
                    //update mr X's position
                    return new Player(mrX.piece(), immutableUpdatedMap, move.destination2);
                }
            });
            //swap to next players turn, if no player left, swap to mrX's turn
            //if it's mrX turn, swap to detectives' turn by add all detectives into the remaining list
            Set<Piece> updatedRemaining = new HashSet<>();
            updatedRemaining.addAll(remaining);
            updatedRemaining.remove(updatedRemaining.iterator().next());
            if(updatedNewPlayer.isMrX()) {
                for(Player d : detectives){
                    updatedRemaining.add(d.piece());
                }
            }
            //if it's detectives' turn swap to next detectives, if it's empty, swap to mrX
            else if (updatedRemaining.isEmpty()){
                updatedRemaining.add(mrX.piece());
            }
            Player updatedMrX = updatedNewPlayer;
            List<Player> updatedDetectives = List.copyOf(detectives);
            if(move.commencedBy().isMrX()) {
                updatedMrX = updatedNewPlayer;
            }
            else{
                updatedMrX = mrX;
                for(Player d : detectives){
                    if(updatedNewPlayer.piece().equals(d.piece())){
                        updatedDetectives.add(updatedDetectives.indexOf(d), updatedNewPlayer);
                        updatedDetectives.remove(d);
                    }
                }
            }
            // make things immutable
            ImmutableSet<Piece> immutableUpdatedRemaining = ImmutableSet.copyOf(updatedRemaining);
            ImmutableList<LogEntry> immutableUpdatedLog = ImmutableList.copyOf(updatedLog);
            return new MyGameState(setup, immutableUpdatedRemaining, immutableUpdatedLog, updatedMrX, updatedDetectives);

        }
	}
}
