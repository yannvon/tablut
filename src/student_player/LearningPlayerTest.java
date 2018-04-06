package student_player;

import boardgame.Move;
import student_player.MyToolsTimer.Pair;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class LearningPlayerTest extends TablutPlayer {
	private MyToolsTimer tools;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public LearningPlayerTest() {
        super("Player1");
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
    		int[] weights = {3, 5, 0};
    		tools = new MyToolsTimer(weights);
    	}
    	
    	//FIXME put depth as weight?
        Pair bestMove = tools.alphaBetaPruning(4, 10, boardState);

        System.out.println(bestMove.getValue());
        Move myMove = bestMove.getMove();
        System.out.println("Everything went well ! Move :" + myMove);

        // Return your move to be processed by the server.
        return myMove;
    }
}