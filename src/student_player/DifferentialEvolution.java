package student_player;

import java.util.ArrayList;
import java.util.Random;

/**
 * 
 * This class implements a Genetic Algorithm called differential evolution.
 * 
 * Disclaimer: 	This code matches closely the Algorithm and pseudocode 
 * 				that can be found on the Wikipedia page.
 * 				(https://en.wikipedia.org/wiki/Differential_evolution)
 *  
 * @author Yann Vonlanthen (260808862)
 *
 */
public class DifferentialEvolution {
	
	public static ArrayList<int[]> population = new ArrayList<>();
	public static Random random = new Random();
	
	private static int populationSize = 10;
	private static double differentialWeight = 1;
	private static double crossOverProba = 0.5;
	private static int dimensionalty = 3;
	private static int maxWeight = 50;
	
	/*
	 * I will first attempt to assign integer weights from 0 to 50 
	 * to reduce the problem domain.
	 * FIXME run with floating point.
	 */
	public static void main(String[] args) {
		
		// Step 1: Generate a new random population.
		for (int i = 0; i < populationSize; i++){
			int[] newWeights = new int[dimensionalty];
			for (int j = 0; j < dimensionalty; j++) {
				newWeights[j] = random.nextInt(maxWeight + 1);
			}
			population.add(newWeights);
		}
		
		
		// Main Loop
		while (true) {
			
			// Go through every agent
			
			
			
			
		}
		
		
		

	}
	
	
	public static boolean fitnessFunction(int player1, int player2){
		
		return true;
	}

}
