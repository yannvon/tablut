/**
 * 
 */
package student_player;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/**
 * Describe class here.
 * 
 * @author Yann Vonlanthen (260808862)
 *
 */
public class EvalFunc1Player extends TablutPlayer {

	@Override
	public Move chooseMove(TablutBoardState boardState) {
		
		/*
		 * Alpha-Beta pruning implementation.
		 * 
		 * Disclaimer: Done with the help of slides Lecture 7.
		 */
		double MaxValue(s,α,β) {
			if cutoff(s), return Evaluation(s).
					for each state s’ in Successors(s)
					let α = max { α, MinValue(s’,α,β) }.
					if α ≥ β, return β.
							return α.			
		}
		double MinValue(s,α,β) {
			if cutoff(s){
				return Evaluation(s).
			}
			for each state s’ in Successors(s)
				let β = min { β, MaxValue(s’,α,β) }.
				if α ≥ β 
					return α.
				return β.			
		}
		
		return null;
	}

}
