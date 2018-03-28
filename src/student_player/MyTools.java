package student_player;

import java.util.List;
import java.util.Random;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class MyTools {
	static private Random rand = new Random(2047); 
	/*
	 * MiniMax with Alpha Beta Pruning algorithm.
	 * 
	 */
	
	public Move initAlphaBetaPruning(int depth, TablutBoardState bs) {
		// Check non zero depth
		
		int opp = bs.getOpponent();
		return alphaBetaPruning(depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, bs, opp);
		
	}
	
	
	/**
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @param bs
	 * @return
	 */
	public double alphaBetaPruning(int depth, double alpha, double beta, TablutBoardState bs, int opp){
        
		if (depth <= 0 || bs.gameOver()){
			return 0; //FIXME
		}
		
		if (bs.getOpponent() == opp) {
			return MaxValue(depth, alpha, beta, bs);
		} else {
			return MinValue(depth, alpha, beta, bs);
		}
	}
	
	
    private double MaxValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth)){
			return Evaluation(bs);
		}

		List<TablutMove> options = bs.getAllLegalMoves();

		double a = alpha;
		for (TablutMove m : options){
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			a = Math.max(a, MinValue(depth-1, alpha, beta, newBS));
			//pruning
			if (a >= beta){
				return beta;
			}
			return a;
		}
	}
    
    private double MinValue(int depth, double alpha, double beta, TablutBoardState bs) {
		if (cutoff(depth)){
			return Evaluation(bs);
		}

		List<TablutMove> options = bs.getAllLegalMoves();

		double b = beta;
		for (TablutMove m : options){
			TablutBoardState newBS = (TablutBoardState) bs.clone();
			newBS.processMove(m);
			b = Math.max(b, MaxValue(depth -1, alpha, beta, newBS));
			//pruning
			if (alpha >= b){
				return alpha;
			}
			return b;
		}
	}
	
	private boolean cutoff(int s) {
		return s <= 0;
	}
	
	private double Evaluation(TablutBoardState bs){
		// Number of pieces evaluation.
		return bs.getNumberPlayerPieces(TablutBoardState.SWEDE) 
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE);
	}
}
