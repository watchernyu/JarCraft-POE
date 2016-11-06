/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

import java.util.Random;

public class Player_Watcher3 extends Player {
	// this player should be able to control one unit and look forward 2 steps
	// using
	// online evolution
	private int _id = 0;
	private int enemy;
	Random ran;

	public Player_Watcher3(int playerID) {
		_id = playerID;
		setID(playerID);
		enemy = GameState.getEnemy(_id);
		ran = new Random();
	}

	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
		boolean foundUnitAction = false;
		int actionMoveIndex = 0;
		List<UnitAction> actions;
		int bestMoveIndex = 0;
		UnitAction move;
		
		/*
		System.out.println(state._unitIndex);
		for(int i=0;i<state._unitIndex.length;i++){
			for(int j=0;j<state._unitIndex[0].length;j++){
				System.out.print(state._unitIndex[i][j]+" ");
			}
			System.out.println();
		}*/
		
		//DNA initialization
		ArrayList<ArrayList<Integer>> DNA = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<30;i++){
			//System.out.println(moves.size());
			DNA.add(new ArrayList<Integer>());
			for(int j=0;j<1;j++){ //currently hardcoded to 1 units and 30 future steps
				DNA.get(i).add(ran.nextInt(4));
			}
		}
		
		double dnaScore=0;
		double dnaBestScore=-9999;
		int[] bestGene = new int[DNA.get(0).size()];
		ArrayList<ArrayList<Integer>> finalDNA = new ArrayList<ArrayList<Integer>>();
		//mutation started
		for(int i=0;i<100;i++){
			this.mutate(DNA);
			//dnaScore = this.dnaEval(state, DNA);
			if(dnaScore>dnaBestScore){
				dnaBestScore = dnaScore;
				for (int k=0;k<DNA.get(0).size();k++){
					bestGene[k] = DNA.get(0).get(k);
				}
			}
		}
		//mutation ended
		//System.out.println("Best DNA score: "+dnaBestScore);
		
		for (Integer u : moves.keySet()) {// currently only testing for one unit....
			actions = moves.get(u);// a list of unit actions of this unit
			//System.out.println(actions);
			moveVec.add(actions.get(bestGene[u]%actions.size()));
		}
	}

	public void mutate(ArrayList<ArrayList<Integer>> DNA) {
		// helper function to mutate a DNA, according to some rate.
		Double rate = 0.6;
		for (int i = 0; i < DNA.size(); i++) {

			for (int j = 0; j < DNA.get(0).size(); j++) {
				Double mut = ran.nextDouble();
				if (mut < rate) {
					int move = ran.nextInt(4);
					DNA.get(i).set(j, move);
				}
			}
		}
	}
/*
	public double dnaEval(GameState currentState, ArrayList<ArrayList<Integer>> DNA) {
		// we can simply call this evaluation function on a DNA!!!!
		// make life easier!!
		GameState sc = currentState.clone(); // sc for state clone
		Game gc = new Game(sc, new Player_NoOverKillAttackValue(this.ID()),
				new Player_NoOverKillAttackValue(this.enemy), 200, false);
		return gc.dnaEval(DNA);
	}
*/
	public String toString() {
		return "Watcher's first state-based alg";
	}
}
