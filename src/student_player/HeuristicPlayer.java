package student_player;

import boardgame.Move;
import student_player.MyTools.Pair;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class HeuristicPlayer extends TablutPlayer {
	private MyTools tools;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public HeuristicPlayer() {
        super("HeuristicPlayer: 1, 0.3");
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
    		double[] weights = {1, 0.3, 0.4};
    		tools = new MyTools(weights);
    	}
    	
        Pair bestMove = tools.alphaBetaPruning(3, boardState);

        // Is random the best you can do?
        System.out.println(bestMove.getValue());
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
}