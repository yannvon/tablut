package student_player;

import java.io.BufferedReader;
import java.io.FileReader;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class LearningPlayer1 extends TablutPlayer {
	private MyLearningTools tools;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public LearningPlayer1() {
        super("LearningPlayer1");
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
    		BufferedReader brTest;
			try {
				brTest = new BufferedReader(new FileReader("data/population.txt"));
				String firstLine = brTest.readLine();
				String numbers = firstLine.substring(8);
				System.out.println("numbers " + numbers);
		        System.out.flush();

				int[] weights = new int[DifferentialEvolution.dimensionalty];
				
				for(int i = 0; i < DifferentialEvolution.dimensionalty; i++){
					weights[i] = Integer.valueOf(numbers.substring(1 + 3*i, 3 + 3*i));
					System.out.println("Weight" + weights[i]);
				}
				
				tools = new MyLearningTools(weights);
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
    	}
    	
    	System.out.println("Start alpha beta");
        MyLearningTools.Pair bestMove = tools.alphaBetaPruning(3, boardState);
        System.out.println("Best Move " + bestMove.getMove());
        System.out.println("Best Value " + bestMove.getValue());
        System.out.flush();
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
}