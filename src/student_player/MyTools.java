package student_player;

import java.util.List;
import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class MyTools {

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
	public static Pair alphaBetaPruning(int depth, TablutBoardState bs){
        
		if (depth <= 0 || bs.gameOver()){
			return new Pair(Evaluation(bs), null);
		}
		double alpha = Double.NEGATIVE_INFINITY;
		double beta = Double.POSITIVE_INFINITY;
		
		if (bs.getOpponent() == TablutBoardState.MUSCOVITE) {
			return MaxValue(depth, alpha, beta, bs);
		} else {
			return MinValue(depth, alpha, beta, bs);
		}
	}
	
	
    private static Pair MaxValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth)){
			return new Pair(Evaluation(bs), null);
		}

		List<TablutMove> options = bs.getAllLegalMoves();

		double newAlpha = alpha;
		Move bestMove = null;
		for (TablutMove m : options){
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			double score = MinValue(depth - 1, newAlpha, beta, newBS).x;
			
			if (score > newAlpha) {
				newAlpha = score;
				bestMove = m;
			}
			
			// Pruning
			if (newAlpha >= beta){
				return new Pair(beta, null);
			}
		}
		return new Pair(newAlpha, bestMove);
	}
    
    private static Pair MinValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth)){
			return new Pair(Evaluation(bs), null);
		}
		List<TablutMove> options = bs.getAllLegalMoves();

		double newBeta = beta;
		Move bestMove = null;
		for (TablutMove m : options){
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			
			double score = MaxValue(depth - 1, alpha, newBeta, newBS).x;
			if (score < newBeta){
				newBeta = score;
				bestMove = m;
			}
			
			// Pruning
			if (alpha >= newBeta){
				return new Pair(alpha, null);
			}
		}
		return new Pair(newBeta, bestMove);
	}
	
	private static boolean cutoff(int d) {
		return d <= 0;
	}
	
	
	/**
	 * Evaluation function.
	 * High value good for SWEDES (white).
	 * Low value good for MUSCOVITE (black).
	 * 
	 * @param bs
	 * @return evaluated board state
	 */
	private static double Evaluation(TablutBoardState bs){
		if (bs.gameOver()) {
			if (bs.getWinner() == TablutBoardState.SWEDE){
				return 1000;				
			} else if (bs.getWinner() == Board.DRAW){
				return 0;
			} else if (bs.getWinner() == TablutBoardState.MUSCOVITE) {
				return -1000;				
			} else {
				throw new Error ("unhandled case"); //FIXME
			}
		}
		// Number of pieces evaluation.
		int piecesDifference = bs.getNumberPlayerPieces(TablutBoardState.SWEDE) 
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE);
		return piecesDifference;
		
		/*
		 * Ideas:
		 * 	make it finish quickly, don't goof around ! (remove points from score -> less pruning?
		 * 
		 * 	total number of pawns on board, (eating is good) 
		 * 
		 * 	remove symmetric states
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
