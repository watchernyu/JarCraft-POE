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
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_pg_helper extends Player {
	//helper for PORTFOLIO GREEDY SEARCH
	
	private int _id=0;
	ArrayList<Integer> ourScripts;
	ArrayList<Player> portfolio0;
	ArrayList<Player> portfolio1;
	ArrayList<Player> portfolio;//this is the one used
	ArrayList<ArrayList<Integer>> su;
	
	public Player_pg_helper(int playerID,ArrayList<Player> portfolio0,ArrayList portfolio1) {
		//BASICALLY THIS HELPER PLAYER HAS A COLLECTION OF SCRIPTS
		//AND EACH UNIT IN A PLAYOUT WILL BE FOLLOWING ITS OWN SCRIPT TO THE END.
		this.portfolio0 = portfolio0;
		this.portfolio1 = portfolio1;
		_id=playerID;
		setID(playerID);
		if(_id==0){portfolio = portfolio0;}else{
			portfolio = portfolio1;
		}
	}
	
	public void setScripts(ArrayList<Integer> ourScripts){
		this.ourScripts = ourScripts;
	}

	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
	    moveVec.clear();
	    makeMove(ourScripts,state,moves,moveVec);
	}
	
	public void makeMove(ArrayList<Integer> scriptsToUse,GameState state, HashMap<Integer,List<UnitAction>> moves,
			List<UnitAction> moveVec){
		su = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<portfolio.size();i++){
			su.add(new ArrayList<Integer>());
		}
		for(Integer u : moves.keySet()){
			//System.out.println(u);
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
	
	public String toString(){
		return "PGS helper";
	}
}
