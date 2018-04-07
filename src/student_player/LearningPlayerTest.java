package student_player;

import boardgame.Move;
import coordinates.Coordinates;
import student_player.AlphaBetaPruning.Pair;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class LearningPlayerTest extends TablutPlayer {
	private AlphaBetaPruning tools;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public LearningPlayerTest() {
        super("Player1");
    }
    
    public static void main(String[] args) {
		 TablutBoardState bs = new TablutBoardState();
		 
		 double[] weights = {3,5,1,2,1,1};
		 AlphaBetaPruning tools = new AlphaBetaPruning(weights);
		 
		 
		 /*
		  * Test some of my helper functions.
		  */
		 System.out.println("Free rows and columns: " + AlphaBetaPruning.freeRowsAndColumns(bs));
		 System.out.println("Blocked by white: " + AlphaBetaPruning.blockedByWhite(bs));
		 System.out.println("Move Number (4,2):" + AlphaBetaPruning.getMoveNumberForPosition(bs, Coordinates.get(4, 2)));
		 
		 bs.processMove(new TablutMove(Coordinates.get(0, 3), Coordinates.get(2, 3), bs.getTurnPlayer()));
		 bs.processMove(new TablutMove(Coordinates.get(4, 2), Coordinates.get(0, 2), bs.getTurnPlayer()));
		 
		 System.out.println("Free rows and columns: " + AlphaBetaPruning.freeRowsAndColumns(bs));
		 System.out.println("Blocked by white: " + AlphaBetaPruning.blockedByWhite(bs));
		 System.out.println("Move Number (0,2):" + AlphaBetaPruning.getMoveNumberForPosition(bs, Coordinates.get(0, 2)));
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
    		double[] weights = {20,5,1,2,1,1};
    		tools = new AlphaBetaPruning(weights);
    	}
    	
        Pair bestMove = tools.getBestMove(3, 10, boardState);

        System.out.println(bestMove.getValue());
        Move myMove = bestMove.getMove();
        System.out.println("Everything went well ! Move :" + myMove);

        // Return your move to be processed by the server.
        return myMove;
    }
}