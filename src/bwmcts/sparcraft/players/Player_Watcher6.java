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
import genetic.Population;

import java.util.Random;

public class Player_Watcher6 extends Player {
	boolean showBestDna = false;
	boolean allowInitRandomMutation = false;
	
	private int _id = 0;
	private int enemy;
	Random ran;
	ArrayList<Player> scripts;
	int numOfUnits=0;
	int EVALUTIONMETHOD = 1;//0 means LTD2, 1 means playout
	int futureSteps = 4;
	int numOfMutations = 7;

	public Player_Watcher6(int playerID) {
		_id = playerID;
		setID(playerID);
		enemy = GameState.getEnemy(_id);
		ran = new Random();
		
		//initialize script players
		scripts = new ArrayList<Player>();
		
		
		scripts.add(new Player_NoOverKillAttackValue(playerID));
		scripts.add(new Player_NOKAVForward(playerID));
		scripts.add(new Player_NOKAVBack(playerID));
		scripts.add(new Player_NOKAVForwardFar(playerID));
		scripts.add(new Player_NOKAVBackClose(playerID));
		scripts.add(new Player_NOKAVBackFar(playerID));
		
		//scripts.add(new Player_KiteDPS(playerID));
		//scripts.add(new Player_AttackAndMove(playerID));
		//scripts.add(new Player_AttackWeakest(playerID));
		
		//scripts.add(new Player_AttackClosest(playerID));
		
		///////ADD A SCRITP TO WALK FORWARD TO THE ENEMY WEAKEST UNIT....
		
		//scripts.add(new Player_Retreat(playerID));
		//scripts.add(new Player_RetreatFar(playerID));
		//scripts.add(new Player_Forward(playerID));
		//scripts.add(new Player_ForwardFar(playerID));
		//scripts.add(new Player_Defense(playerID));
		//currently only support playerID = 0;
	}

	public void setToTestOnly(){
		futureSteps = 1;
		numOfMutations = 1;
	}
	
	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
		List<UnitAction> actions;
		int bestMoveIndex = 0;
		UnitAction move;

		int numOfScripts = scripts.size();
		//DNA initialization
		//fix here: we need to add the sense of population.
		
		Population P = new Population(this,state,futureSteps,numOfScripts,numOfUnits,scripts);
		P.initialize();
		long startTime = System.currentTimeMillis();
		long timeLimit = 40;
		for(int e=0;e<numOfMutations;e++){
			if(System.currentTimeMillis()-startTime>timeLimit){
				break;
			}
			P.evolve(1);
		}
		//System.out.println("Time used: "+ (System.currentTimeMillis()-startTime));
		
		ArrayList<Integer> bestDnaPiece = P.bestDna().get(0);
		if(showBestDna){
			System.out.println("BestDNA: "+bestDnaPiece);
		}
		dnaMoves(state,bestDnaPiece,moves,moveVec);
	}

	public void dnaMoves(GameState state, ArrayList<Integer> DNAi,
			HashMap<Integer,List<UnitAction>> moves, List<UnitAction> moveVec){
		for (Integer u : moves.keySet()){
			//this u is a unit index!!
			int scriptN = DNAi.get(u);
			Player scriptToUse = this.scripts.get(scriptN); //
			HashMap<Integer,List<UnitAction>> oneUnitMap = new HashMap<Integer,List<UnitAction>>();
			oneUnitMap.put(u, moves.get(u));
			scriptToUse.getMoves(state, oneUnitMap, moveVec);
		}
	}
	
	/*
	public void mutateZeroOneGroup(ArrayList<ArrayList<Integer>> DNA) {
		// helper function to mutate a DNA, according to some rate.
		Double rate = 0.3;
		for(int i=0;i<DNA.size();i++){
			for(int j=0;j<DNA.get(0).size();j++){
				Double mut = ran.nextDouble();
				ArrayList<Integer> gene = DNA.get(i);
				if (mut < rate) {
					int newGene = ran.nextInt(2); //only give 0 or 1 so only NOKAV or retreat
					gene.set(j, newGene);
				}
			}
		}
	}*/

	public void setNumUnit(int n){
		this.numOfUnits = n;
	}
	
	public String toString() {
		return "Portfolio Online Evolution with DNA population control";
	}
}
