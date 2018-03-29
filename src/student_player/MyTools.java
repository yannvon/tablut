package student_player;

import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class MyTools {

	/*
	 * MiniMax with Alpha Beta Pruning algorithm.
	 * 
	 */
	
	public static Pair initAlphaBetaPruning(int depth, TablutBoardState bs) {
		// Check non zero depth
		
		return alphaBetaPruning(depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, bs);
	}
	
	
	/**
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @param bs
	 * @return
	 */
	public static Pair alphaBetaPruning(int depth, double alpha, double beta, TablutBoardState bs){
        
		if (depth <= 0 || bs.gameOver()){
			return new Pair(Evaluation(bs), null);
		}
		
		if (bs.getOpponent() == TablutBoardState.MUSCOVITE) {
			return MaxValue(depth, alpha, beta, bs);
		} else {
			return MinValue(depth, alpha, beta, bs);
		}
	}
	
	
    private static Pair MaxValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth)){
			System.out.println("cutoff");
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
				System.out.println("new Alpha score: " + score);
				newAlpha = score;
				bestMove = m;
			}
			
			// Pruning
			if (newAlpha >= beta){
				System.out.println("Pruning occured !");
				return new Pair(beta, null);
			}
			
			// TODO maybe remember best move -> not useful actually?
		}
		return new Pair(newAlpha, bestMove);
	}
    
    private static Pair MinValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth)){
			System.out.println("cutoff");
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
				System.out.println("Pruning occured !");
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
	 * 
	 * @param bs
	 * @return
	 */
	private static double Evaluation(TablutBoardState bs){
		if (bs.gameOver()) {
			if (bs.getWinner() == TablutBoardState.SWEDE){
				return 100;				
			} else if (bs.getWinner() == Board.DRAW){
				return 0; //FIXME
			} else if (bs.getWinner() == TablutBoardState.MUSCOVITE) {
				return -100;				
			} else {
				throw new Error ("unhandled case"); //FIXME
			}
		}
		// Number of pieces evaluation.
		int piecesDifference = bs.getNumberPlayerPieces(TablutBoardState.SWEDE) 
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE);
		return 2 * piecesDifference;
		
		/*
		 * Ideas:
		 * 	make it finish quickly
		 * 
		 * 	total number of pawns on board, (eating is good) 
		 */
	}
	
	public static class Pair {
		public double getX() {
			return x;
		}
		public void setX(double x) {
			this.x = x;
		}
		public Move getM() {
			return m;
		}
		public void setM(Move m) {
			this.m = m;
		}
		public Pair(double x, Move m) {
			this.x = x;
			this.m = m;
		}
		private double x;
		private Move m;
	}
}
