package student_player;

import java.util.List;
import boardgame.Board;
import boardgame.Move;
import coordinates.Coord;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class MyTools {
	public double[] weights;
	
	public MyTools(double[] weights) {
		this.weights = weights;
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
	public Pair alphaBetaPruning(int depth, TablutBoardState bs){
        
		if (depth <= 0 || bs.gameOver()){
			return new Pair(Evaluation(bs), null);
		}
		double alpha = Double.NEGATIVE_INFINITY;
		double beta = Double.POSITIVE_INFINITY;
		
		if (bs.getTurnPlayer() == TablutBoardState.SWEDE) {
			return MaxValue(depth, alpha, beta, bs);	//FIXME create different methods here that keep track of the move !!
		} else {
			return MinValue(depth, alpha, beta, bs);
		}
	}
	
	
    private Pair MaxValue(int depth, double alpha, double beta, TablutBoardState bs) {
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
    
    private Pair MinValue(int depth, double alpha, double beta, TablutBoardState bs) {
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
	
	private boolean cutoff(int d) {
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
	private double Evaluation(TablutBoardState bs){
		double value = 0;
		
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
		
		// HEURISTIC 1: Number of pieces difference.
		int piecesDifference = bs.getNumberPlayerPieces(TablutBoardState.SWEDE) 
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE);
		value += weights[0] * piecesDifference;

		
		// HEURISTIC 2: King Mobility.
		if (weights[1] != 0){
			int mobilityKing = bs.getLegalMovesForPosition(bs.getKingPosition()).size();
			value += weights[1]* mobilityKing;
		}
		
		// HEURISTIC 3: General Mobility
		int count = 0;
		int mult = (bs.getTurnPlayer() == TablutBoardState.SWEDE) ? 1 : -1;
		for (Coord c : bs.getPlayerPieceCoordinates()) {
			count += mult * bs.getLegalMovesForPosition(c).size();
		}
		for (Coord c : bs.getOpponentPieceCoordinates()) {
			count -= mult * bs.getLegalMovesForPosition(c).size();
		}
		value += weights[2] * count;
		
		return value;
		
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
