package student_player;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.ws.WebEndpoint;

import boardgame.Move;
import student_player.MyTools.Pair;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class LearningPlayer2 extends TablutPlayer {
	private MyLearningTools tools;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public LearningPlayer2() {
        super("LearningPlayer2");
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
			int[] weights = {3,1,1};
			tools = new MyLearningTools(weights);
    	}
    	
        MyLearningTools.Pair bestMove = tools.alphaBetaPruning(3, boardState);
        System.out.println("Best Move " + bestMove.getMove());
        System.out.println("Best Value " + bestMove.getValue());
        System.out.flush();
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
}