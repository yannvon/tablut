package student_player;

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
	private static int dimensionalty = 3;
	private static int maxWeight = 50;

	/*
	 * I will first attempt to assign integer weights from 0 to 50 to reduce the
	 * problem domain. FIXME run with floating point.
	 */
	public static void main(String[] args) {

		// Step 1: Generate a new random population.
		for (int i = 0; i < populationSize; i++) {
			int[] newWeights = new int[dimensionalty];
			for (int j = 0; j < dimensionalty; j++) {
				newWeights[j] = random.nextInt(maxWeight + 1);
			}
			population.add(newWeights);
		}

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
				if (isNewBetter(newWeights, population.get(x))) {
					population.remove(x);
					population.addFirst(newWeights);	//FIXME make sure I dont mess up
				}
			}
		}
	}

	public static boolean isNewBetter(int[] player1, int[] player2) {
		
		return true;
	}
}
