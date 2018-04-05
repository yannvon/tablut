package student_player;

import java.util.List;

import boardgame.Board;
import boardgame.Move;
import student_player.MyTools.Pair;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
	private MyTools tools;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260808862");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
    	
        Pair bestMove = alphaBetaPruning(4, boardState);

        // Is random the best you can do?
        System.out.println(bestMove.getValue());
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
    
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
		if (cutoff(depth, bs)){
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
		if (cutoff(depth, bs)){
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
	
	private boolean cutoff(int d, TablutBoardState bs) {
		return d <= 0 || bs.gameOver();
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
				- bs.getNumberPlayerPieces(TablutBoardState.MUSCOVITE) + 7;
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