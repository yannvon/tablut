package student_player;

import boardgame.Move;
import student_player.AlphaBetaPruning.Pair;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
	private AlphaBetaPruning tools;
	
	public static final int MIN_DEPTH = 3;
	public static final int MAX_DEPTH = 5;
	
	// Weights learned after a couple hours of evolution
	private static final double N_PIECES_W = 0.323785;
	private static final double KING_MOBILITY_W = 0.059866;
	private static final double BLACK_MOBILITY_W = 0.049957;
	private static final double KING_DIST_CORNER_W = 0.411849;
	private static final double N_FREE_ROW_COL = 0.020662;
	private static final double N_BLOCKED_CORNERS = -0.133881;
	

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
    	
    	if(tools == null){
    		double[] weights = {N_PIECES_W, KING_MOBILITY_W, BLACK_MOBILITY_W,
    							KING_DIST_CORNER_W, N_FREE_ROW_COL, N_BLOCKED_CORNERS}; // trained weights
    		tools = new AlphaBetaPruning(weights);
    	}
    	
        Pair bestMove = tools.getBestMove(MIN_DEPTH, MAX_DEPTH, boardState);
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
}