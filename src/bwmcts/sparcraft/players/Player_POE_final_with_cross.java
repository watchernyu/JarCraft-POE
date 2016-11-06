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
import genetic.Population_final;
import genetic.Population_final_with_cross;

import java.util.Random;

public class Player_POE_final_with_cross extends Player {
	//Portfolio Online Evolution player with crossover implemented
	
	public int CROSSMODE = 0;
	//0 means random init then cross
	
	boolean allowInitRandomMutation = false;
	private String NAME;
	
	private int _id = 0;
	private int enemy_id;
	Random ran;
	ArrayList<Player> scripts;
	int scriptsSize;
	int numOfUnits=0;
	long timeLimit = 20000000;//20ms
	int EVALUTIONMETHOD = 1;//0 means LTD2, 1 means playout
	int futureSteps = 3;//3 steps before simulation with all NOKAV
	int numOfMutations = 30;//max number of mutations
	Population_final_with_cross P;
	boolean firstTimeInit;
	
	public Player_POE_final_with_cross(int playerID) {
		firstTimeInit=true;
		_id = playerID;
		setID(playerID);
		enemy_id = GameState.getEnemy(_id);
		ran = new Random();//initialize random generator
		init2scripts();//use this to initialize scripts.
	}
	
	public void init2scripts(){
		//init POE using only 2 scripts
		NAME = "POE_new_2";
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(_id));
		scripts.add(new Player_KiteDPS(_id));
		scriptsSize = scripts.size();
	}
	
	public void init6scripts(){
		//init POE with 6 different scripts
		//allow more freedom in unit movements
		//but restrict units to move forward
		//if enemy is not in sight
		//will cause quite chaotic behavior 
		//if no restrictions are set
		NAME = "POE_new_6";
		scripts = new ArrayList<Player>();

		//these are bad scripts to use...
		//will cause chaotic behavior, especially
		//in large battles 
		//use to demonstrate POE goes very bad 
		//when using scripts with large freedom
		//scripts.add(new Player_NOKAVMicroRight(_id));
		//scripts.add(new Player_NOKAVMicroLeft(_id));
		//scripts.add(new Player_NOKAVMicroDown(_id));
		//scripts.add(new Player_NOKAVMicroUp(_id));

		scripts.add(new Player_NoOverKillAttackValue(_id));
		scripts.add(new Player_NOKAVBack2(_id));
		scripts.add(new Player_NOKAVForward2(_id));
		scripts.add(new Player_NOKAVForwardFar2(_id));
		scripts.add(new Player_NOKAVBackClose2(_id));
		scripts.add(new Player_NOKAVBackFar2(_id));
		scriptsSize = scripts.size();
	}

	public void setTimeLimit(int tl){
		timeLimit = tl * 1000000;
	}
	public void setTimeLimit20(){
		timeLimit = 20000000;
	}
	public void setTimeLimit40(){
		timeLimit = 40000000;
	}	

	public void setFirstTimeInit(){
		//NEED TO SET THIS AT EACH BEGGINNING OF THE GAME (WHEN UNIT NUMBER CHANGES)
		firstTimeInit = true;
	}
	
	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
		List<UnitAction> actions;
		int bestMoveIndex = 0;
		UnitAction move;

		//System.out.println(moves.size());
		
		long startTime = System.nanoTime();
		//System.out.println("nano time: "+System.nanoTime());
		int numOfScripts = scripts.size();
		//DNA initialization
		//fix here: we need to add the sense of population.
		
		if(firstTimeInit){
			//will init population when the player is called
			//for the first time
			P = new Population_final_with_cross(this,state,futureSteps,numOfScripts,numOfUnits,scripts);
			
			if(CROSSMODE==0){
				P.initializeRandom();
			}else if(CROSSMODE == 1){
				P.initialize();
			}
			
			firstTimeInit = false;
		}
		
		if(CROSSMODE==0){
			P.reinitializeRandom(state,startTime);
		}else if(CROSSMODE == 1){
			P.reinitialize(state,startTime);
		}
		
		int evolveCount = 0;
		
		//NOW WE HAVE CROSSOVER SO NEED SOME CHANGE
		
		//0 means random init and then cross.
		if(CROSSMODE==0){
			for(int e=0;e<numOfMutations;e++){
				if(System.nanoTime()-startTime>timeLimit){
					break;
				}
				P.evolveWithCross(1);
				evolveCount++;
			}
		}else if(CROSSMODE==1){
			for(int e=0;e<numOfMutations;e++){
				if(System.nanoTime()-startTime>timeLimit){
					break;
				}
				P.evolveWithCross(1);
				evolveCount++;
			}
		}
		

		//System.out.println("evo: "+evolveCount);
		ArrayList<Integer> bestDnaPiece = P.bestDna().get(0);
		//System.out.println("Evolve Count: "+evolveCount+" Time used: "+(System.nanoTime()-startTime)/1000000);
		
		dnaMoves(state,bestDnaPiece,moves,moveVec);
	}

	public void dnaMoves(GameState state, ArrayList<Integer> DNAi,
			HashMap<Integer,List<UnitAction>> moves, List<UnitAction> moveVec){
		//this function will first get unit-script pairs
		//from DNAi, then assign all the units
		//to the script that they will be using
		//then use the script's getMoves() function
		//to fill moves in the moveVec


		ArrayList<ArrayList<Integer>> su = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<scriptsSize;i++){
			su.add(new ArrayList<Integer>());
		}
		//script-units
		
		for(Integer u : moves.keySet()){
			int scriptN = DNAi.get(u);
			su.get(scriptN).add(u);//add each unit to its script.
		}
		for(int s=0;s<scriptsSize;s++){
			int numUnitsInScript = su.get(s).size();
			if(numUnitsInScript>0){
				Player scriptToUse = scripts.get(s);
				HashMap<Integer,List<UnitAction>> scriptUnitMap = new HashMap<Integer,List<UnitAction>>();
				for(int z=0;z<numUnitsInScript;z++){
					int unit = su.get(s).get(z);
					scriptUnitMap.put(unit, moves.get(unit));
				}
				scriptToUse.getMoves(state, scriptUnitMap, moveVec);
			}
		}
	}

	public void setNumUnit(int n){
		this.numOfUnits = n;
		this.setFirstTimeInit();
	}
	
	public String toString() {
		return NAME;
	}
}
