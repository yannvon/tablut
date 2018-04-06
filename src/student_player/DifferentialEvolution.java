package student_player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * 
 * This class implements a Genetic Algorithm called differential evolution.
 * 
 * Disclaimer: This code matches closely the Algorithm and pseudocode that can
 * be found on the Wikipedia page.
 * (https://en.wikipedia.org/wiki/Differential_evolution)
 * 
 * @author Yann Vonlanthen (260808862)
 *
 */
public class DifferentialEvolution {

	public static LinkedList<int[]> population = new LinkedList<>();
	public static Random random = new Random(2048);

	private static int populationSize = 10;
	private static double differentialWeight = 1;
	private static double crossOverProba = 0.5;
	public static final int dimensionalty = 3;
	private static int maxWeight = 50;
	
    private static File logDir = null;
    private static PrintStream logOut = null;

	
	public static volatile int[] LearningPlayer1Weights;
	public static volatile int[] LearningPlayer2Weights;

	/*
	 * I will first attempt to assign integer weights from 0 to 50 to reduce the
	 * problem domain. FIXME run with floating point.
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		// Step 1: Generate a new random population.
		for (int i = 0; i < populationSize; i++) {
			int[] newWeights = new int[dimensionalty];
			for (int j = 0; j < dimensionalty; j++) {
				newWeights[j] = random.nextInt(maxWeight + 1);
			}
			population.add(newWeights);
		}
		
		writePopulationToFile();
		
		// Main Loop
		for (int i = 0; i < 10; i++) {

			// Go through every agent x
			for (int x = 0; x < populationSize; x++) {
				int a, b, c;
				// 1) Pick 3 distinct agents a, b & c
				do {
					a = random.nextInt(populationSize);
				} while (a == x);
				do {
					b = random.nextInt(populationSize);
				} while (b == x || b == a);
				do {
					c = random.nextInt(populationSize);
				} while (c == x || c == a || c == b);

				// 2) Pick random index -> this weight will get changed for sure !
				int R = random.nextInt(dimensionalty);
				
				// 3) compute the agents new weights
				int[] newWeights = population.get(x).clone(); // shallow copy but good enough
				
				int[] indiv1 = population.get(a);
				int[] indiv2 = population.get(b);
				int[] indiv3 = population.get(c);
				
				
				/*
				 *  for each weight change if :
				 *  -uniformly distributed number smaller than cross over probability
				 *  -weight was previously selected to change.
				 */
				for (int d = 0; d < dimensionalty; d++) {
					if (R == d || random.nextDouble() < crossOverProba) {
						newWeights[d] = (int) (indiv1[d] + Math.round(differentialWeight * (indiv2[d] - indiv3[d])));
					}
				}
				
				// 4) check if better than original if so, replace it !
				if (isNewBetter(newWeights, population.get(x), 2)) {
					population.remove(x);
					population.addFirst(newWeights);	//FIXME make sure I dont mess up
				}
			}
		}
	}

	public static boolean isNewBetter(int[] player1, int[] player2, int n_games) {
		LearningPlayer1Weights = player1.clone();
		LearningPlayer2Weights = player2.clone();
		
        try {
            ProcessBuilder server_pb = new ProcessBuilder("java", "-cp", "bin", "boardgame.Server", "-ng", "-k");
            server_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            Process server = server_pb.start();
            
            ProcessBuilder client1_pb = new ProcessBuilder("java", "-cp", "bin", "-Xms520m", "-Xmx520m",
                    "boardgame.Client", "student_player.LearningPlayer1");
            client1_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            ProcessBuilder client2_pb = new ProcessBuilder("java", "-cp", "bin", "-Xms520m", "-Xmx520m",
                    "boardgame.Client", "student_player.LearningPlayer2");
            client2_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            
            
            for (int i = 0; i < n_games; i++) {
                System.out.println("Game " + i);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                Process client1 = ((i % 2 == 0) ? client1_pb.start() : client2_pb.start());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                Process client2 = ((i % 2 == 0) ? client2_pb.start() : client1_pb.start());

                try {
                    client1.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    client2.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Complicated: Read who won game
            

            server.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
	}
	
	public static void writePopulationToFile() throws FileNotFoundException {
		logDir = new File("data");
		File logFile = new File(logDir, "population.txt");
		logOut = new PrintStream(new FileOutputStream(logFile));
		
		for (int i = 0; i < populationSize; i++) {
			logOut.print("indiv" + String.format("%03d", i) + " ");
			int[] weights = population.get(i);
			for (int j = 0; j < dimensionalty; j++) {
				logOut.print(String.format("%02d", weights[j]) + " ");
			}
			logOut.println("");
		}
		logOut.flush();
	}
}
