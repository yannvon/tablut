package student_player;

import java.io.BufferedReader;
import java.io.FileReader;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class LearningPlayer1 extends TablutPlayer {
	public static final int MAX_DEPTH = 3;
	
	private AlphaBetaPruning tools;
	
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
    		BufferedReader br;
			try {
				br = new BufferedReader(new FileReader("data/playing.txt"));
				String firstLine = br.readLine();
				String numbers = firstLine.substring(8);
				System.out.println("weights " + numbers);
		        System.out.flush();

				double[] weights = new double[DifferentialEvolution3D.DIMENSIONALITY];
				
				for(int i = 0; i < DifferentialEvolution3D.DIMENSIONALITY; i++){
					weights[i] = Double.valueOf(numbers.substring(1 + (DifferentialEvolution3D.WEIGHT_SIZE + 1)*i, 1 + (DifferentialEvolution3D.WEIGHT_SIZE + 1)*(i+1)));
				}
				
				tools = new AlphaBetaPruning(weights);
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
    	}
    	
        AlphaBetaPruning.Pair bestMove = tools.getBestMove(MAX_DEPTH, MAX_DEPTH, boardState);
        Move myMove = bestMove.getMove();

        // Return your move to be processed by the server.
        return myMove;
    }
}