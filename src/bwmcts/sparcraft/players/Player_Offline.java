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

public class Player_Offline extends Player {
	
	private int _id=0;
	private int enemy;
	private int A = 0;
	private int B = 0;
	private int C = 0;
	
	public Player_Offline(int playerID) {
		_id=playerID;
		setID(playerID);
		enemy=GameState.getEnemy(_id);
	}
	
	public Player_Offline(int playerID,int a,int b,int c) {
		_id=playerID;
		setID(playerID);
		enemy=GameState.getEnemy(_id);
		this.A = a;
		this.B = b;
		this.C = c;
	}
	
	public void setGenome(int a, int b, int c){
		this.A = a;
		this.B = b;
		this.C = c;
	}

	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		moveVec.clear();
		boolean foundUnitAction					= false;
		int actionMoveIndex						= 0;
		int backMoveIndex					= 0;
		int backMoveDist					= 0;
		int forwardMoveIndex					= 0;
		int actionDistanceClose						= Integer.MAX_VALUE;
		int actionDistanceFar =0;
		int forwardMoveDist		  			= Integer.MAX_VALUE;
		Unit ourUnit, closestUnit=null;
		List<UnitAction> actions;
		int dist=0, bestMoveIndex = 0;
		UnitAction move;
		int hp=0;
		int minhp = 9999;
		for (Integer u : moves.keySet())
		{
			foundUnitAction		= false;
			actionMoveIndex		= 0;
			backMoveIndex	= 0;
			

			if(B==0){ //move away from closest enemy
				backMoveDist	= 0;
			}else if(B==1){//move to closest ally
				backMoveDist	= Integer.MAX_VALUE;
			}else if(B==2){//move to farthest ally
				backMoveDist	= Integer.MAX_VALUE;
			}
			
			
			
			
			forwardMoveIndex	= 0;
			actionDistanceClose		= Integer.MAX_VALUE;
			actionDistanceFar		= 0;
			
			hp=0;
			minhp = 9999;
			
			forwardMoveDist		= Integer.MAX_VALUE;

			ourUnit				= (state.getUnit(_id, u));
			
			actions=moves.get(u);//a list of unit actions of this unit
			
			for (int m = 0; m <actions.size(); ++m)
			{
				move						= actions.get(m);
				//System.out.println("u index: "+move.getUnitIndex());	
				if (move.type() == UnitActionTypes.ATTACK)
				{
					dist			= ourUnit.getDistanceSqToUnit(state.getUnit(enemy, move._moveIndex), state.getTime());
					Unit target			=state.getUnit(GameState.getEnemy(move.player()), move.index());
					
					if(A==0){//attack closest
						if (dist < actionDistanceClose)
						{
							actionDistanceClose = dist;
							actionMoveIndex = m;
							foundUnitAction = true;
						}
					}else if(A==1){//attack farthest
						if(dist>actionDistanceFar){
							actionDistanceFar = dist;
							actionMoveIndex = m;
							foundUnitAction = true;
						}
					}else if(A==2){//attack weakest
						hp = target.currentHP();
						if (hp < minhp)
						{
							minhp = hp;
							actionMoveIndex = m;
							foundUnitAction = true;
						}
					}
				}
				else if (move.type() == UnitActionTypes.MOVE)
				{
					//System.out.println(move);
					//Position ourDest = new Position(ourUnit.pos().getX() + Constants.Move_Dir[move._moveIndex][0], 
					//								 ourUnit.pos().getY() + Constants.Move_Dir[move._moveIndex][1]);
					closestUnit			= (ourUnit.canHeal() ? state.getClosestOurUnit(_id, u) : state.getClosestEnemyUnit(ourUnit.currentPosition(state._currentTime),enemy,Integer.MAX_VALUE,0,0));
					if(B==0){ //move away from closest enemy
						dist = closestUnit.getDistanceSqToPosition(ourUnit.pos().getX() + Constants.Move_DirX[move._moveIndex],ourUnit.pos().getY() + Constants.Move_DirY[move._moveIndex], state.getTime());

						if (dist > backMoveDist)
						{
							backMoveDist = dist;
							backMoveIndex = m;
						}
					}else if(B==1){//move to closest ally
						Unit closestAlly = state.getClosestOurUnit(ID(), u);
						dist = closestAlly.getDistanceSqToPosition(ourUnit.pos().getX() + Constants.Move_DirX[move._moveIndex],ourUnit.pos().getY() + Constants.Move_DirY[move._moveIndex], state.getTime());
						if (dist < backMoveDist)
						{
							backMoveDist = dist;
							backMoveIndex = m;
						}
					}else if(B==2){//move to closest ally
						Unit farthestAlly = state.getFarthestOurUnit(ID(), u);
						dist = farthestAlly.getDistanceSqToPosition(ourUnit.pos().getX() + Constants.Move_DirX[move._moveIndex],ourUnit.pos().getY() + Constants.Move_DirY[move._moveIndex], state.getTime());
						if (dist < backMoveDist)
						{
							backMoveDist = dist;
							backMoveIndex = m;
						}
					}

					if(C==0){ // to closest enemy
						if (dist < forwardMoveDist)
						{
							forwardMoveDist = dist;
							forwardMoveIndex = m;
						}
					}else if(C==1){// to farthest enemy
						Unit farthestEnemy=state.getFarthestEnemyUnit(_id,u);
						dist = farthestEnemy.getDistanceSqToPosition(ourUnit.pos().getX() + Constants.Move_DirX[move.index()], 
								ourUnit.pos().getY() + Constants.Move_DirY[move.index()], state.getTime());
						if (dist < forwardMoveDist)
						{
							forwardMoveDist = dist;
							forwardMoveIndex = m;
						}
					}else if(C==2){// to enemy center
						//System.out.println("aa");
						Position enemyCenter = state.getEnemyCenter(_id, u);
						
						dist = ourUnit.getDistanceSqToPosition(enemyCenter.getX() - Constants.Move_DirX[move.index()], 
								enemyCenter.getY() - Constants.Move_DirY[move.index()], state.getTime());

						if (dist < forwardMoveDist)
						{
							forwardMoveDist = dist;
							forwardMoveIndex = m;
						}
					}
					

				}
			}

			// the move we will be returning
			bestMoveIndex = 0;

			// if we have an attack move we will use that one
			if (foundUnitAction)
			{
				bestMoveIndex = actionMoveIndex;
			}
			// otherwise use the closest move to the opponent
			else
			{
				 //if we are in attack range of the unit, back up
				if (ourUnit.canAttackTarget(closestUnit, state.getTime()))
				{
				bestMoveIndex = backMoveIndex;
				}
				//otherwise get back into the fight
				else
				{
					bestMoveIndex = forwardMoveIndex;
				}
			}
			
			moveVec.add(actions.get(bestMoveIndex));
		}
	}
	
	public String toString(){
		return "Kite";
	}
}
