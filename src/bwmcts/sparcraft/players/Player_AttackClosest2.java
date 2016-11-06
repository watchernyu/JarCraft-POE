/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_AttackClosest2 extends Player {

	private int _id = 0;

	public Player_AttackClosest2(int playerID) {
		_id = playerID;
		setID(playerID);
	}

	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
	/*
		System.out.println("moves:");
		for (Integer key : moves.keySet()){
			List<UnitAction> z = moves.get(key);
			for (int i=0;i<z.size();i++){
				System.out.println(z.get(i));
			}
		}
		*/
		
		for (int u = 0; u < moves.size(); u++) {//for every unit.
			//the size of moves is the number of units you have.
			//System.out.println("moves size: "+moves.size());
			boolean foundUnitAction = false;
			int actionMoveIndex = 0;
			int closestMoveIndex = 0;
			int actionDistance = Integer.MAX_VALUE;
			int closestMoveDist = Integer.MAX_VALUE;
			int furthestMoveIndex	= 0;
			int furthestMoveDist	= 0;

			Unit ourUnit = state.getUnit(ID(), u);
			//refer to the current unit to control

			Unit closestUnit = ourUnit.canHeal() ? state.getClosestOurUnit(ID(), u) : state.getClosestEnemyUnit(_id, u);
			//don't care for heal now... so on this line closestUnit now is set to closest enemy unit
			
			for (int m = 0; m < moves.get(u).size(); m++) {//for every move that this unit can conduct
				//(moves include walk in 4 direction, move/attack one of the enemies or do sth to an allied unit)
				UnitAction move = moves.get(u).get(m); //get a move of the current unit

				if (move.type() == UnitActionTypes.ATTACK) {
					Unit target = state.getUnit(GameState.getEnemy(move.player()), move.index());
					int dist = ourUnit.getDistanceSqToUnit(target, state.getTime());
					//dist = distance between 2 units

					if (dist < actionDistance) {
						actionDistance = dist;
						actionMoveIndex = m; 
						foundUnitAction = true;
					}
				}
				if (move.type() == UnitActionTypes.HEAL) {
					Unit target = state.getUnit(move.player(), move.index());
					int dist = ourUnit.getDistanceSqToUnit(target, state.getTime());

					if (dist < actionDistance) {
						actionDistance = dist;
						actionMoveIndex = m;
						foundUnitAction = true;
					}
				} else if (move.type() == UnitActionTypes.RELOAD) {
					if (ourUnit.canAttackTarget(closestUnit, state.getTime())) {
						closestMoveIndex = m;
						break;
					}
				} else if (move.type() == UnitActionTypes.MOVE) {
					Position ourDest = new Position(ourUnit.pos().getX() + Constants.Move_Dir[move.index()][0],
							ourUnit.pos().getY() + Constants.Move_Dir[move.index()][1]);
					int dist = closestUnit.getDistanceSqToPosition(ourDest, state.getTime());

					if (dist < closestMoveDist) {
						closestMoveDist = dist;
						closestMoveIndex = m;
					}
					
					if(dist>furthestMoveDist){
						furthestMoveDist = dist;
						furthestMoveIndex = m;
					}
				}
			}

			int bestMoveIndex = foundUnitAction ? actionMoveIndex : closestMoveIndex;
			if(ourUnit.currentHP()<10){
				bestMoveIndex = furthestMoveIndex;
			}
			//if can attack then attack closest, otherwise move closer to the closest enemy

			moveVec.add(moves.get(u).get(bestMoveIndex));
			//when loop through moveVec, each move in the list corresponds to a unit's next move
		}
	}

	public String toString() {
		return "AttackClosest";
	}
}
