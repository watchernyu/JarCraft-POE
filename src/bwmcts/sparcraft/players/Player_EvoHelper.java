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

public class Player_EvoHelper extends Player {
	//this class serves as a helper for the actual online evolution player
	private int _id = 0;
	private int enemy;

	public Player_EvoHelper(int playerID) {
		_id = playerID;
		setID(playerID);
		enemy = GameState.getEnemy(_id);
	}

	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec, ArrayList<Integer> DNAi) {
		moveVec.clear();
		List<UnitAction> actions;
		for (Integer u : moves.keySet()) {//currently only testing for one unit....
			actions = moves.get(u);// a list of unit actions of this unit
			moveVec.add(actions.get(DNAi.get(u)));////the helper will return moves according to the DNA
		}
	}
	
	public String toString() {
		return "Evo helper";
	}
}
