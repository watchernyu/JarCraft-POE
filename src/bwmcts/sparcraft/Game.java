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

public class Game {

	/**
	 * @param args
	 */
	
	private GameState state;
	
	private Player[]	_players=new Player[2];
	private int				rounds;
	public int				moveLimit;
	
	private boolean display=false;
	public SparcraftUI ui;
	public ArrayList<Player> scripts;
	private boolean STOP;
		
	public Game(GameState initialState, Player p1, Player p2, int limit){
		state=initialState;
		_players[0]=p1;
		_players[1]=p2;
		this.moveLimit=limit;
		this.rounds=0;
		this.STOP = false;
	}

	public Game(GameState initialState, Player p1, Player p2, int limit, boolean display){
		state=initialState;
		_players[0]=p1;
		_players[1]=p2;
		this.moveLimit=limit;
		this.rounds=0;
		this.display=display;
		if (display){
	    	ui = SparcraftUI.getUI(state, p1, p2);
	    }
	}

	public Game(GameState initialState, Player p1, Player p2, int limit, boolean display,ArrayList<Player> scripts){
		state=initialState;
		_players[0]=p1;
		_players[1]=p2;
		this.moveLimit=limit;
		this.rounds=0;
		this.display=display;
		this.scripts = scripts;//NEW NEW NEW NEW NEW
		if (display){

	    	ui = SparcraftUI.getUI(state, p1, p2);
	        
	    }
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
			

	        if (display)
	        {
	        	GameState copy=state.clone();
	        	copy.finishedMoving();
	        	
	        	int nextTime=Math.min(copy.getUnit(0,0).firstTimeFree(), copy.getUnit(1,0).firstTimeFree());
	        	int time=state.getTime();
	        	if (time<nextTime){
		        	while (time<nextTime){
		        		copy.setTime(time);
				        ui.setGameState(copy);
				        ui.repaint();
			        	try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			        	time++;
		        	}
	        	} else {
	        		ui.setGameState(copy);
			        ui.repaint();
		        	try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        	}
	        }

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
		/*
		for (Integer u : moves.keySet()){
			//this u is a unit index!!
			//System.out.println("monitor: "+u +": "+monitor[u]+" DNA size: "+DNA.size());
			//System.out.println("mlength: "+monitor.length+" DNA length: "+DNA.size()+" u: "+u +" monitor[u]: "+monitor[u]);
			
			int scriptN = DNA.get(monitor[u]).get(u);
			monitor[u]+=1;
			if(monitor[u]>=DNA.size()-1){STOP=true;}
			
			Player scriptToUse = this.scripts.get(scriptN); //
			HashMap<Integer,List<UnitAction>> oneUnitMap = new HashMap<Integer,List<UnitAction>>();
			oneUnitMap.put(u, moves.get(u));
			scriptToUse.getMoves(state, oneUnitMap, scriptMoves);
		}*/
	}

	public int dnaEvalGroup(ArrayList<ArrayList<Integer>> DNA,int method,int thisplayerid){
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
	        	if(enemy.ID()==0){
	        		dnaMoves(DNA,monitor,moves_B, scriptMoves_B);
	        	}else{
	        		enemy.getMoves(state, moves_B, scriptMoves_B);
	        	}
	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);
	        }
	        
	        if(toMove.ID()==0){
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
        	return this.getState().eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2)._val;
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
        
	    int scoreval=this.getState().eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2)._val;
	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    return scoreval;
	}	
	
	public int dnaEvalGroup(ArrayList<ArrayList<Integer>> DNA,int method){
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
	        	if(enemy.ID()==0){
	        		dnaMoves(DNA,monitor,moves_B, scriptMoves_B);
	        	}else{
	        		enemy.getMoves(state, moves_B, scriptMoves_B);
	        	}
	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);
	        }
	        
	        if(toMove.ID()==0){
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
        	return this.getState().eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2)._val;
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
        
	    int scoreval=this.getState().eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2)._val;
	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    return scoreval;
	}	
	/*	
	public double dnaEval(ArrayList<Integer> DNA){
		//DNA as a sequence of scripts

		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;

        int size = DNA.size();
        int index = 0; //break out of game loop when index == size;
        
	    while (index<size&&!this.gameOver()){
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

	        if(toMove.ID()==0){
	        }else{
	        	state.generateMoves(moves_A, toMove.ID());
	        }
	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {
	        	if(enemy.ID()==0){
	        		Player scriptToUseB = this.scripts.get(DNA.get(index++));
	        		scriptToUseB.getMoves(state, moves_B, scriptMoves_B);
	        	}else{
	        		state.generateMoves(moves_B, enemy.ID());
	        		enemy.getMoves(state, moves_B, scriptMoves_B);
	        	}
	            state.makeMoves(scriptMoves_B);
	            //System.out.println("B moves: "+scriptMoves_B);
	        }
	        
	        if(toMove.ID()==0){
        		Player scriptToUseA = this.scripts.get(DNA.get(index++));
        		scriptToUseA.getMoves(state, moves_A, scriptMoves_A);
	        }else{
	        	toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	        }
	       
	        // make the moves
			state.makeMoves(scriptMoves_A); //let's not worry about its details for now
			//System.out.println("A moves: "+scriptMoves_A);
	        
	        state.finishedMoving();
	        rounds++;
	    }
	    
	    GameState finalState = this.getState();
	    StateEvalScore score = finalState.eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2);
	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    return score._val;
	}	

	//I'll need a forward model where instead of playing til the end, the gamestate
	//only forward step by step
	//use this to implement search-based alg
	public void forward(){

		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;

        if(!this.gameOver()&&rounds < moveLimit){
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove(); //int
	        toMove = _players[playerToMove]; //Player object
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
	        }
	        
	        // the tuple of moves he wishes to make
	        toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	        //IN FACT YOU DON'T HAVE TO USE GETMOVES() YOU CAN JUST ADD THINGS INTO SCRIPTMOVES_A
	        //AND THAT WILL ALLOW THE STATE TO BE FORWARDED
	    
	        // make the moves
			state.makeMoves(scriptMoves_A); //let's not worry about its details for now
			
			//to this line, the game state is forwarded.!!!!!!!!!!!!!!!!!!!!!!!!!
			
	        state.finishedMoving();
	        rounds++;
        }
	}
	*/

	
	/*
	public void forwardWithMoves(ArrayList<UnitAction> forwardMoves){
		//this function is used for state-based search
		//ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;

        if(!this.gameOver()&&rounds < moveLimit){
	        //scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove(); //int
	        toMove = _players[playerToMove]; //Player object
	        enemy = _players[GameState.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        //moves_A.clear();
	        moves_B.clear();

			//state.generateMoves(moves_A, toMove.ID());
	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {
	        	state.generateMoves(moves_B, enemy.ID());
				enemy.getMoves(state, moves_B, scriptMoves_B);
	            state.makeMoves(scriptMoves_B);
	        }
	        
	        // the tuple of moves he wishes to make
	        //toMove.getMoves(state, moves_A, scriptMoves_A); //THIS IS WHERE PLAYER.GETMOVES IS CALLED
	        //IN FACT YOU DON'T HAVE TO USE GETMOVES() YOU CAN JUST ADD THINGS INTO SCRIPTMOVES_A
	        //AND THAT WILL ALLOW THE STATE TO BE FORWARDED
	        
	        // make the moves
			state.makeMoves(forwardMoves); //let's not worry about its details for now
			
			//to this line, the game state is forwarded.!!!!!!!!!!!!!!!!!!!!!!!!!
			
	        state.finishedMoving();
	        rounds++;
        }
	}	
	*/

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