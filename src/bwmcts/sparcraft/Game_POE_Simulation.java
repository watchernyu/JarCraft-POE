/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class Game_POE_Simulation {

	/**
	 * @param args
	 */
	
	private GameState state;
	
	private Player[]	_players=new Player[2];
	private int				rounds;
	public int				moveLimit;
	
	public ArrayList<Player> scripts;
	private boolean STOP;

	public Game_POE_Simulation(GameState initialState, Player p1, Player p2, int limit, ArrayList<Player> scripts){
		state=initialState;
		_players[0]=p1; //this is NOKAV player with id = 0 
		_players[1]=p2;//this is NOKAV player with id = 1 
		this.moveLimit=limit;
		this.rounds=0;
		this.scripts = scripts;//NEW NEW NEW NEW NEW
		
	}
	
// play the game until there is a winner
	public void play(){
		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;

        long testtt = System.nanoTime();//T
	    while (!this.gameOver()){
	    	
	        if (rounds >= moveLimit)
	        {
	            break;
	        }
	    	
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove();
	        toMove = _players[playerToMove];
	        enemy = _players[GameState.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        moves_A.clear();
	        moves_B.clear();

			state.generateMoves(moves_A, toMove.ID());

	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {

	        	state.generateMoves(moves_B, enemy.ID());

				enemy.getMoves(state, moves_B, scriptMoves_B);

	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);

	        }
	        
	        // the tuple of moves he wishes to make
	        toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	       
	        // make the moves
			state.makeMoves(scriptMoves_A); //let's not worry about its details for now
			
			//System.out.println("A moves: "+scriptMoves_A);

	        state.finishedMoving();
		    
	        rounds++;
	        
	    }
	    //System.out.println(System.nanoTime()-testtt);
	}
	
	// play the game until there is a winner
	public void playWithTimeLimit(long startTime,long timeLimit){
		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;

	    while (!this.gameOver()){
	        if (rounds >= moveLimit)
	        {
	            break;
	        }
	        if(rounds%20==19){
	        	if(System.nanoTime()-startTime>timeLimit)
	        	{break;}}
	    	
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove();
	        toMove = _players[playerToMove];
	        enemy = _players[GameState.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        moves_A.clear();
	        moves_B.clear();

			state.generateMoves(moves_A, toMove.ID());

	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {

	        	state.generateMoves(moves_B, enemy.ID());

				enemy.getMoves(state, moves_B, scriptMoves_B);

	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);

	        }
	        
	        // the tuple of moves he wishes to make
	        toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	       
	        // make the moves
			state.makeMoves(scriptMoves_A); //let's not worry about its details for now
	        state.finishedMoving();
	        rounds++;
	    }
	}
	
	public void dnaMoves(ArrayList<ArrayList<Integer>> DNA,int[] monitor,
			HashMap<Integer,List<UnitAction>> moves, ArrayList<UnitAction> scriptMoves){
		//System.out.println(moves.size());
		ArrayList<ArrayList<Integer>> su = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<scripts.size();i++){
			su.add(new ArrayList<Integer>());
		}
		
		for(Integer u : moves.keySet()){
			int scriptN = DNA.get(monitor[u]).get(u);
			su.get(scriptN).add(u);//add each unit to its script.
			monitor[u]+=1;
			if(monitor[u]>=DNA.size()-1){STOP=true;}
		}
		
		for(int s=0;s<scripts.size();s++){
			int numUnitsInScript = su.get(s).size();
			if(numUnitsInScript>0){
				Player scriptToUse = scripts.get(s);
				HashMap<Integer,List<UnitAction>> scriptUnitMap = new HashMap<Integer,List<UnitAction>>();
				for(int z=0;z<numUnitsInScript;z++){
					int unit = su.get(s).get(z);
					scriptUnitMap.put(unit, moves.get(unit));
				}
				scriptToUse.getMoves(state, scriptUnitMap, scriptMoves);
			}
		}
	}

	//THIS IS WHERE PROBLEM COMES
	public int dnaEvalGroup(ArrayList<ArrayList<Integer>> DNA,int method,int us_id, int enemy_id){
		//method, 0 = LTD2
		//1 = playout
		
		//DNA as a sequence of scripts

		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;

        //System.out.println("!!");
        
        int size = DNA.size();
        int index = 0; //break out of game loop when index == size;
        int monitor[] = new int[DNA.get(0).size()];//hardcoded to keep track of how many scripts a unit has finished
	    Arrays.fill(monitor, 0);
	    STOP = false;
        while (!this.gameOver()){
	        if (STOP||rounds >= moveLimit)
	        {
	            break;
	        }
	    	
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove();
	        
	        //here toMove and enemy refer to NOKAV players
	        toMove = _players[playerToMove];
	        enemy = _players[GameState.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        moves_A.clear();
	        moves_B.clear();

	        state.generateMoves(moves_A, toMove.ID());
	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {
	        	state.generateMoves(moves_B, enemy.ID());
	        	if(enemy.ID()==us_id){
	        		dnaMoves(DNA,monitor,moves_B, scriptMoves_B);
	        	}else{
	        		enemy.getMoves(state, moves_B, scriptMoves_B);
	        	}
	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);
	        }
	        
	        if(toMove.ID()==us_id){
	        	dnaMoves(DNA,monitor,moves_A, scriptMoves_A);
	        }else{
	        	toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	        }
	       
	        // make the moves
			state.makeMoves(scriptMoves_A); //let's not worry about its details for now
			//System.out.println("A moves: "+scriptMoves_A);
	        
	        state.finishedMoving();
	        rounds++;
	    }
	    
        if(method==0){
        	return this.getState().eval(us_id, EvaluationMethods.LTD2)._val;
        }
        
        rounds=0;
        moveLimit = 15;
		_players[0]=new Player_NoOverKillAttackValue(Players.Player_One.ordinal());
		_players[1]=new Player_NoOverKillAttackValue(Players.Player_Two.ordinal());
	    while (!this.gameOver()){
	        if (rounds >= moveLimit)
	        {
	            break;
	        }
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove();
	        toMove = _players[playerToMove];
	        enemy = _players[GameState.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        moves_A.clear();
	        moves_B.clear();

			state.generateMoves(moves_A, toMove.ID());

	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {

	        	state.generateMoves(moves_B, enemy.ID());

				enemy.getMoves(state, moves_B, scriptMoves_B);

	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);

	        }
	        
	        // the tuple of moves he wishes to make
	        toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	       
	        // make the moves
			state.makeMoves(scriptMoves_A); //let's not worry about its details for now
			
			//System.out.println("A moves: "+scriptMoves_A);
			state.finishedMoving();
	        rounds++;
	    }
        
	    int scoreval=this.getState().eval(us_id, EvaluationMethods.LTD2)._val;
	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    return scoreval;
	}	
	
	public int getRounds(){
		return rounds;
	}
	

// returns whether or not the game is over
	public boolean gameOver()
	{
	    return state.isTerminal(); 
	}

	public GameState getState()
	{
	    return state;
	}

// determine the player to move
	public int getPlayerToMove()
	{
	   Players whoCanMove=state.whoCanMove();
	
	   Players random = Math.random() >= 0.5 ? Players.Player_One : Players.Player_Two;
	   
	   return whoCanMove==Players.Player_Both ? random.ordinal(): whoCanMove.ordinal();
	}
}