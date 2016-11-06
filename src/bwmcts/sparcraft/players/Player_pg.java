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

public class Player_pg extends Player {
	//PORTFOLIO GREEDY SEARCH
	//THIS IS NOW SPECIFICALLY THE OLD CHURCHILL'S VERSION.
	
	private int _id=0;
	private int _eid = 1;
	ArrayList<Player> portfolioA;
	ArrayList<Player> portfolioB;
	ArrayList<Player> ourScripts;
	ArrayList<Player> enemyScripts;
	int Iteration = 1;
	int R = 0;
	int numOfUnits = 0;
	long timeLimit = 40000000; //in nano sec 1 millie =1 000 000
	long timeElapsed = 0;
	long startTime = 0;
	boolean LIMITTIME = true;
	int ROUNDLIMIT = 200;
	boolean REACHLIMIT = false;
	
	public Player_pg(int playerID) {
		_id=playerID;
		_eid = (playerID+1)%2;
		setID(playerID);
		portfolioA = new ArrayList<Player>();
		portfolioB = new ArrayList<Player>();
		ourScripts= new ArrayList<Player>();
		enemyScripts= new ArrayList<Player>();
		
		///////////////////////////////////////////
		//SINCE EACH PLAYER HAS A ID IN IT..... WE NEED TO HAVE TWO PORTFOLIO....
		portfolioA.add(new Player_NoOverKillAttackValue(_id));
		portfolioA.add(new Player_KiteDPS(_id));
		
		portfolioB.add(new Player_NoOverKillAttackValue(_eid));
		portfolioB.add(new Player_KiteDPS(_eid));
		//portfolio.add(new Player_NOKAVForward(playerID));
		//portfolio.add(new Player_NOKAVBack(playerID));
		//portfolio.add(new Player_NOKAVForwardFar(playerID));
		//portfolio.add(new Player_NOKAVBackClose(playerID));
		//portfolio.add(new Player_NOKAVBackFar(playerID));
		
		setting1();
	}
	
	private void initSettingEnhanced(){
		//called after setting the num of units
		if (numOfUnits>=64){
			ROUNDLIMIT = 10;
		}else if (numOfUnits>=48){
			ROUNDLIMIT = 15;
		}else if (numOfUnits>=32){
			ROUNDLIMIT = 35;
		}else if (numOfUnits>=24){
			ROUNDLIMIT = 60;
		}else if(numOfUnits>=16){
			ROUNDLIMIT = 100; 
		}else{
			ROUNDLIMIT = 200;
		}
	}
	
	private void initSetting(){
		//call this after setting the num of units
		//if (numOfUnits>=32){
		//	setting2();
		//}
		//if(numOfUnits>=48){
		//	setting3();
		//}
	}

	public void setting1(){
		Iteration = 1;
		ROUNDLIMIT = 200;
	}
	
	public void setting2(){
		Iteration = 1;
		ROUNDLIMIT = 40;
	}
	
	public void setting3(){
		Iteration = 1;
		ROUNDLIMIT = 15;
	}
	
	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		startTime = System.nanoTime();
	    moveVec.clear();
	    REACHLIMIT = false;
	    
	    for(int i=0;i<numOfUnits;i++){
	    	enemyScripts.add(portfolioB.get(0));
	    }
	    
	    fill(enemyScripts,portfolioB.get(0));
	    ourScripts = getSeedScripts(state, _id, enemyScripts);
	    enemyScripts = getSeedScripts(state, (_id+1)%2,ourScripts);
	    //System.out.println(ourScripts);
	    improve(state, _id, ourScripts,enemyScripts);
	    //System.out.println(ourScripts);
	    for(int r=0;r<R;r++){
			if(LIMITTIME&&(System.nanoTime()-startTime>timeLimit)){
				break; //this kind of structure is safer.
			}
	    	improve(state, _id, ourScripts,enemyScripts);
	    	improve(state, (_id+1)%2,enemyScripts,ourScripts);
	    }
	    
