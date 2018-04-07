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
 * For this version I use floating point weights, and even allow negative values.
 * Though I hope that these individuals will not survive.
 * 
 * Disclaimer: This code matches closely the Algorithm and pseudocode that can
 * be found on the Wikipedia page.
 * (https://en.wikipedia.org/wiki/Differential_evolution)
 * 
 * @author Yann Vonlanthen (260808862)
 *
 */
public class DifferentialEvolution6D {

	public static final int WEIGHT_SIZE = 10;
	public static final int N_GAMES = 2;
	public static final int MAX_GENERATIONS = 1000;
	public static final double MAX_WEIGHT = Double.POSITIVE_INFINITY;
	public static final double MIN_WEIGHT = Double.NEGATIVE_INFINITY;
	public static final int POP_SIZE = 10;
	public static final double DIFF_WEIGHT = 1;
	public static final double CROSS_OVER_PROBA = 0.5;
	public static final int DIMENSIONALITY = 6;
	
	public static LinkedList<double[]> population = new LinkedList<>();
	public static Random random = new Random(2048);
	
	// Variables to keep track of evolution
	public static int generationN = 0;
	public static int indivUpdated = 0;
	public static int totalProposalWin = 0;
	public static int totalOldWin = 0;
	public static int totalGamesPlayed = 0;
	public static int indivUpdatedBecauseMoves = 0;
	
	public static PrintStream statsOut;

	public static void main(String[] args) throws IOException {
		
		// Step 0: Keep file with some statistics
		File dataFile = new File("data/stats.txt");
		statsOut = new PrintStream(new FileOutputStream(dataFile));
		
		writeStatsToFile();
		
		// Step 1: Generate a new random population.
		for (int i = 0; i < POP_SIZE; i++) {
			double[] newWeights = new double[DIMENSIONALITY];
			for (int j = 0; j < DIMENSIONALITY; j++) {
				newWeights[j] = random.nextDouble();
			}
			population.add(normalize(newWeights));
		}
		
		writePopulationToFile();
		
		// Main Loop of Generations
		for (int i = 0; i < MAX_GENERATIONS; i++) {
			generationN++;
			
			// Go through every agent x
			for (int x = 0; x < POP_SIZE; x++) {
				int a, b, c;
				// 1) Pick 3 distinct agents a, b & c
				do {
					a = random.nextInt(POP_SIZE);
				} while (a == x);
				do {
					b = random.nextInt(POP_SIZE);
				} while (b == x || b == a);
				do {
					c = random.nextInt(POP_SIZE);
				} while (c == x || c == a || c == b);

				// 2) Pick random index -> this weight will get changed for sure !
				int R = random.nextInt(DIMENSIONALITY);
				
				// 3) compute the agents new weights
				double[] newWeights = population.get(x).clone(); // shallow copy but good enough
				
				double[] indiv1 = population.get(a);
				double[] indiv2 = population.get(b);
				double[] indiv3 = population.get(c);
				
				
				/*
				 *  for each weight change if :
				 *  -uniformly distributed number smaller than cross over probability
				 *  -weight was previously selected to change.
				 */
				for (int d = 0; d < DIMENSIONALITY; d++) {
					if (R == d || random.nextDouble() < CROSS_OVER_PROBA) {
						double newW = indiv1[d] + DIFF_WEIGHT * (indiv2[d] - indiv3[d]);
						if (newW > MAX_WEIGHT) {
							newW = MAX_WEIGHT;
						} else if (newW < MIN_WEIGHT) {
							newW = MIN_WEIGHT;
						}
						newWeights[d] = newW;
					}
				}
				newWeights = normalize(newWeights);
				
				// 4) check if better than original if so, replace it !
				// Put both proposal and x at the top of file.
				
				// Decide how many games to play: set to 0 to keep new value
				setPlayingWeights(x, newWeights);
				
				
				if (isNewBetter(N_GAMES)) { 
					// Change population in memory
					population.remove(x);
					population.add(x, newWeights);
					indivUpdated++;
				
					// Write new population to file
					writePopulationToFile();
				
				} else {
					// Don't change population
					
					// Write Population to file again
					writePopulationToFile();
				}
				writeStatsToFile();
			}
		}
	}

	public static boolean isNewBetter(int n_games) throws IOException {
		/*
		 *  Disclaimer: Code copied from Autoplay.java
		 */
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
         * If all goes well no concurrency issue here.
         */
        int[] stats = readLogs();
        
        // Keep track of some stats
        totalOldWin += stats[0];
        totalProposalWin += stats[1];
        totalGamesPlayed += N_GAMES;
        
        if (stats[1] == stats[0] && (stats[4] > stats[3])) {
        	indivUpdatedBecauseMoves++;
        	System.out.println("Updated because more Moves!");
        }
        
        // Debug output
        System.out.println("Old weights WINS (P1): " + stats[0]);
        System.out.println("New weights WINS (P2): " + stats[1]);
        System.out.println("Draws: " + stats[2]);
        System.out.println("Moves Played When Lost: " + stats[3] + " vs " + stats[4]);
        
        boolean newerIsBetter = stats[1] > stats[0] || (stats[1] == stats[0] && (stats[4] > stats[3]));
        
        return newerIsBetter;	// Never is only better if more wins or survived longer!
	}
	
	
	public static int[] readLogs() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("logs/outcomes.txt"));
		LinkedList<String> allLines = new LinkedList<>();
		
		while (br.ready()) {
			allLines.add(br.readLine());
		}
		
		// Reverse order
		Collections.reverse(allLines);
		
		int[] stats = {0,0,0,0,0}; // WINS P1, WINS P2, DRAWS, P1 MOVES WHEN LOST, P2 MOVES WHEN LOST
		for (int i = 0; i < N_GAMES; i++){
			String s = allLines.removeFirst();
			
			boolean player1Win = s.lastIndexOf("LearningPlayer1") > s.lastIndexOf("LearningPlayer2");
			int movesPlayed = Integer.valueOf(s.split(",")[5]);
			
			if (s.contains("DRAW")) {
				stats[2]++;
			} else if (player1Win) {
				stats[0]++;
				stats[4] += movesPlayed; 
			} else {
				stats[1]++;
				stats[3] += movesPlayed;
			}
		}
		br.close();
		return stats;
		
	}
	
	public static void writePopulationToFile() throws FileNotFoundException {
		File logDir = new File("data");
		File logFile = new File(logDir, "population" + generationN + ".txt");
		PrintStream dataOut = new PrintStream(new FileOutputStream(logFile));
		
		for (int i = 0; i < POP_SIZE; i++) {
			dataOut.print("indiv" + String.format("%03d", i) + " ");
			double[] weights = population.get(i);
			for (int j = 0; j < DIMENSIONALITY; j++) {
				dataOut.print(String.format("%010f", weights[j]) + " ");
			}
			dataOut.println("");
		}
		dataOut.flush();
		dataOut.close();
	}
	
	public static void setPlayingWeights(int x, double[] weights) throws FileNotFoundException {
		File dataFile = new File("data/playing.txt");
		PrintStream dataOut = new PrintStream(new FileOutputStream(dataFile));
		
		// Put old first -> Player 1
		dataOut.print("Player01 ");
		double[] w = population.get(x);
		for (int j = 0; j < DIMENSIONALITY; j++) {
			dataOut.print(String.format("%010f", w[j]) + " ");
		}
		dataOut.println("");
	
		// Put new weights -> Player 2
		dataOut.print("Player02 ");
		for (int j = 0; j < DIMENSIONALITY; j++) {
			dataOut.print(String.format("%010f", weights[j]) + " ");
		}
		
		dataOut.flush();
		dataOut.close();
	}
	
	public static void writeStatsToFile() throws FileNotFoundException {
		statsOut.println("Current Generation: " + generationN + " Total games: " + totalGamesPlayed);
		statsOut.print("Player01 (OLD) wins: " + totalOldWin);
		statsOut.println(" Player02 (NEW) wins: " + totalProposalWin);
		statsOut.println("Individuals updated: " + indivUpdated);
		statsOut.println("Individuals updated because of more Steps only: " + indivUpdatedBecauseMoves);
		statsOut.flush();
	}
	
	public static double[] normalize(double[] weights) {
		double sum = 0;
		for (double i : weights) {
			sum += Math.abs(i); // FIXME Should values lay on a sphere or cube?
			// i.e. should take square root of squared or abs?
		}
		double[] newWeights = weights.clone();
		for (int i = 0; i < weights.length; i++) {
			newWeights[i] = weights[i] / sum;
		}
		return newWeights;
	}

}
