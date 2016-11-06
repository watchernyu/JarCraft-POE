/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_PGS extends Player {
	// PORTFOLIO GREEDY SEARCH
	// THIS IS NOW SPECIFICALLY THE OLD CHURCHILL'S VERSION.
	public String NAME = "PGS-2";
	private int _id = 0;
	private int _eid = 1;
	ArrayList<Player> portfolio0;
	ArrayList<Player> portfolio1;
	ArrayList<Integer> ourScripts;
	ArrayList<Integer> enemyScripts;
	int Iteration = 1;
	int R = 0;
	int numOfUnits = 0;
	long timeLimit = 20000000; // in nano sec 1 millie =1 000 000
	long timeElapsed = 0;
	long startTime = 0;
	boolean LIMITTIME = true;
	int ROUNDLIMIT = 200;
	//boolean REACHLIMIT = false;
	Player pg_helper0;
	Player pg_helper1;
	Player_pg_helper _pg_helper0;
	Player_pg_helper _pg_helper1;

	public void setTimeLimit(int tl){
		timeLimit = tl * 1000000;
	}
	
	public void setTimeLimit20(){
		timeLimit = 20000000;
	}
	public void setTimeLimit40(){
		timeLimit = 40000000;
	}
	
	public Player_PGS(int playerID) {
		_id = playerID;
		_eid = (playerID + 1) % 2;
		setID(playerID);
		portfolio0 = new ArrayList<Player>();
		portfolio1 = new ArrayList<Player>();
		ourScripts = new ArrayList<Integer>();// need to use numbers to
												// represent script.
		enemyScripts = new ArrayList<Integer>();

		///////////////////////////////////////////
		// SINCE EACH PLAYER HAS A ID IN IT..... WE NEED TO HAVE TWO
		/////////////////////////////////////////// PORTFOLIO....
		portfolio0.add(new Player_NoOverKillAttackValue(0));
		portfolio0.add(new Player_KiteDPS(0));

		portfolio1.add(new Player_NoOverKillAttackValue(1));
		portfolio1.add(new Player_KiteDPS(1));
		// portfolio.add(new Player_NOKAVForward(playerID));
		// portfolio.add(new Player_NOKAVBack(playerID));
		// portfolio.add(new Player_NOKAVForwardFar(playerID));
		// portfolio.add(new Player_NOKAVBackClose(playerID));
		// portfolio.add(new Player_NOKAVBackFar(playerID));
		
		pg_helper0 = new Player_pg_helper(0,portfolio0,portfolio1);
		pg_helper1 = new Player_pg_helper(1,portfolio0,portfolio1);
		_pg_helper0 = (Player_pg_helper) pg_helper0;
		_pg_helper1 = (Player_pg_helper) pg_helper1;
		
		NAME = "PGS-2";
	}
	
	public void init2scripts(){
		portfolio0 = new ArrayList<Player>();
		portfolio1 = new ArrayList<Player>();
		
		portfolio0.add(new Player_NoOverKillAttackValue(0));
		portfolio0.add(new Player_KiteDPS(0));

		portfolio1.add(new Player_NoOverKillAttackValue(1));
		portfolio1.add(new Player_KiteDPS(1));

		pg_helper0 = new Player_pg_helper(0,portfolio0,portfolio1);
		pg_helper1 = new Player_pg_helper(1,portfolio0,portfolio1);
		_pg_helper0 = (Player_pg_helper) pg_helper0;
		_pg_helper1 = (Player_pg_helper) pg_helper1;
		
		NAME = "PGS-2";
	}
	
	public void init6scripts(){
		portfolio0 = new ArrayList<Player>();
		portfolio1 = new ArrayList<Player>();

		portfolio0.add(new Player_NoOverKillAttackValue(0));
		portfolio0.add(new Player_NOKAVForward(0));
		portfolio0.add(new Player_NOKAVBack(0));
		portfolio0.add(new Player_NOKAVForwardFar(0));
		portfolio0.add(new Player_NOKAVBackClose(0));
		portfolio0.add(new Player_NOKAVBackFar(0));

		portfolio1.add(new Player_NoOverKillAttackValue(1));
		portfolio1.add(new Player_NOKAVForward(1));
		portfolio1.add(new Player_NOKAVBack(1));
		portfolio1.add(new Player_NOKAVForwardFar(1));
		portfolio1.add(new Player_NOKAVBackClose(1));
		portfolio1.add(new Player_NOKAVBackFar(1));

		pg_helper0 = new Player_pg_helper(0,portfolio0,portfolio1);
		pg_helper1 = new Player_pg_helper(1,portfolio0,portfolio1);
		_pg_helper0 = (Player_pg_helper) pg_helper0;
		_pg_helper1 = (Player_pg_helper) pg_helper1;
		
		NAME = "PGS-6";
	}
	
	/*
	public void initEnhancedzzzzzz() {
		NAME = "PGS-enhanced";
		Iteration = 2;
		R = 2;
		// called after setting the num of units
		if (numOfUnits >= 64) {
			ROUNDLIMIT = 10;
		} else if (numOfUnits >= 48) {
			ROUNDLIMIT = 15;
		} else if (numOfUnits >= 32) {
			ROUNDLIMIT = 35;
		} else if (numOfUnits >= 24) {
			ROUNDLIMIT = 60;
		} else if (numOfUnits >= 16) {
			ROUNDLIMIT = 100;
		} else {
			ROUNDLIMIT = 200;
		}
	}*/

	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		startTime = System.nanoTime();
		moveVec.clear();

		fill(enemyScripts, 0);
		ourScripts = getSeedScripts(state, _id, enemyScripts);
		enemyScripts = getSeedScripts(state, (_id + 1) % 2, ourScripts);
		// System.out.println(ourScripts);
		improve(state, _id, ourScripts, enemyScripts);
		// System.out.println(ourScripts);
		for (int r = 0; r < R; r++) {
			if (LIMITTIME && (System.nanoTime() - startTime > timeLimit)) {
				break; // this kind of structure is safer.
			}
			improve(state, _id, ourScripts, enemyScripts);
			improve(state, (_id + 1) % 2, enemyScripts, ourScripts);
		}

		makeMove(ourScripts, state, moves, moveVec);
		//System.out.println("Time used: "+(System.nanoTime()-startTime)/1000000);
	}

	public ArrayList<Integer> getSeedScripts(GameState state, int playerId, ArrayList<Integer> enemy_scripts) {

		ArrayList<Player> portfolio;
		if (playerId == 0) {
			portfolio = portfolio0;
		} else {
			portfolio = portfolio1;
		}

		int bestValue = -99999;
		int bestScript = 0;
		ArrayList<Integer> seedScripts = new ArrayList<Integer>();
		//System.out.println(seedScripts.size()+ " "+enemy_scripts.size());
		
		for (int i = 0; i < portfolio.size(); i++) {
			if (LIMITTIME && (System.nanoTime() - startTime > timeLimit)) {
				break; // this kind of structure is safer.
			}
			fill(seedScripts, i);
			int value = playout(state, playerId, seedScripts, enemy_scripts);
			if (value > bestValue) {
				bestValue = value;
				bestScript = i;
			}
		}
		fill(seedScripts, bestScript);
		return seedScripts;
	}

	public void improve(GameState state, int playerId, ArrayList<Integer> self_scripts,
			ArrayList<Integer> enemy_scripts) {
		ArrayList<Player> portfolio;
		if (playerId == _id) {
			portfolio = portfolio0;
		} else {
			portfolio = portfolio1;
		}

		for (int i = 0; i < Iteration; i++) {
			for (int u = 0; u < numOfUnits; u++) {
				boolean findnew = false;
				int bestValue = -99999;
				int bestScript = 0;
				for (int s = 0; s < portfolio.size(); s++) {
					if (LIMITTIME && (System.nanoTime() - startTime > timeLimit)) {
						if (findnew) {
							self_scripts.set(u, bestScript);
						}
						return;
					}
					self_scripts.set(u, s);
					int value = playout(state, playerId, self_scripts, enemy_scripts);
					if (value > bestValue) {
						findnew = true;
						bestValue = value;
						bestScript = s;
					}
				}
				self_scripts.set(u, bestScript);
			}
		}
		// System.out.println("Improved scripts: "+self_scripts);
		// System.out.println("Count: "+count);
	}

	public int playout(GameState state, int playerId, ArrayList<Integer> our_scripts, ArrayList<Integer> enemy_scripts) {
		GameState sc = state.clone(); // sc for state clone
		Game gc;
		
		if (playerId == 0) {
			_pg_helper0.setScripts(our_scripts);
			_pg_helper1.setScripts(enemy_scripts);
			gc = new Game(sc, pg_helper0,
					pg_helper1, ROUNDLIMIT, false); 
		} else {
			_pg_helper1.setScripts(our_scripts);
			_pg_helper0.setScripts(enemy_scripts);
			gc = new Game(sc, pg_helper0,
					pg_helper1, ROUNDLIMIT, false); 
		}
		//gc.play();
		gc.playWithTimeLimit(startTime,timeLimit);
		// Game gc = new Game(sc, new Player_NoOverKillAttackValue(_id),
		// new Player_NoOverKillAttackValue(), ROUNDLIMIT, false); //send
		// scripts to game...
		// gc.play();

		int scoreval = gc.getState().eval(playerId, EvaluationMethods.LTD2)._val;
		//System.out.println("ID: "+playerId+" score: "+scoreval);
		return scoreval;
	}

	/////////////////////////////// FIXED/////////////////////////
	public void fill(ArrayList<Integer> seedScripts, int scriptI) {
		// seedScripts has to be initialized before using this helper
		seedScripts.clear();
		for (int i = 0; i < numOfUnits; i++) {
			seedScripts.add(scriptI);
		}
	}

	public void makeMove(ArrayList<Integer> scriptsToUse,GameState state, HashMap<Integer,List<UnitAction>> moves,
			List<UnitAction> moveVec){

		ArrayList<Player> portfolio;
		if(_id==0){portfolio = portfolio0;}else{
			portfolio = portfolio1;
		}
		
		ArrayList<ArrayList<Integer>> su = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<portfolio.size();i++){
			su.add(new ArrayList<Integer>());
		}
		
		for(Integer u : moves.keySet()){
			int scriptN = scriptsToUse.get(u);
			su.get(scriptN).add(u);//add each unit to its script.
		}
		
		for(int s=0;s<portfolio.size();s++){
			int numUnitsInScript = su.get(s).size();
			if(numUnitsInScript>0){
				Player scriptToUse = portfolio.get(s);
				HashMap<Integer,List<UnitAction>> scriptUnitMap = new HashMap<Integer,List<UnitAction>>();
				for(int z=0;z<numUnitsInScript;z++){
					int unit = su.get(s).get(z);
					scriptUnitMap.put(unit, moves.get(unit));
				}
				scriptToUse.getMoves(state, scriptUnitMap, moveVec);
			}
		}
	}

	public String toString() {
		return NAME;
	}

	public void setNumUnit(int i) {
		numOfUnits = i;
	}
}
