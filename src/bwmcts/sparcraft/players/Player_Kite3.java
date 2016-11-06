/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_Kite3 extends Player {
	
	private int _id=0;
	private int enemy;
	
	public Player_Kite3(int playerID) {
		_id=playerID;
		setID(playerID);
		enemy=GameState.getEnemy(_id);
	}

	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		moveVec.clear();
		boolean foundUnitAction					= false;
		int actionMoveIndex						= 0;
		int furthestMoveIndex					= 0;
		int furthestMoveDist					= 0;
		int closestMoveIndex					= 0;
		int actionDistance						= Integer.MAX_VALUE;
		int closestMoveDist		  			= Integer.MAX_VALUE;
		Unit ourUnit, closestUnit=null;
		List<UnitAction> actions;
		int dist=0, bestMoveIndex = 0;
		UnitAction move;
		
		float totaldpf = 0;
		//testing
		
		int score = state.LTD2(this.ID());
		//System.out.println("Player1 state eval score: "+score);
		
		for (Integer u : moves.keySet())
		{
			foundUnitAction		= false;
			actionMoveIndex		= 0;
			furthestMoveIndex	= 0;
			furthestMoveDist	= 0;
			closestMoveIndex	= 0;
			actionDistance		= Integer.MAX_VALUE;
			closestMoveDist		= Integer.MAX_VALUE;

			ourUnit				= (state.getUnit(_id, u));
			totaldpf=totaldpf+ourUnit.dpf();
			
			actions=moves.get(u);//a list of unit actions of this unit
			for (int m = 0; m <actions.size(); ++m)
			{
				move						= actions.get(m);
					
				if (move.type() == UnitActionTypes.ATTACK)
				{
					dist			= ourUnit.getDistanceSqToUnit(state.getUnit(enemy, move._moveIndex), state.getTime());

					if (dist < actionDistance)
					{
						actionDistance = dist;
						actionMoveIndex = m;
						foundUnitAction = true;
					}
				}
				else if (move.type() == UnitActionTypes.HEAL)
				{

					dist				= ourUnit.getDistanceSqToUnit(state.getUnit(move.player(), move._moveIndex), state.getTime());

					if (dist < actionDistance)
					{
						actionDistance = dist;
						actionMoveIndex = m;
						foundUnitAction = true;
					}
				}
				else if (move.type() == UnitActionTypes.MOVE)
				{
					//Position ourDest = new Position(ourUnit.pos().getX() + Constants.Move_Dir[move._moveIndex][0], 
					//								 ourUnit.pos().getY() + Constants.Move_Dir[move._moveIndex][1]);
					closestUnit			= (ourUnit.canHeal() ? state.getClosestOurUnit(_id, u) : state.getClosestEnemyUnit(ourUnit.currentPosition(state._currentTime),enemy,Integer.MAX_VALUE,0,0));
					dist = closestUnit.getDistanceSqToPosition(ourUnit.pos().getX() + Constants.Move_DirX[move._moveIndex],ourUnit.pos().getY() + Constants.Move_DirY[move._moveIndex], state.getTime());

					if (dist > furthestMoveDist)
					{
						furthestMoveDist = dist;
						furthestMoveIndex = m;
					}

					if (dist < closestMoveDist)
					{
						closestMoveDist = dist;
						closestMoveIndex = m;
					}
				}
			}

			// the move we will be returning
			bestMoveIndex = 0;
			
			// if we have an attack move we will use that one
			if (foundUnitAction) //if can attack now
			{
				bestMoveIndex = actionMoveIndex;
			}
			// otherwise use the closest move to the opponent
			else
			{
				 //if we are in attack range of the unit, back up
				if (ourUnit.canAttackTarget(closestUnit, state.getTime()))
				{
				bestMoveIndex = furthestMoveIndex;
				}
				//otherwise get back into the fight
				else
				{
					bestMoveIndex = closestMoveIndex;
				}
			}
			
			
			//if I can put some state-based stuff here and then pick a new bestMoveIndex...
			int moveLimit = 30; //so this is the limit of a game's length
			GameState stateClone = state.clone();
			Game gameClone = new Game(stateClone, new Player_NoOverKillAttackValue(this.enemy), new Player_NoOverKillAttackValue(this.enemy),moveLimit, false);
			gameClone.play(); //so this will simply play til the end...
		    GameState finalState = gameClone.getState();
		    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
		    StateEvalScore gameScore = finalState.eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2);
			System.out.println(gameScore._val);
			
			
			//not necessary to use play() but we can use the following:
			//state.makeMoves(moveVec); //let's not worry about its details for now
	        //state.finishedMoving();
			//moveVec.clear();
			
			moveVec.add(actions.get(bestMoveIndex));
		}
		//System.out.println(totaldpf);
	}
	
	public String toString(){
		return "Kite";
	}
}
