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
import tablut.TablutMove;

public class MyTools {
	public static final long MAX_TIME = 1900;
	
	public int[] weights;
	private volatile boolean timeOver; //NOT STATIC !
	private volatile Timer timer;
	final TimerTask timeoutTask = new TimerTask() {
		public void run() {
			System.out.println("time over !");
			timeOver = true;
		}
	};

	public MyTools(int[] weights) {
		this.weights = weights;
		this.timeOver = false;
	}

	/*
	 * MiniMax with Alpha Beta Pruning algorithm.
	 * 
	 */
	
	/**
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @param bs
	 * @return
	 */
	public Pair alphaBetaPruning(int maxDepth, TablutBoardState bs){
		timeOver = false;
		timer = new Timer();
		
		timer.schedule(timeoutTask, MAX_TIME);		
        
		if (cutoff(maxDepth, bs)){
			timer.cancel();
			return new Pair(Evaluation(bs), null);
		}
		
		
		final double alpha = Double.NEGATIVE_INFINITY;
		final double beta = Double.POSITIVE_INFINITY;
		
		long startTime = System.nanoTime();
		
		if (bs.getTurnPlayer() == TablutBoardState.SWEDE) {
			// -- re-implement a slight different version of Max Value
			List<TablutMove> options = bs.getAllLegalMoves();

			double newAlpha = alpha;
			Move bestMove = null;
				
			// Iterate over depths (just like iterative deepening)
			for (int d = 3; d <= maxDepth; d++) {
				for (TablutMove m : options){
					TablutBoardState newBS = (TablutBoardState) bs.clone();
					newBS.processMove(m);
					double score = MinValue(d - 1, newAlpha, beta, newBS);
					
					/*
					 *  Handle time management, return best so far.
					 *  Note that the last possible move is not taken into consideration,
					 *  as it might be incorrect due to early stopping of calculations.
					 */
					if (timeOver){
						System.out.println("Abort at depth: " + d + " step: " + options.indexOf(m) + " t = " + (System.nanoTime() - startTime));
						System.out.flush();
						timer.cancel();
						return new Pair(newAlpha, bestMove);
					}
					
					/*
					 * Normal case: update new best move if a better score
					 * could be achieved taking it.
					 * Note: No pruning on the top level!
					 */
					if (score > newAlpha) {
						newAlpha = score;
						bestMove = m;
					}
				}
			}
			
			return new Pair(newAlpha, bestMove);
		} else {
			// -- re-implement a slight different version of Min Value
			List<TablutMove> options = bs.getAllLegalMoves();

			double newBeta = beta;
			Move bestMove = null;
			
			for (int d = 3; d <= maxDepth; d++) {
				for (TablutMove m : options){
					TablutBoardState newBS = (TablutBoardState) bs.clone();
					newBS.processMove(m);
					
					double score = MaxValue(d - 1, alpha, newBeta, newBS);
					
					/*
					 *  Handle time management, return best so far.
					 *  Note that the last possible move is not taken into consideration,
					 *  as it might be incorrect due to early stopping of calculations.
					 */
					if (timeOver){
						System.out.println("Abort at depth: " + d + " step: " + options.indexOf(m) + " t = " + (System.nanoTime() - startTime));
						System.out.flush();
						timer.cancel();
						return new Pair(newBeta, bestMove);
					}
					
					/*
					 * Normal case: update new best move if a better score
					 * could be achieved taking it.
					 * Note: No pruning on the top level!
					 */
					if (score < newBeta){
						newBeta = score;
						bestMove = m;
					}
				}				
			}
			timer.cancel();
			System.out.println("Done with my shit");
			return new Pair(newBeta, bestMove);
		}
	}
	
	
    private double MaxValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth, bs)){
			return Evaluation(bs);
		}

		List<TablutMove> options = bs.getAllLegalMoves();

		double newAlpha = alpha;
		for (TablutMove m : options){
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			double score = MinValue(depth - 1, newAlpha, beta, newBS);
			
			if (score > newAlpha) {
				newAlpha = score;
			}
			
			// Pruning
			if (newAlpha >= beta){
				return beta;
			}
		}
		return newAlpha;
	}
    
    private double MinValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth, bs)){
			return Evaluation(bs);
		}
		List<TablutMove> options = bs.getAllLegalMoves();

		double newBeta = beta;
		for (TablutMove m : options){
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			
			double score = MaxValue(depth - 1, alpha, newBeta, newBS);
			if (score < newBeta){
				newBeta = score;
			}
			
			// Pruning
			if (alpha >= newBeta){
				return alpha;
			}
		}
		return newBeta;
	}
	
	private boolean cutoff(int d, TablutBoardState bs) {
		return d <= 0 || bs.gameOver() || timeOver;
	}
	
	
	/**
	 * Evaluation function.
	 * High value good for SWEDES (white).
	 * Low value good for MUSCOVITE (black).
	 * 
	 * @param bs
	 * @return evaluated board state
	 */
	private double Evaluation(TablutBoardState bs){
		double value = 0;
		
		if (bs.gameOver() ) {
			/*
			 * Assign very large positive and negative numbers for loosing/winning.
			 * Take away turn number to make it terminate more quickly.
			 * 
			 * FIXME how does removing this turn-number impact performance?
			 * Should I take it away for tournament?
			 * I guess its not too important, since it only comes into play
			 * when the player is in a very favorable position.
			 */
			if (bs.getWinner() == TablutBoardState.SWEDE){
				return 10000 - bs.getTurnNumber(); // Trick to make it finish as quickly as possible.		
			} else if (bs.getWinner() == Board.DRAW){
				return 0;
			} else if (bs.getWinner() == TablutBoardState.MUSCOVITE) {
				return -10000 + bs.getTurnNumber();
			}
			// Note: return directly otherwise weird behavior may happen ! (Trust me)
		}
		
		// HEURISTIC 1: Number of pieces difference.
		int piecesDifference = bs.getNumberPlayerPieces(TablutBoardState.SWEDE) 
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE);
		value += weights[0] * piecesDifference;

		
		// HEURISTIC 2: King Mobility.
		if (weights[1] != 0){
			int mobilityKing = bs.getLegalMovesForPosition(bs.getKingPosition()).size();
			value += weights[1] * mobilityKing;
		}
		
		// HEURISTIC 3: General Mobility
		int count = 0;
		if (weights[2] != 0){	// this line avoids unnecessary computations
			int mult = (bs.getTurnPlayer() == TablutBoardState.SWEDE) ? 1 : -1;
			for (Coord c : bs.getPlayerPieceCoordinates()) {
				count += mult * bs.getLegalMovesForPosition(c).size();
			}
			for (Coord c : bs.getOpponentPieceCoordinates()) {
				count -= mult * bs.getLegalMovesForPosition(c).size();
			}
			value += weights[2] * count;			
		}
		
		// HEURISTIC 4: black pawns near corners
//		count = 0;
//		if (weights[3] != 0){	// this line avoids unnecessary computations
//			HashSet<Coord> pieces = (bs.getTurnPlayer() == TablutBoardState.SWEDE) ? bs.getOpponentPieceCoordinates() : bs.getPlayerPieceCoordinates();
//			
//			for(Coord p : pieces){
//				count += Coordinates.distanceToClosestCorner(p);
//			}
//			value += weights[3] * count;
//		}
//		
		return value;
		
		/*
		 * Ideas:
		 * 	make it finish quickly, don't goof around ! (remove points from score -> less pruning?
		 * 
		 * 	total number of pawns on board, (eating is good) 
		 * 
		 * 	remove symmetric states
		 * 
		 * genetic algorithm
		 */
	}
	
	/**
	 * Helper class.
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
