package student_player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.plaf.synth.SynthSeparatorUI;

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
	/*
	 * PROBLEMS:
	 * 
	 *  1) set n_games to 0, it will converge to an average of the values..
	 * 
	 */
	public static int COUNT = 0;
	public static final int N_GAMES = 10;
	public static final int MAX_WEIGHT = 50; // TODO Test different values
	public static final int MIN_WEIGHT = 0;
	public static LinkedList<int[]> population = new LinkedList<>();
	public static Random random = new Random(2048);

	private static int populationSize = 10;
	private static double differentialWeight = 0.2;
	private static double crossOverProba = 0.5;
	public static final int DIMENSIONALITY = 3;
	
	public static volatile int[] LearningPlayer1Weights;
	public static volatile int[] LearningPlayer2Weights;

	/*
	 * I will first attempt to assign integer weights from 0 to 50 to reduce the
	 * problem domain. FIXME run with floating point.
	 */
	public static void main(String[] args) throws IOException {
		
		// Step 1: Generate a new random population.
		for (int i = 0; i < populationSize; i++) {
			int[] newWeights = new int[DIMENSIONALITY];
			for (int j = 0; j < DIMENSIONALITY; j++) {
				newWeights[j] = random.nextInt(MAX_WEIGHT + 1);
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
				int R = random.nextInt(DIMENSIONALITY);
				
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
				for (int d = 0; d < DIMENSIONALITY; d++) {
					if (R == d || random.nextDouble() < crossOverProba) {
						int newW = (int) (indiv1[d] + Math.round(differentialWeight * (indiv2[d] - indiv3[d])));
						if (newW > MAX_WEIGHT) {
							newW = MAX_WEIGHT;
						} else if (newW < MIN_WEIGHT) {
							newW = MIN_WEIGHT;
						}
						newWeights[d] = newW;
					}
				}
				
				// 4) check if better than original if so, replace it !
				// Put both proposal and x at the top of file.
				
				// Decide how many games to play: set to 0 to keep new value
				setPlayingWeights(x, newWeights);
				
				
				if (isNewBetter(N_GAMES)) { 
					// Change population in memory
					population.remove(x);
					population.add(x, newWeights);
				
					// Write new population to file
					writePopulationToFile();
				
				} else {
					// Don't change population
					
					// Write Population to file again
					writePopulationToFile();
				}
			}
		}
	}

	public static boolean isNewBetter(int n_games) throws IOException {
		
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
            
            server.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
         * Read last N_GAMES logs to see who won.
         */
        int[] stats = readLogs();
        System.out.println("Old weights WINS (P1): " + stats[0]);
        System.out.println("New weights WINS (P2): " + stats[1]);
        System.out.println("Draws: " + stats[2]);
        return stats[1] > stats[0];
	}
	
	
	public static int[] readLogs() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("logs/outcomes.txt"));
		LinkedList<String> allLines = new LinkedList<>();
		
		while (br.ready()) {
			allLines.add(br.readLine());
		}
		
		// Reverse order
		Collections.reverse(allLines);
		
		int[] stats = {0,0,0}; // WINS P1, WINS P2, DRAWS
		for (int i = 0; i < N_GAMES; i++){
			String s = allLines.removeFirst();
			
			boolean player1Win = s.lastIndexOf("LearningPlayer1") > s.lastIndexOf("LearningPlayer2");

			if (s.contains("DRAW")) {
				stats[2]++;
			} else if (player1Win) {
				stats[0]++;
			} else {
				stats[1]++;
			}
		}
		br.close();
		return stats;
		
	}
	
	public static void writePopulationToFile() throws FileNotFoundException {
		File logDir = new File("data");
		File logFile = new File(logDir, "population" + COUNT + ".txt");
		PrintStream dataOut = new PrintStream(new FileOutputStream(logFile));
		
		for (int i = 0; i < populationSize; i++) {
			dataOut.print("indiv" + String.format("%03d", i) + " ");
			int[] weights = population.get(i);
			for (int j = 0; j < DIMENSIONALITY; j++) {
				dataOut.print(String.format("%02d", weights[j]) + " ");
			}
			dataOut.println("");
		}
		dataOut.flush();
		dataOut.close();
		COUNT++;
	}
	
	public static void setPlayingWeights(int x, int[] weights) throws FileNotFoundException {
		File dataFile = new File("data/playing.txt");
		PrintStream dataOut = new PrintStream(new FileOutputStream(dataFile));
		
		// Put old first -> Player 1
		dataOut.print("Player01 ");
		int[] w = population.get(x);
		for (int j = 0; j < DIMENSIONALITY; j++) {
			dataOut.print(String.format("%02d", w[j]) + " ");
		}
		dataOut.println("");
	
		// Put new weights -> Player 2
		dataOut.print("Player02 ");
		for (int j = 0; j < DIMENSIONALITY; j++) {
			dataOut.print(String.format("%02d", weights[j]) + " ");
		}
		
		dataOut.flush();
		dataOut.close();
	}
}
