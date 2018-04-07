package student_player;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import boardgame.Board;
import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutBoardState.Piece;
import tablut.TablutMove;

/**
 * 
 * This class implements the basic Alpha Beta Pruning Algorithm. It uses a basic
 * Evaluation function at the leaves.
 * 
 * The basic structure of the code was taken from the Lecture slides.
 * 
 * @author Yann Vonlanthen (260808862)
 *
 */
public class AlphaBetaPruning {

	// Constants
	public static final double INIT_ALPHA = Double.NEGATIVE_INFINITY;
	public static final double INIT_BETA = Double.POSITIVE_INFINITY;
	public static final long MAX_TIME = 1900;
	public static final int HEURISTIC_N = 6;

	// Class Attributes
	public double[] weights;
	private volatile boolean timeOver; // Volatile because of concurrency issues
	private volatile Timer timer;

	/**
	 * Constructor.
	 * 
	 * @param weights
	 */
	public AlphaBetaPruning(double[] weights) {
		this.weights = weights;
		this.timeOver = false;
	}

	/**
	 * Get Best Move according to the given heuristic, using alpha beta pruning.
	 * 
	 * @param startingDepth
	 * @param maxDepth
	 * @param bs
	 * @return
	 */
	public Pair getBestMove(int startingDepth, int maxDepth, TablutBoardState bs) {
		// Set up a Timer for time management
		timeOver = false;
		timer = new Timer();
		TimerTask timeoutTask = new TimerTask() {
			public void run() {
				System.out.println("time over !");
				timeOver = true;
			}
		};
		timer.schedule(timeoutTask, MAX_TIME);

		// Only possible if game isn't over yet.
		if (cutoff(maxDepth, bs)) {
			timer.cancel();
			return new Pair(Evaluation(bs), null);
		}

		// FIXME only for debugging purposes, second way to keep time
		long startTime = System.nanoTime();

		// Perform first level of the MiniMax openly.
		if (bs.getTurnPlayer() == TablutBoardState.SWEDE) {

			// -- re-implement a slight different version of Max Value,
			// capable of handling time.
			List<TablutMove> options = bs.getAllLegalMoves();

			double newAlpha = INIT_ALPHA;
			Move bestMove = null;

			// Keep Track of best values at last depth
			// These values will get returned if time is over.
			// FIXME If this isn't done, I experienced weird behavior.
			Move bestMoveLastDepth = null;
			double bestAlphaLastDepth = INIT_ALPHA;

			// Iterate over depths (just like iterative deepening)
			// -> Done to take advantage of extra time, though
			// going straight to the perfect depth would be better ofc
			for (int d = startingDepth; d <= maxDepth; d++) {

				newAlpha = INIT_ALPHA;

				// Traverse all options
				for (TablutMove m : options) {
					TablutBoardState newBS = (TablutBoardState) bs.clone();
					newBS.processMove(m);
					double score = MinValue(d - 1, newAlpha, INIT_BETA, newBS);

					/*
					 * FIXME Handle time management, return best so far. Note
					 * that the last possible move is not taken into
					 * consideration, as it might be incorrect due to early
					 * stopping of calculations.
					 */
					if (timeOver) {
						// FIXME for debugging
						System.out.println("Abort at depth: " + d + " step: " + options.indexOf(m) + " t = "
								+ (System.nanoTime() - startTime));
						System.out.flush();
						timer.cancel();
						return new Pair(bestAlphaLastDepth, bestMoveLastDepth);
					}
					/*
					 * Normal case: update new best move if a better score could
					 * be achieved taking it. Note: No pruning on the top level!
					 */
					if (score > newAlpha) {
						newAlpha = score;
						bestMove = m;
						if (d == startingDepth) {
							bestMoveLastDepth = m;
							bestAlphaLastDepth = newAlpha;
						}
					}
				}

				// Keep track of overall best Moves
				bestMoveLastDepth = bestMove;
				bestAlphaLastDepth = newAlpha;
			}

			// We reached maxDepth without timeOut
			timer.cancel();
			return new Pair(bestAlphaLastDepth, bestMoveLastDepth);
		} else {
			// -- re-implement a slight different version of Min Value
			// -- analogous to code above, see comments above for explanations.
			List<TablutMove> options = bs.getAllLegalMoves();

			double newBeta = INIT_BETA;
			Move bestMove = null;

			Move bestMoveLastDepth = null;
			double bestBetaLastDepth = INIT_ALPHA;

			for (int d = startingDepth; d <= maxDepth; d++) {
				for (TablutMove m : options) {
					TablutBoardState newBS = (TablutBoardState) bs.clone();
					newBS.processMove(m);

					double score = MaxValue(d - 1, INIT_ALPHA, newBeta, newBS);

					if (timeOver) {
						System.out.println("Abort at depth: " + d + " step: " + options.indexOf(m) + " t = "
								+ (System.nanoTime() - startTime));
						System.out.flush();
						timer.cancel();
						return new Pair(bestBetaLastDepth, bestMoveLastDepth);
					}

					if (score < newBeta) {
						newBeta = score;
						bestMove = m;
						if (d == startingDepth) {
							bestMoveLastDepth = m;
							bestBetaLastDepth = newBeta;
						}
					}
				}

				bestMoveLastDepth = bestMove;
				bestBetaLastDepth = newBeta;
			}
			timer.cancel();
			return new Pair(bestBetaLastDepth, bestMoveLastDepth);
		}
	}

