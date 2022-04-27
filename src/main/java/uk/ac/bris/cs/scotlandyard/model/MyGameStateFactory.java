package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.List;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public final class MyGameStateFactory implements Factory<GameState> {
	@Nonnull
	@Override
	public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

	private final class MyGameState implements GameState {
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining; //equals mrX or all the other detectives
		private final ImmutableList<LogEntry> log; //record of moves of mrX
		private final Player mrX;
		private final List<Player> detectives;
		private final ImmutableSet<Move> moves; //to store all available moves for players
		private ImmutableSet<Piece> winner; //to store the winner of the game

		private MyGameState(final GameSetup setup, final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log, final Player mrX, final List<Player> detectives) {
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
			detectives.forEach(d -> players.add(d.piece()));
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
			detectives.forEach(d -> detectivePieces.add(d.piece()));
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
		 * @return all available moves of the list of players given by invoking makeSingleMove and makeDoubleMove
		 */
		private ImmutableSet<Move> giveMoves(List<Player> players){
			Set<Move> moves = new HashSet<>();
			players.forEach(p -> moves.addAll(makeSingleMoves(setup, detectives, p, p.location())));
			if(log.size() + 2 <= setup.moves.size())
				players.forEach(p -> moves.addAll(makeDoubleMoves(setup, detectives, p, p.location())));
			return ImmutableSet.copyOf(moves);
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			if(!winner.isEmpty() && !giveWinner().isEmpty()) return ImmutableSet.of();          /* Already have winner*/
			List<Player> players = new ArrayList<>();
			remaining.forEach(p -> players.add(getPlayer(p)));
			return giveMoves(players);          /* game is not over, return all availableMoves by invoke giveMoves*/
		}

		/**
		 * @return a Set of all possible single move a player can make
		 */
		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives,
													   Player player, int source) {
			Set<SingleMove> possibleMoves = Sets.newHashSet();
			Set<Integer> destination = setup.graph.adjacentNodes(source);
			for (int d : destination) {
				boolean notOccupied = true;          /* node not occupied by any detective*/
				for (Player detective : detectives) {
					if (detective.location() == d) {
						notOccupied = false;
						break;
					}
				}
				//iterate through set containing all possible transportation from source to destination
				if (notOccupied) {
					if (player.hasAtLeast(Ticket.SECRET, 1)){          /* with SECRET MOVES */
						possibleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, d));
					}
					//get all possible transportations and add ones with available tickets
					Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, d, ImmutableSet.of())).stream()
							.filter(t -> player.hasAtLeast(t.requiredTicket(), 1))
							.forEach(t -> possibleMoves.add(
									new SingleMove(player.piece(), source, t.requiredTicket(), d)));
				}
			}
			return possibleMoves;
		}
		/*
		doubleAvailableMove: all available double moves to be returned
		firstAvailableMove: all available first move of double move
		secondAvailableMove: all available second move of double move
		 */
		private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives,
													   Player player, int source) {
			if(!player.hasAtLeast(Ticket.DOUBLE,1)) return new HashSet<>();		/*no Double ticket left*/
			Set<DoubleMove> doubleAvailableMoves = new HashSet<>();
			//store all available first move by invoke makeSingleMove method
			List<SingleMove> firstAvailableMoves = new ArrayList<>(makeSingleMoves(setup, detectives, player, source));
			//iterate through all possible single moves and store its corresponding second move
			for (SingleMove firstMove : firstAvailableMoves) {
				makeSingleMoves(setup, detectives, player, firstMove.destination).stream()
						//if used same ticket check if >=2 of that ticket left
						.filter(secMove -> secMove.ticket != firstMove.ticket
								|| player.tickets().get(secMove.ticket) >= 2)
						.forEach(secMove -> doubleAvailableMoves.add(new DoubleMove(player.piece(), firstMove.source(),
								firstMove.ticket, firstMove.destination, secMove.ticket, secMove.destination)));
			}
			return doubleAvailableMoves;
		}

		@Nonnull
		@Override
		public GameState advance(Move move){
			//error checking
			if(!remaining.contains(move.commencedBy())) return new MyGameState(setup,remaining,log,mrX,detectives);
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			if(!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			List<LogEntry> updatedLog = new ArrayList<>(log);
			//using visitor pattern to get information of the player who made the move
			Player updatedNewPlayer = move.accept(new Visitor<>() {
				@Override
				public Player visit(SingleMove move) {
					final Piece currentPiece = move.commencedBy();
					final Ticket ticketUsed = move.ticket;
					Player currentPlayer = mrX;                    /* set mrX as default currentPlayer*/
					for (Player detective : detectives) {          /* check if move made by detective*/
						if (detective.piece() == currentPiece) currentPlayer = detective;
					}
					//if it's mrX's move, add move to the log (determine whether its reveal or hidden)
					if (currentPlayer.isMrX()) {
						updatedLog.add(setup.moves.get(updatedLog.size())
								? LogEntry.reveal(ticketUsed, move.destination)
								: LogEntry.hidden(ticketUsed));
					}
					//Update current player's tickets
					Map<Ticket, Integer> updatedMap = new HashMap<>(currentPlayer.tickets());
					updatedMap.replace(ticketUsed, currentPlayer.tickets().get(ticketUsed) - 1);
					return new Player(currentPiece, ImmutableMap.copyOf(updatedMap), move.destination);
				}

				@Override
				public Player visit(DoubleMove move) {
					updatedLog.add(setup.moves.get(updatedLog.size())
							? LogEntry.reveal(move.ticket1, move.destination1)
							: LogEntry.hidden(move.ticket1));
					updatedLog.add(setup.moves.get(updatedLog.size())
							? LogEntry.reveal(move.ticket2, move.destination2)
							: LogEntry.hidden(move.ticket2));
					//update ticket state
					Map<Ticket, Integer> updatedMap = new HashMap<>(mrX.tickets());
					updatedMap.replace(move.ticket1, mrX.tickets().get(move.ticket1) - 1);
					updatedMap.replace(move.ticket2, mrX.tickets().get(move.ticket2) - 1);
					updatedMap.replace(Ticket.DOUBLE, mrX.tickets().get(Ticket.DOUBLE) - 1);
					return new Player(mrX.piece(), ImmutableMap.copyOf(updatedMap), move.destination2);
				}
			});
			//give ticket to mrX if its detectives turn
			Map<Ticket, Integer> mrXTickets = new HashMap<>(mrX.tickets());
			Player playerBeforeUpdate = getPlayer(updatedNewPlayer.piece());
			//check what ticket has been used
			Objects.requireNonNull(playerBeforeUpdate).tickets().keySet().stream()
					.filter(t -> !updatedNewPlayer.tickets().get(t).equals(playerBeforeUpdate.tickets().get(t)))
					.forEach(t -> mrXTickets.replace(t, mrX.tickets().get(t) + 1));
			Player updatedMrX = new Player(mrX.piece(),ImmutableMap.copyOf(mrXTickets), mrX.location());
			//update remaining
			Set<Piece> updatedRemaining = new HashSet<>();
			//copy players who was in the remaining to the updatedRemaining and remove player who made current move
			remaining.stream().filter(p -> !giveMoves(List.of(Objects.requireNonNull(getPlayer(p)))).isEmpty())
					.filter(p -> !p.equals(move.commencedBy()))
					.forEach(updatedRemaining::add);
			//swap to detectives' turn, add detectives who still can make move
			if(move.commencedBy().isMrX()) {
				detectives.stream()
						.filter(d -> !giveMoves(List.of(d)).isEmpty())
						.forEach(d -> updatedRemaining.add(d.piece()));
			}
			//swap to mrX's turn
			else if (updatedRemaining.isEmpty()) updatedRemaining.add(mrX.piece());
			//update detectives
			List<Player> updatedDetectives = new ArrayList<>(detectives);
			if(move.commencedBy().isMrX()) updatedMrX = updatedNewPlayer;          /*rewrite mrX's ticket status*/
			else {
				for (Player d : detectives) {                 /*update detectives list by replacing with new player*/
					if (updatedNewPlayer.piece().equals(d.piece())) {
						updatedDetectives.add(updatedDetectives.indexOf(d), updatedNewPlayer);
						updatedDetectives.remove(d);
					}
				}
			}
			//update game state for the next move
			return new MyGameState(setup, ImmutableSet.copyOf(updatedRemaining), ImmutableList.copyOf(updatedLog),
					updatedMrX, updatedDetectives);
		}
	}
}