	    //System.out.println("Time used: "+(System.nanoTime()-startTime)/1000000);
	    makeMove(ourScripts,state,moves,moveVec);
	}
	
	public ArrayList<Player> getSeedScripts(GameState state,int playerId,ArrayList<Player> enemy_scripts){
		ArrayList<Player> portfolio;
		if(playerId==_id){portfolio=portfolioA;}else{portfolio=portfolioB;}
		
		int bestValue = -99999;
		Player bestScript=portfolio.get(0);
		ArrayList<Player> seedScripts = new ArrayList<Player>();
		for(int i=0;i<numOfUnits;i++){
			seedScripts.add(portfolio.get(0));
		}
		
		for(int i=0;i<portfolio.size();i++){
			if(LIMITTIME&&(System.nanoTime()-startTime>timeLimit)){
				break; //this kind of structure is safer.
			}
			fill(seedScripts,portfolio.get(i));
			int value = playout(state,playerId,seedScripts,enemy_scripts);

			//need to create a helper player that
			//has a sequence of scripts and will getmoves from those scripts.
			
			//we need to do TWO VERSIONS OF THE PLAYOUT
			//ONE VERSION IS SAME TO THE ONE IN THE PAPER:
			//IN THE PLAYOUT, EACH UNIT FOLLOW ITS ASSIGNED SCRIPT
			//THE OTHER VERSION... WE WILL THINK ABOUT IT...
			//THE OTHER VERSION: EACH UNIT USE THEIR OWN SCRIPT AT FIRST STEP THEN PLAYOUT ALL WITH NOKAV
			if (value>bestValue){
				bestValue = value;
				bestScript = portfolio.get(i);
			}
			//System.out.println("BestScript: "+bestScript+" Best value: "+bestValue);
		}
		fill(seedScripts,bestScript);
		return seedScripts;
	}
	
	public void improve(GameState state,int playerId,ArrayList<Player> self_scripts,ArrayList<Player> enemy_scripts){
		ArrayList<Player> portfolio;
		if(playerId==_id){portfolio=portfolioA;}else{portfolio=portfolioB;}
		
		int count = 0;//just for testing.
		for (int i=0;i<Iteration;i++){
			for (int u=0;u<numOfUnits;u++){
				boolean findnew = false;
				int bestValue = -99999;
				Player bestScript=portfolio.get(0);
				for(int s=0;s<portfolio.size();s++){
					if(LIMITTIME&&(System.nanoTime()-startTime>timeLimit)){
						if(findnew){self_scripts.set(u, bestScript);}
						return;
					}
					count++;
					self_scripts.set(u, portfolio.get(s));
					int value = playout(state,playerId,self_scripts,enemy_scripts);
					if(value>bestValue){
						findnew = true;
						bestValue = value;
						bestScript = portfolio.get(s);
					}
				}
				self_scripts.set(u, bestScript);
			}
		}
		//System.out.println("Improved scripts: "+self_scripts);
		//System.out.println("Count: "+count);
	}
	
	public int playout(GameState state,int playerId,ArrayList<Player> our_scripts,ArrayList<Player> enemy_scripts){
		GameState sc = state.clone(); // sc for state clone
		
		Game gc;
		
		ArrayList<Player> portoflio0;
		ArrayList<Player> portoflio1;
		if(_id==0){
			portoflio0 = portfolioA;
			portoflio1 = portfolioB;
		}else{
			portoflio1 = portfolioA;
			portoflio0 = portfolioB;
		}
		
		if(playerId==0){
			 gc = new Game(sc, new Player_pg_helper(playerId,our_scripts,portoflio0),
					new Player_pg_helper((playerId+1)%2,enemy_scripts,portoflio1), ROUNDLIMIT, false); //send scripts to game...
		}else{
			 gc = new Game(sc, new Player_pg_helper((playerId+1)%2, enemy_scripts,portoflio0),
					 new Player_pg_helper(playerId,our_scripts,portoflio1), ROUNDLIMIT, false); //send scripts to game...
		}

		gc.play();
		//Game gc = new Game(sc, new Player_NoOverKillAttackValue(_id),
		//		new Player_NoOverKillAttackValue(), ROUNDLIMIT, false); //send scripts to game...
		//gc.play();
		
		int scoreval = gc.getState().eval(playerId, EvaluationMethods.LTD2)._val;
		//System.out.println("ID: "+playerId+" score: "+scoreval);
		return scoreval;
	}
	
	public void fill(ArrayList<Player> seedScripts,Player s){
		//seedScripts has to be initialized before using this helper
		int n = seedScripts.size();
		for(int i=0;i<n;i++){
			seedScripts.set(i, s);
		}
	}
	
	public void makeMove(ArrayList<Player> scriptsToUse,GameState state, HashMap<Integer,List<UnitAction>> moves,
			List<UnitAction> moveVec){
		HashMap<Player,ArrayList<Integer>> su = new HashMap<Player,ArrayList<Integer>>();
		
		for(int i=0;i<portfolioA.size();i++){
			su.put(portfolioA.get(i), new ArrayList<Integer>());
		}

		for(Integer u : moves.keySet()){
			Player scriptN = scriptsToUse.get(u);
			su.get(scriptN).add(u);//add each unit to its script.
		}
		
		for(int s=0;s<portfolioA.size();s++){
			int numUnitsInScript = su.get(portfolioA.get(s)).size();
			if(numUnitsInScript>0){
				Player scriptToUse = portfolioA.get(s);
				HashMap<Integer,List<UnitAction>> scriptUnitMap = new HashMap<Integer,List<UnitAction>>();
				for(int z=0;z<numUnitsInScript;z++){
					int unit = su.get(scriptToUse).get(z);
					scriptUnitMap.put(unit, moves.get(unit));
				}
				scriptToUse.getMoves(state, scriptUnitMap, moveVec);
			}
		}
	}
	
	
	public String toString(){
		return "Portfolio Greedy Search-original";
	}
	
	public void setNumUnit(int i){
		numOfUnits = i;
		initSetting();
	}
}
