package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;


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
		private ImmutableSet<Piece> remaining; //equals mrX or all the other detectives
		private ImmutableList<LogEntry> log; //record of moves of mrX
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves; //to store all available moves for players
		private ImmutableSet<Piece> winner; //to store the winner of the game

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
            this.winner = ImmutableSet.of();
            this.winner = giveWinner();
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

        /**
         * @param piece of a player
         * @return instantiated player with this piece
         */
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
            players.add(mrX.piece());
            for(Player d : detectives){
				players.add(d.piece());
			}
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

        /**
         * @param piece the player piece
         * @return ticket board of a specific player
         */

        @Nonnull
        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
            Player player = getPlayer(piece);
            if(player == null) return Optional.empty();          /* if piece does not exist, return empty */
            TicketBoard ticketBoard = ticket ->
                    player.tickets().getOrDefault(Objects.requireNonNull(ticket), 0);
                    return Optional.of(ticketBoard);
        }

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {return log;}

        /**
         * Helper function that computes the winner based on current state
         * @return winner if exists, else return empty
         */
        private ImmutableSet<Piece> giveWinner() {
            //set up a set contains all detective pieces
            Set<Piece> detectivePieces = new HashSet<>();
            for (Player d : detectives) {
                detectivePieces.add(d.piece());
            }
            //mr X being caught
            for(Player detective : detectives){
                if(detective.location() == mrX.location()) return ImmutableSet.copyOf(detectivePieces);
            }
            //detective run out of moves
            if(giveMoves(detectives).isEmpty()) return ImmutableSet.of(mrX.piece());
            //mr X cannot move anymore
            if(giveMoves(List.of(mrX)).isEmpty() && remaining.contains(mrX.piece()))
                return ImmutableSet.copyOf(detectivePieces);
            //no more round left  && detectives all made moves for this round
            if (log.size() == setup.moves.size() && remaining.contains(mrX.piece()))
                return ImmutableSet.of(mrX.piece());
            //no winner yet
            return ImmutableSet.of();
        }

		@Nonnull
		@Override
        public ImmutableSet<Piece> getWinner() { return winner;}

        /**
         * @param players list
         * @return all available moves of the list of players given
         */
        private ImmutableSet<Move> giveMoves(List<Player> players){
            Set<Move> moves = new HashSet<>();
            for(Player p : players){
                moves.addAll(makeSingleMoves(setup, detectives, p, p.location()));
                if(log.size()+2 <= setup.moves.size()) {          /* Check where enough round for double move*/
                    moves.addAll(makeDoubleMoves(setup, detectives, p, p.location()));
                }
            }
            return ImmutableSet.copyOf(moves);
        }

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
            if(!winner.isEmpty()) return ImmutableSet.of();          /* Already have winner*/
            if(setup.moves.size() == log.size() && remaining.contains(mrX.piece())){
                return ImmutableSet.of();          /* All player moved for the last round, game finished*/
            }
            List<Player> players = new ArrayList<>();
            for(Piece p : remaining){
                players.add(getPlayer(p));
            }
            return giveMoves(players);          /* game is not over, return all availableMoves*/
        }

        /**
         * @return a Set of all possible single move a player can make
         */
		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives,
                                                       Player player, int source) {
            Set<SingleMove> possibleMoves = Sets.newHashSet();
            Set<Integer> destination = setup.graph.adjacentNodes(source);
            for (int d : destination) {
                boolean notOccupied = true;          /* node not occupied by detectives*/
                for (Player detective : detectives) {
                    if (detective.location() == d) {
                        notOccupied = false;
                        break;
                    }
                }
                //iterate through set containing all possible transportation from source to destination
                for (Transport t : setup.graph.edgeValueOrDefault(source, d, ImmutableSet.of())) {
                    if (notOccupied) {
                        if (player.hasAtLeast(t.requiredTicket(), 1)) {
                            possibleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), d));
                        }
//                        System.out.println("ticket has enough: "+ player.hasAtLeast(t.requiredTicket(), 1));
                        if (player.hasAtLeast(Ticket.SECRET, 1)){
                            possibleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, d));
                        }
                    }
                }

            }
            return possibleMoves;
        }

        //get all available moves in a double move
        private static Set<DoubleMove> makeDoubleMoves(GameSetup setup,
                                                       List<Player> detectives,
                                                       Player player,
                                                       int source) {
            if(!player.hasAtLeast(Ticket.DOUBLE,1))
                return new HashSet<>();
            Set<DoubleMove> doubleAvailableMoves = new HashSet<>(); //store available double moves
            List<SingleMove> firstAvailableMoves = new ArrayList<>(); //store first available moves
            //store all available first move by invoke makeSingleMove method
            firstAvailableMoves.addAll(makeSingleMoves(setup,detectives,player, source));
            //iterate through all possible single moves and store its corresponding second move
            for (SingleMove firstMove : firstAvailableMoves){
                List<SingleMove> secondAvailableMoves = new ArrayList<>(); //store second available moves
                secondAvailableMoves.addAll(makeSingleMoves(setup,detectives,player, firstMove.destination));
                //iterate through all possible second move for a particular first move and create new double move and store it
                //Check if still have tickets for second move
                Ticket ticketUsed = firstMove.ticket;
                Integer ticketLeft = player.tickets().get(ticketUsed);
                if(!secondAvailableMoves.isEmpty()) {
                    for (SingleMove secondMove : secondAvailableMoves) {
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
            System.out.println(">advance being called.");
//            System.out.println("mr X before move: " + mrX);
            System.out.println("remaining: " + remaining + ", " + getPlayer(remaining.iterator().next()));
            System.out.println("available moves: " + getAvailableMoves());
            //error checking
            if(!remaining.contains(move.commencedBy())) return new MyGameState(setup,remaining,log,mrX,detectives);
            if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
            if(!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

            //implements:
            // 1. add move to log if it's mr X's move
            // 2. update tickets state(include giving to mrX
            // 3. update player's position
            List<LogEntry> updatedLog = new ArrayList<>();
            updatedLog.addAll(log);

            //using visitor pattern to get information of the player
            Player updatedNewPlayer = move.accept(new Visitor<Player>() {
                @Override
                public Player visit(SingleMove move) {
                    Piece currentPiece = move.commencedBy();
                    Ticket ticketUsed = move.ticket;
                    Player currentPlayer = mrX;
                    // check if move is made by detectives
                    for (Player detective : detectives) {
                        if (detective.piece() == currentPiece) {
                            currentPlayer = detective;
                        }
                    }
                    //if it's mrX's move, add move to the log (determine whether its reveal or hidden)
                    if(currentPlayer.isMrX()){
                        //System.out.println("updated log size: "+ updatedLog.size());
                        if(setup.moves.get(updatedLog.size()) == true){
                            updatedLog.add(LogEntry.reveal(ticketUsed, move.destination));
                        } else{
                            updatedLog.add(LogEntry.hidden(ticketUsed));
                        }
                    }
                    //Update current player's tickets
                    Map<ScotlandYard.Ticket, Integer> updatedMap = new HashMap<>();
                    updatedMap.putAll(currentPlayer.tickets());
                    updatedMap.replace(ticketUsed, currentPlayer.tickets().get(ticketUsed) - 1);
                    ImmutableMap<ScotlandYard.Ticket, Integer> immutableUpdatedMap = ImmutableMap.copyOf(updatedMap);
                    return new Player(currentPiece, immutableUpdatedMap, move.destination);
                }
                /**implements:
                 * 1. add move to log
                 * 2. update tickets state
                 * 3. update mr X's position
                 */
                @Override
                public Player visit(DoubleMove move) {
                    updatedLog.add(setup.moves.get(updatedLog.size()) == true
                            ? LogEntry.reveal(move.ticket1, move.destination1)
                            : LogEntry.hidden(move.ticket1));
                    updatedLog.add(setup.moves.get(updatedLog.size()) == true
                            ? LogEntry.reveal(move.ticket2, move.destination2)
                            : LogEntry.hidden(move.ticket2));
                    //update ticket state
                    Map<ScotlandYard.Ticket, Integer> updatedMap = new HashMap<>();
                    updatedMap.putAll(mrX.tickets());
                    updatedMap.replace(move.ticket1, mrX.tickets().get(move.ticket1) - 1);
                    updatedMap.replace(move.ticket2, mrX.tickets().get(move.ticket2) - 1);
                    updatedMap.replace(Ticket.DOUBLE, mrX.tickets().get(Ticket.DOUBLE) - 1);
                    ImmutableMap<ScotlandYard.Ticket, Integer> immutableUpdatedMap = ImmutableMap.copyOf(updatedMap);
                    //update mrX's position
                    return new Player(mrX.piece(), immutableUpdatedMap, move.destination2);
                }
            });
            //give ticket to mr
            Map<ScotlandYard.Ticket, Integer> mrXTickets = new HashMap<>();
            mrXTickets.putAll(mrX.tickets());
            Player whoGiveToMrX = getPlayer(updatedNewPlayer.piece());
            for(Ticket t : whoGiveToMrX.tickets().keySet()){
                if(!updatedNewPlayer.tickets().get(t).equals(whoGiveToMrX.tickets().get(t))){
                    mrXTickets.replace(t, mrX.tickets().get(t) + 1);
                }
            }
            ImmutableMap<Ticket, Integer> immutableMrXTicket = ImmutableMap.copyOf(mrXTickets);
            Player updatedMrX = new Player(mrX.piece(),immutableMrXTicket, mrX.location());

            //swap to next players turn, if no player left, swap to mrX's turn
            //if it's mrX turn, swap to detectives' turn by add all detectives into the remaining list
            Set<Piece> updatedRemaining = new HashSet<>();
            for(Piece p : remaining){
                Boolean stillCanMove = false;
                Player player = getPlayer(p);
                if(!giveMoves(List.of(getPlayer(p))).isEmpty());{
                    updatedRemaining.add(p);
                }
            }
            updatedRemaining.remove(move.commencedBy());
            if(move.commencedBy().isMrX()) {
                for(Player d : detectives){
                    if(!giveMoves(List.of(d)).isEmpty()) {
                        updatedRemaining.add(d.piece());
                    }
                }
            }
            //if it's detectives' turn swap to next detectives, if it's empty, swap to mrX
            else if (updatedRemaining.isEmpty()){
                updatedRemaining.add(mrX.piece());
            }
            List<Player> updatedDetectives = new ArrayList<>();
            updatedDetectives.addAll(detectives);
            if(move.commencedBy().isMrX()) {
                updatedMrX = updatedNewPlayer;
            }
                for(Player d : detectives){
                    if(updatedNewPlayer.piece().equals(d.piece())){
                        updatedDetectives.add(updatedDetectives.indexOf(d), updatedNewPlayer);
                        updatedDetectives.remove(d);
                    }
                }
            // make things immutable
            ImmutableSet<Piece> immutableUpdatedRemaining = ImmutableSet.copyOf(updatedRemaining);
            ImmutableList<LogEntry> immutableUpdatedLog = ImmutableList.copyOf(updatedLog);
            updatedLog.clear();
            //update game state for the next move
            System.out.println(move.commencedBy() + " made move " + move);
            return new MyGameState(setup, immutableUpdatedRemaining, immutableUpdatedLog, updatedMrX, updatedDetectives);
        }
	}
}
