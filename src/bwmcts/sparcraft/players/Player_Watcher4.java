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

public class Player_Watcher4 extends Player {
	// this player should be able to control one unit and look forward 2 steps
	// using
	// online evolution
	private int _id = 0;
	private int enemy;
	Random ran;
	ArrayList<Player> scripts;

	public Player_Watcher4(int playerID) {
		_id = playerID;
		setID(playerID);
		enemy = GameState.getEnemy(_id);
		ran = new Random();
		
		//initialize script players
		scripts = new ArrayList<Player>();
		scripts.add(new Player_AttackClosest(playerID));
		scripts.add(new Player_Kite(playerID));
		scripts.add(new Player_NoOverKillAttackValue(playerID));
		scripts.add(new Player_Defense(playerID));
		//currently only support playerID = 0;
	}

	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
		List<UnitAction> actions;
		int bestMoveIndex = 0;
		UnitAction move;
		
		//DNA initialization
		ArrayList<Integer> DNA = new ArrayList<Integer>();
		for(int i=0;i<5;i++){ //the next 30 steps of a single unit
			DNA.add(ran.nextInt(scripts.size()));
		}
		//System.out.println("DNA: "+DNA);
		
		double dnaScore=0;
		double dnaBestScore=-9999;
		int bestDna = 0;
		//mutation started
		for(int i=0;i<300;i++){//100 mutations
			this.mutate(DNA);
			dnaScore = this.dnaEval(state, DNA);
			if(dnaScore>dnaBestScore){
				dnaBestScore = dnaScore;
				bestDna = DNA.get(0);
			}
		}
		//mutation ended
		//System.out.println("BestScore: "+dnaBestScore+" on "+this.scripts.get(bestDna));
		
		Player scriptToUseA = this.scripts.get(bestDna);
		scriptToUseA.getMoves(state, moves, moveVec);
	}

	public void mutate(ArrayList<Integer> DNA) {
		// helper function to mutate a DNA, according to some rate.
		Double rate = 0.8;
		for(int i=0;i<DNA.size();i++){
			Double mut = ran.nextDouble();
			if (mut < rate) {
				int newGene = ran.nextInt(this.scripts.size());
				DNA.set(i, newGene);
			}
		}
	}
	
	public double dnaEval(GameState currentState,ArrayList<Integer> DNA){
		GameState sc = currentState.clone(); // sc for state clone
		Game gc = new Game(sc, new Player_NoOverKillAttackValue(this.ID()),
				new Player_NoOverKillAttackValue(this.enemy), 200, false, scripts); //send scripts to game...
		//return gc.dnaEval(DNA);
		return 0.0;
	}

	public String toString() {
		return "Watcher's first state-based alg";
	}
}