	/**
	 * MaxValue function used for alpha beta pruning.
	 * 
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @param bs
	 * @return
	 */
	private double MaxValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth, bs)) {
			return Evaluation(bs);
		}

		List<TablutMove> options = bs.getAllLegalMoves();

		double newAlpha = alpha;
		for (TablutMove m : options) {
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			double score = MinValue(depth - 1, newAlpha, beta, newBS);

			if (score > newAlpha) {
				newAlpha = score;
			}

			// Pruning
			if (newAlpha >= beta) {
				return beta;
			}
		}
		return newAlpha;
	}

	/**
	 * Min Value function used in Alpha Beta Pruning.
	 * 
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @param bs
	 * @return
	 */
	private double MinValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth, bs)) {
			return Evaluation(bs);
		}
		List<TablutMove> options = bs.getAllLegalMoves();

		double newBeta = beta;
		for (TablutMove m : options) {
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);

			double score = MaxValue(depth - 1, alpha, newBeta, newBS);
			if (score < newBeta) {
				newBeta = score;
			}

			// Pruning
			if (alpha >= newBeta) {
				return alpha;
			}
		}
		return newBeta;
	}

	/**
	 * Helper function that defines when to stop the recursion.
	 * 
	 * @param d
	 * @param bs
	 * @return
	 */
	private boolean cutoff(int d, TablutBoardState bs) {
		return d <= 0 || bs.gameOver() || timeOver;
	}

	/**
	 * Simple linear weighted Evaluation function. 
	 * High value good for SWEDES (white). 
	 * Low value good for MUSCOVITE (black).
	 * 
	 * @param bs
	 * @return evaluated board state
	 */
	private double Evaluation(TablutBoardState bs) {
		double value = 0;

		if (bs.gameOver()) {
			/*
			 * Assign very large positive and negative numbers for
			 * loosing/winning. Take away turn number to make it terminate more
			 * quickly.
			 */
			if (bs.getWinner() == TablutBoardState.SWEDE) {
				return 10000 - bs.getTurnNumber();
			} else if (bs.getWinner() == Board.DRAW) {
				return 0;
			} else if (bs.getWinner() == TablutBoardState.MUSCOVITE) {
				return -10000 + bs.getTurnNumber();
			}
			// Note: return directly otherwise weird behavior may happen !
		}

		// HEURISTIC 1: Number of pieces difference.
		int piecesDifference = bs.getNumberPlayerPieces(TablutBoardState.SWEDE)
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE) + 7;
		value += weights[0] * piecesDifference;

		// HEURISTIC 2: King Mobility
		if (weights[1] != 0) {
			int mobilityKing = getMoveNumberForPosition(bs, bs.getKingPosition());
			value += weights[1] * mobilityKing;
		}
		
		// HEURISTIC 3: Black Mobility
		/* (might impact ability to answer to what white does)
		 * can't be done easily since getAllLegalMoves works only for
		 * current player and the useful methods are private..
		 */
		if (weights[2] != 0) {
			int blackMobility = 0;

			HashSet<Coord> blackPieces;
			if (bs.getTurnPlayer() == TablutBoardState.SWEDE){
				blackPieces = bs.getOpponentPieceCoordinates();
			} else {
				blackPieces = bs.getPlayerPieceCoordinates();
			}
			for (Coord c : blackPieces) {
				blackMobility += getMoveNumberForPosition(bs, c);
			}
			
			// Black mobility is supposed to be bad for White
			value -= weights[2] * blackMobility;
		}

		// HEURISTIC 4: Distance of King to Corner
		// small distance is better for white
		value -= weights[3] * Coordinates.distanceToClosestCorner(bs.getKingPosition());

		// HEURISTIC 5: free rows and columns
		if (weights[4] != 0) {
			// Free rows and columns favors attacker ! (Or does it? Genetic Algo will tell)
			value += weights[5] * freeRowsAndColumns(bs); 
		}

		// HEURISTIC 6: Number of corners blocked by attacker
		// i.e the first piece on edge is white
		if (weights[5] != 0) {
			value += weights[5] * blockedByWhite(bs);
		}

		// HEURISTIC 7: Corner escape
		// TODO
		
		
		/*
		 * Note: I originally thought that too many heuristics were bad 
		 * for performance, and additionally and that they made the use of 
		 * a genetic algorithm much harder, since more dimensions imply 
		 * a longer execution time.
		 * 
		 * To my surprise that was actually not true, as even when adding
		 * lots of (non-optimized) heuristics, I could still achieve a
		 * similar depth. This counter-intuitive fact is certainly due
		 * to a larger amount of nodes that can be pruned, since the
		 * heuristics produce a larger range of values! :)
		 */

		return value;
	}
	
	/**
	 * Helper function.
	 * Disclaimer: similar to getAllLegalMoves(), code taken from there.
	 */
	public static int getMoveNumberForPosition(TablutBoardState bs, Coord c) {
		int count = 0;
		
		count += getLegalCoordsInDirection(bs, c, -1, 0); // move in -x direction
        count += getLegalCoordsInDirection(bs, c, 0, -1); // move in -y direction
        count += getLegalCoordsInDirection(bs, c, 1, 0); // move in +x direction
        count += getLegalCoordsInDirection(bs, c, 0, 1); // move in +y direction
		
		return count;
	}
	
	/**
	 * Helper function.
	 * Disclaimer: copied from TablutBoardState.
	 */
	 private static int getLegalCoordsInDirection(TablutBoardState bs, Coord start, int x, int y) {
		int count = 0;
		
        assert (!(x != 0 && y != 0));
        int startPos = (x != 0) ? start.x : start.y; // starting at x or y
        int incr = (x != 0) ? x : y; // incrementing the x or y value
        int endIdx = (incr == 1) ? TablutBoardState.BOARD_SIZE - 1 : 0; // moving in the 0 or 8 direction

        for (int i = startPos + incr; incr * i <= endIdx; i += incr) { // increasing/decreasing functionality
            // new coord is an x coord change or a y coord change
            Coord coord = (x != 0) ? Coordinates.get(i, start.y) : Coordinates.get(start.x, i);
            if (bs.coordIsEmpty(coord)) {
            	count++;
            } else {
                break;
            }
        }
        return count;
    }
	 
	/**
	 * Helper function that returns free rows and columns.
	 * 
	 * TODO write it cleaner and shorter. (sadly I ran out of time)
	 * 
	 * @param bs
	 * @return
	 */
	public static int freeRowsAndColumns(TablutBoardState bs) {
		int emptyRowAndCol = 0;
		for (int col = 0; col < TablutBoardState.BOARD_SIZE; col++) {
			boolean empty = true;
			for (int row = 0; row < TablutBoardState.BOARD_SIZE && empty; row++) {
				empty &= bs.getPieceAt(col, row) == Piece.EMPTY;
			}
			emptyRowAndCol += empty ? 1 : 0;
		}

		for (int row = 0; row < TablutBoardState.BOARD_SIZE; row++) {
			boolean empty = true;
			for (int col = 0; col < TablutBoardState.BOARD_SIZE && empty; col++) {
				empty &= bs.getPieceAt(col, row) == Piece.EMPTY;
			}
			emptyRowAndCol += empty ? 1 : 0;
		}

		return emptyRowAndCol;
	}

	/**
	 * Helper function that returns number of corners blocked
	 * by white pieces.
	 * 
	 * @param bs
	 * @return
	 */
	public static int blockedByWhite(TablutBoardState bs) {
		int blockedByWhite = 0;

		// left from top
		boolean empty = true;
		for (int i = 1; i < TablutBoardState.BOARD_SIZE && empty; i++) {
			Piece p = bs.getPieceAt(0, i);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// right from top
		empty = true;
		for (int i = 1; i < TablutBoardState.BOARD_SIZE && empty; i++) {
			Piece p = bs.getPieceAt(TablutBoardState.BOARD_SIZE - 1, i);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// top from left
		empty = true;
		for (int i = 1; i < TablutBoardState.BOARD_SIZE && empty; i++) {
			Piece p = bs.getPieceAt(i, 0);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// bottom from left
		empty = true;
		for (int i = 1; i < TablutBoardState.BOARD_SIZE && empty; i++) {
			Piece p = bs.getPieceAt(i, TablutBoardState.BOARD_SIZE - 1);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// left from bottom
		empty = true;
		for (int i = TablutBoardState.BOARD_SIZE - 1; i > 0 && empty; i--) {
			Piece p = bs.getPieceAt(0, i);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// right from bottom
		empty = true;
		for (int i = TablutBoardState.BOARD_SIZE - 1; i > 0 && empty; i--) {
			Piece p = bs.getPieceAt(TablutBoardState.BOARD_SIZE - 1, i);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// bottom from right
		empty = true;
		for (int i = TablutBoardState.BOARD_SIZE - 1; i > 0 && empty; i--) {
			Piece p = bs.getPieceAt(i, TablutBoardState.BOARD_SIZE - 1);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}

		// top from right
		empty = true;
		for (int i = TablutBoardState.BOARD_SIZE - 1; i > 0 && empty; i--) {
			Piece p = bs.getPieceAt(i, 0);
			if (p == Piece.WHITE) {
				blockedByWhite++;
				empty = false;
			} else if (p == Piece.BLACK) {
				empty = false;
			}
		}
		
		return blockedByWhite;
	}

	/**
	 * Helper class. Represents a value (from the evaluation function) and a
	 * Move that leads to that value.
	 */
	public static class Pair {
		public double getValue() {
			return x;
		}

		public Move getMove() {
			return m;
		}

		public Pair(double x, Move m) {
			this.x = x;
			this.m = m;
		}

		private double x;
		private Move m;
	}
}
