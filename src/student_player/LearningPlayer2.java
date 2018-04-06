package student_player;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.ws.WebEndpoint;

import boardgame.Move;
import student_player.MyToolsTimer.Pair;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class LearningPlayer2 extends TablutPlayer {
	public static final int MAX_DEPTH = 3;
	
	private MyToolsClean tools;
	
	
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
    		BufferedReader br;
			try {
				br = new BufferedReader(new FileReader("data/population.txt"));
				br.readLine();
				String firstLine = br.readLine();
				String numbers = firstLine.substring(8);
				System.out.println("numbers " + numbers);
		        System.out.flush();

				int[] weights = new int[DifferentialEvolution.DIMENSIONALITY];
				
				for(int i = 0; i < DifferentialEvolution.DIMENSIONALITY; i++){
					weights[i] = Integer.valueOf(numbers.substring(1 + 3*i, 3 + 3*i));
					System.out.println("Weight"+ i +": " + weights[i]);
				}
				
				tools = new MyToolsClean(weights);
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
    	}
    	
        MyToolsClean.Pair bestMove = tools.alphaBetaPruning(MAX_DEPTH, boardState);
        System.out.println("Player2: Best Value " + bestMove.getValue());
        System.out.flush();
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
}