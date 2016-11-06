package bwmcts.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javabot.BWAPIEventListener;
import javabot.JNIBWAPI;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import bwmcts.clustering.DynamicKMeans;
import bwmcts.clustering.KMeans;
import bwmcts.clustering.UPGMA;
import bwmcts.combat.RandomScriptLogic;
import bwmcts.clustering.*;
import bwmcts.combat.UctLogic;
import bwmcts.uct.UctConfig;
import bwmcts.uct.UctStats;
import bwmcts.uct.flatguctcd.FlatGUCTCD;
import bwmcts.uct.guctcd.ClusteringConfig;
import bwmcts.uct.guctcd.GUCTCD;
import bwmcts.uct.guctcd.GUCTCD_p;
import bwmcts.uct.iuctcd.IUCTCD;
import bwmcts.uct.iuctcd.IUCTCD_p;
import bwmcts.uct.portfolio.UCTPortfolio_2;
//import bwmcts.uct.portfolio.UCTPortfolio_1;
//import bwmcts.uct.portfolio.UCTPortfolio_2;
import bwmcts.uct.rguctcd.RGUCTCD;
import bwmcts.uct.uctcd.UCTCD;
import bwmcts.sparcraft.*;
import bwmcts.sparcraft.players.*;

public class TestTOTALLYSymmetric implements BWAPIEventListener  {
	
	private static boolean graphics = false;
	private static boolean SHOWALLRESULTS = true;
	private static boolean LOGALLRESULTS = false;
	BufferedWriter OUT;
	JNIBWAPI bwapi;
	StringBuffer BUF;
	
	int[] numOfUnitsInTest =  new int[]{25};
	//int[] numOfUnitsInTest =  new int[]{96};
	int totalRuns = 50;
	
	public static void main(String[] args) throws Exception{
		System.out.println("Create TC instance");
		TestTOTALLYSymmetric tc=new TestTOTALLYSymmetric();
		//tc.bwapi=new JNIBWAPI(tc);
		//tc.bwapi.start();
		
		tc.bwapi = new JNIBWAPI_LOAD(tc);
		tc.bwapi.loadTypeData();
		
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(tc.bwapi);
		UnitProperties.Init(tc.bwapi);
		//graphics = true;
		
		Constants.Max_Units = 300;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		GUCTCD guctcdA = new GUCTCD(new UctConfig(0), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		GUCTCD guctcdB = new GUCTCD(new UctConfig(1), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		
		RGUCTCD rguctcdA = new RGUCTCD(new UctConfig(0), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		RGUCTCD rguctcdB = new RGUCTCD(new UctConfig(1), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		FlatGUCTCD flatGuctcdA = new FlatGUCTCD(new UctConfig(0, true), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		FlatGUCTCD flatGuctcdB = new FlatGUCTCD(new UctConfig(1, true), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		GUCTCD_p guctcd_p1 = new GUCTCD_p(new UctConfig(0), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		GUCTCD_p guctcd_p2 = new GUCTCD_p(new UctConfig(1), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		
////////IN BETWEEN SET EXPERIMENTS///////////////////////////////////////////////////////////
		boolean EXPERIMENT = true;
		String student = "cw";//cw,pc,ydl
		long startt = System.currentTimeMillis();
		if(EXPERIMENT){

			//tc.pgs_vs_suct();
			//tc.giveTime(startt);
			//startt = System.currentTimeMillis();
			//tc.pgs_vs_cuct();
			//tc.giveTime(startt);
			startt = System.currentTimeMillis();
			tc.vs_ac();
			tc.giveTime(startt);
			
			//startt = System.currentTimeMillis();
			
			/*
			tc.evo_vs_pgs();
			tc.giveTime(startt);
			startt = System.currentTimeMillis();
			tc.evo_vs_suct();
			tc.giveTime(startt);
			startt = System.currentTimeMillis();
			tc.evo_vs_cuct();
			tc.giveTime(startt);*/
			//startt = System.currentTimeMillis();
			
			System.out.println("All experiments finished!");
			//tc.PGSvPGSEN(tc);
			return;
		} //use to skip all the code follows
////////IN BETWEEN SET EXPERIMENTS///////////////////////////////////////////////////////////

		Player p1;
		p1 = new Player_Evolution(0);
		//p1 = new Player_PGS(0);
		//p1 = new Player_NoOverKillAttackValue(0);
		//p1 = new UctLogic(tc.bwapi, guctcdA, 40);
		//p1 = new UctLogic(tc.bwapi, guctcd_p1, 20);
		//Player p1 = new Player_ClusteredUnitStateToUnitAction(0);
		//Player p1 = new UctLogic(tc.bwapi, new IUCTCD(new UctConfig(0)),40);
		//ayer p2 = new Player_Random(1);
		//Player p2 = new Player_Nothing(1);
		Player p2;
		//p2 = new Player_PGS(1);
		//p2 = new Player_NoOverKillAttackValue(1);
		//p2 = new UctLogic(tc.bwapi, new UCTCD(new UctConfig(1)),40);
		//Player p2 = new RandomScriptLogic(1);
		p2 = new UctLogic(tc.bwapi, guctcdB, 20);
		//p2 = new UctLogic(tc.bwapi, rguctcdB, 40);
		// p2 = new UctLogic(tc.bwapi, new UCTPortfolio_2(new UctConfig(1)), 40);
		//p2 = new UctLogic(tc.bwapi, guctcd_p2, 20);
		

		//tc.newTest(p1, p2, 100, numOfUnitsInTest);
		
		//tc.newTest(p1, p2, tc.totalRuns,new int[]{96},TestSetting.D);
		tc.newTest(p1, p2, tc.totalRuns,new int[]{96},TestSetting.DZ);
		
		//tc.newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.L);
		//tc.newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DM);
		//tc.newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.ML);
		
		//D,L,DZ,DM,ML;
		
		//tc.moreTest2(tc);
	}
	
	public void giveTime(long start){
		System.out.println("=================================");
		long timeUsed = (System.currentTimeMillis()-start)/60000; //will print out time used in min
		System.out.println("One experiment ended. Minutes: "+timeUsed);
		System.out.println("=================================");
	}
	
	
	////////////////////////////////////////////////////////
	public void vs_ac(){
		int N = 6;
		int M = 3;
		int[][] P = new int[N][M]; //4 genomes, each genome has 3 genes
		float[] Fit = new float[N];
		Random ran = new Random();
		for(int i=0;i<N;i++){
			for(int j=0;j<M;j++){
				P[i][j] = ran.nextInt(M);
			}
		}
		
		for(int i=0;i<N;i++){
			Fit[i] = 0.0f;
		}
		
		boolean firstRound = true;
		
		for(int e=0;e<5;e++){//evolve 7 times
			for(int i=0;i<N;i++){
				int[] g = P[i];
				Player p1= new Player_EvoTESTONLY(0);
				Player_EvoTESTONLY pe = (Player_EvoTESTONLY) p1;
				pe.initSingleOffline(g[0],g[1],g[2]);
				Player p2= new Player_AttackClosest(1);
				float fitness = 0;
				try{
					fitness = offTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.D);
					}catch(Exception ee){
						ee.printStackTrace();
					}
				Fit[i] = fitness;
			}
			
			for(int i=0;i<N;i++){
				String genomePrint = ""+P[i][0]+","+P[i][1]+","+P[i][2];
				System.out.println("Player: "+i+" genome: "+ genomePrint+" fitness: "+Fit[i]);
			}
			
			//here write selection and mutation code.
			float max = -9999;
			float secondMax = -9999;
			int bestI = 0;
			int secondI = 0;
			for(int i=0;i<N;i++){
				if(Fit[i]>max){
					secondMax = max;
					max = Fit[i];
					secondI = bestI;
					bestI = i;
					//System.out.println("1,2: ::::"+max+" "+secondMax);
				}else if(Fit[i]<max&&Fit[i]>secondMax){
					secondMax = Fit[i];
					secondI = i;
				}
			}
			
			int[] temp1 = new int[M];
			int[] temp2 = new int[M];
			
			//System.out.println("bestI "+bestI);
			//System.out.println("best2I "+secondI);
			//System.out.println(max);
			//System.out.println(secondMax);
			
			for(int j =0;j<M;j++){//bestOne
				temp1[j] = P[bestI][j];
			}
			for(int j =0;j<M;j++){//secondOne
				temp2[j] = P[secondI][j];
			}
			
			for(int i=0;i<N;i++){
				for(int j=0;j<M;j++){
					P[i][j] = ran.nextInt(M);
				}
			}
			
			for(int j =0;j<M;j++){//bestOne
				P[0][j] = temp1[j];
			}
			for(int j =0;j<M;j++){//secondOne
				P[1][j] = temp2[j];
			}
			
			float totalfitness = 0;
			for(int i=0;i<N;i++){
				totalfitness += Fit[i];
			}
			
			if(firstRound){
				firstRound = false;
				System.out.println("==================##########================");
				System.out.println("First Round average: "+(totalfitness/N));
				System.out.println("==================##########================");
			}
			
			for(int i=0;i<N;i++){
				Fit[i] = 0.0f;
			}
			
			Fit[0] = max;
			Fit[1] = secondMax;
			
			for(int j=0;j<M;j++){
				System.out.print("Best: "+P[0][j]+" ");
			}
			System.out.println();
			
			for(int j=0;j<M;j++){
				System.out.print("Second: "+P[1][j]+" ");
			}
			System.out.println();
			
			for(int i=0;i<2;i++){
				System.out.println("Fit: "+Fit[i]);
			}
			
			//=2
			//=3
			for(int j=0;j<M;j++){
				int rr = ran.nextInt(1);
				P[2][j] = rr == 0? P[0][j]: P[1][j];
			}
			
			for(int j=0;j<M;j++){
				int rr = ran.nextInt(1);
				P[3][j] = rr == 0? P[0][j]: P[1][j];
			}
			
			//=4
			//=5
			for(int j=0;j<M;j++){
				int rr = ran.nextInt(2);
				if(rr==1){
					P[4][j] = ran.nextInt(3);
				}else{
					P[4][j] = P[0][j];
				}
			}
			
			for(int j=0;j<M;j++){
				int rr = ran.nextInt(2);
				if(rr==1){
					P[5][j] = ran.nextInt(3);
				}else{
					P[5][j] = P[1][j];
				}
			}
			//write evolution here.
		}
		System.out.println("@@@@@@@@@@@@@@ Best genome: "+P[0][0]+" "+P[0][1]+" "+P[0][2]+" ");
		System.out.println("@@@@@@@@@@@@@@ Best fitness: "+Fit[0]);
		System.out.println("End");
	}
	
	public enum TestSetting{
		D,DZ; //dragoon, zergling, dragoon+zealot, dragoon+marine, marine+ling
	}
	
	private float offTest(Player p1, Player p2, int runs, int[] n, TestSetting setting) throws IOException {
		String player0=p1.toString();
		if (player0.indexOf(" ")>0){
			player0=player0.substring(0, player0.indexOf(" "));
		}
		String player1=p2.toString();
		if (player1.indexOf(" ")>0){
			player1=player1.substring(0, player1.indexOf(" "));
		}

		float totalScore = 0;
		int bigGameCount = 0;
		for(Integer i : n){
			try {
				System.out.println("---NEW GAME--- units: " + i+" rounds: "+runs);
				ensureUnitNumSetting(p1,i);
				ensureUnitNumSetting(p2,i);
				
				float result=0;
				switch(setting){
				case D:
					result = DOffGames(p1, p2, (int)i, runs);//this result is a win rate!!!
					totalScore+=result;
					bigGameCount++;
					break;
				default:
					break;
				}
				
				//buf.append("AI GAME TEST RESULT: " + result+"\r\n");
				//System.out.println("AI GAME TEST RESULT: " + result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return totalScore/bigGameCount;
	}
	
	private void newTest(Player p1, Player p2, int runs, int[] n, TestSetting setting) throws IOException {
		this.BUF=new StringBuffer();
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		
		try{
			String player0=p1.toString();
			if (player0.indexOf(" ")>0){
				player0=player0.substring(0, player0.indexOf(" "));
			}
			String player1=p2.toString();
			if (player1.indexOf(" ")>0){
				player1=player1.substring(0, player1.indexOf(" "));
			}
		File f = new File(player0+"_vs_"+player1+"_"+dateFormat.format(cal.getTime())+".txt");
        this.OUT = new BufferedWriter(new FileWriter(f,true));}catch(Exception e)
		{
        	e.printStackTrace();
        	System.out.println("Writing to file error, terminate experiment.");
        	return;
        }	

		this.BUF.append("Player0: "+p1.toString()+"\r\n");
		this.BUF.append("Player1: "+p2.toString()+"\r\n");
		
		for(Integer i : n){
			try {
				System.out.println("---NEW GAME--- units: " + i+" rounds: "+runs);
				this.BUF.append("\r\n########## NEW GAME ########## units: "+i+" runs: "+runs+"\r\n");
				ensureUnitNumSetting(p1,i);
				ensureUnitNumSetting(p2,i);
				
				float result=0;
				switch(setting){
				case DZ:
					result = DZGames(p1, p2, (int)i, runs);
					break;
				case D:
					result = DGames(p1, p2, (int)i, runs);
					break;
				default:
					break;
				}
				
				//buf.append("AI GAME TEST RESULT: " + result+"\r\n");
				//System.out.println("AI GAME TEST RESULT: " + result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try{
		OUT.close();}catch(Exception e){e.printStackTrace();
		System.out.println("Close file failed");}
	}
	
	private void ensureUnitNumSetting(Player p,int i){
		if(p instanceof Player_Watcher6){
			Player_Watcher6 pw = (Player_Watcher6) p;
			pw.setNumUnit(i);
		}
		if(p instanceof Player_Watcher7){
			Player_Watcher7 pw = (Player_Watcher7) p;
			pw.setNumUnit(i);
		}
		if(p instanceof Player_Evolution){
			Player_Evolution pw = (Player_Evolution) p;
			pw.setNumUnit(i);
		}
		if(p instanceof Player_pg){
			Player_pg pw = (Player_pg) p;
			pw.setNumUnit(i);
		}
		if(p instanceof Player_PGS){
			Player_PGS pw = (Player_PGS) p;
			pw.setNumUnit(i);
		}
		if(p instanceof Player_EvoTESTONLY){
			Player_EvoTESTONLY pw = (Player_EvoTESTONLY) p;
			pw.setNumUnit(i);
		}
	}

	int testGame(Player p1, Player p2, HashMap<UnitTypes, Integer> unitsA, HashMap<UnitTypes, Integer> unitsB) throws Exception
	{
		GameState initialState = gameState(unitsA, unitsB);
		//LOCATOR
		shufflePositionsSymmetric(initialState, 100);
		//shufflePositions(initialState, 100); //this has bug
		
		p1.setID(0);
		p2.setID(1);
	    
	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 20000;

	    // construct the game
	    Game g=new Game(initialState, p1, p2, moveLimit, graphics);

	    // play the game
	    
	    for(int i = 0; i < 10000000; i++){
	    	if(Math.random()>100)
	    		break;
	    }
	    
	    g.play();
	    //System.out.println("a game just ended");

	    // you can access the resulting game state after g has been played via getState
	    GameState finalState = g.getState();
	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
	    StateEvalScore score = finalState.eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2);
	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    //System.out.println("Last game score: "+score._val);
	    if(SHOWALLRESULTS){
		    System.out.println("P1 score: "+score._val);
	    }
	    return score._val;
	}
	
	private void shufflePositionsSymmetric(GameState state, int amount) {
		Random ran = new Random();
		int maxnumran = 200;
		int xran[] = new int[maxnumran];
		int yran[] = new int[maxnumran];
		for(int i=0;i<maxnumran;i++){
			xran[i] = ran.nextInt(4);
			yran[i] = ran.nextInt(4);
		}
		
		int u = 0;
		for(Unit unit : state.getAllUnit()[0]){
			if (unit == null || unit.pos() == null)
				continue;
			int x = unit.pos().getX();
			int y = unit.pos().getY();
			int newX = x - xran[u];
			int newY = y - yran[u];
			u++;
			unit.pos().setX(newX);
			unit.pos().setY(newY);
		}
		
		u=0;
		for(Unit unit : state.getAllUnit()[1]){
			if (unit == null || unit.pos() == null)
				continue;
			
			int x = unit.pos().getX();
			int y = unit.pos().getY();
			int newX = x + xran[u];
			int newY = y + yran[u];
			u++;
			unit.pos().setX(newX);
			unit.pos().setY(newY);
		}
	}

	private GameState gameState(HashMap<UnitTypes, Integer> unitsA,
			HashMap<UnitTypes, Integer> unitsB) throws Exception {
		
		// GameState only has a default constructor, you must add units to it manually
	    GameState state=new GameState();
	    state.setMap(new Map(25, 20));
	    
	    int startXA = 275;
	    //int startXA = 450;
	    int startXB = 500;
	    int space = 28;
	    int startAY = 50;
	    int startBY = 50;
	    int unitsPerLine = 16;
	    
	    for(UnitTypes type : unitsA.keySet()){
	    	try {
	    	    state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_One.ordinal(),new Position(startXA, startAY + space));
	    	} catch (Exception e){}
	    	
 	    	for(int i = 1; i < unitsA.get(type); i++){
 	    		int x = startXA - (i/unitsPerLine) * space;
 	    		int y = startAY + space*(i%unitsPerLine) + space;
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_One.ordinal(), new Position(x, y));
 	    		} catch (Exception e){
 		 	    	//e.printStackTrace();
 		 	    }
 	    	}
	    	startXA -= space * 2;
	    }
	    for(UnitTypes type : unitsB.keySet()){
	    	try {
	    	    state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(),new Position(startXB, startBY + space));
	    	} catch (Exception e){}
	    	
 	    	for(int i = 1; i < unitsB.get(type); i++){
 	    		int x = startXB + (i/unitsPerLine) * space;
 	    		int y = startBY + space*(i%unitsPerLine) + space;
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(), new Position(x, y));
 	    		} catch (Exception e){
		 	    	//e.printStackTrace();
		 	    }
 	    	}
	    	startXB += space * 2;
	    }
	    return state;
	}
	
	private double deviation(List<Double> times) {
		double average = average(times);
		double sum = 0;
		for(Double d : times){
			sum += (d - average) * (d - average);
		}
		return Math.sqrt(sum/times.size());
	}

	private double average(List<Double> times) {
		double sum = 0;
		for(Double d : times){
			sum+=d;
		}
		return sum/((double)times.size());
	}
	

	float DZGames(Player p1, Player p2, int n, int games) throws Exception{
		String CombatName = "Dragoon+Zealot";
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Protoss_Dragoon, n/2);
		unitsA.put(UnitTypes.Protoss_Zealot, n/2);
		/////////////////////////////
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Protoss_Dragoon, n/2);
		unitsB.put(UnitTypes.Protoss_Zealot, n/2);
		
		Constants.Max_Units = n*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		this.BUF.append(CombatName+": "+ n+ "\r\n");
		List<Double> results = new ArrayList<Double>();
		int wins = 0;
		
		for(int i = 1; i <= games; i++){
			long st = System.currentTimeMillis();
			double result = testGame(p1, p2, unitsA, unitsB); /////////////!!!!!!!!!!!!!!!!!!!
			results.add(result);
			if (result>0)
				{wins++;}			
			//System.out.println("Score average: " + average(results) + "\tDeviation: " + deviation(results));
			System.out.println("Games: "+i+" Win average: " + ((double)wins)/((double)i)+" Time: "+(System.currentTimeMillis()-st));

			this.BUF.append("Game: "+i+" Win average: " + ((double)wins)/((double)i)+" score: "+result+"\r\n");
		}
		
		// Calc deviation and average
		System.out.println("--------------- Score average: " + average(results) + "\tDeviation: " + deviation(results));
		this.BUF.append("--------------- Score average: " + average(results) + "\tDeviation: " + deviation(results)+"\r\n");
		System.out.println("--------------- Win average: " + ((double)wins)/((double)games));
		this.BUF.append("--------------- Win average: " + ((double)wins)/((double)games)+"\r\n");
		
		OUT.append(BUF.toString());
		BUF = new StringBuffer();
		
		return (float)wins / (float)games;
	}
	
	
	float DGames(Player p1, Player p2, int n, int games) throws Exception{
		String CombatName = "All Dragoon";
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Protoss_Dragoon, n);
		//unitsA.put(UnitTypes.Protoss_Zealot, n/2);
		/////////////////////////////
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Protoss_Dragoon, n);
		//unitsB.put(UnitTypes.Protoss_Zealot, n/2);
		
		Constants.Max_Units = n*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		this.BUF.append(CombatName+": "+ n+ "\r\n");
		List<Double> results = new ArrayList<Double>();
		int wins = 0;
		
		for(int i = 1; i <= games; i++){
			long st = System.currentTimeMillis();
			double result = testGame(p1, p2, unitsA, unitsB); /////////////!!!!!!!!!!!!!!!!!!!
			results.add(result);
			if (result>0)
				{wins++;}			
			//System.out.println("Score average: " + average(results) + "\tDeviation: " + deviation(results));
			System.out.println("Games: "+i+" Win average: " + ((double)wins)/((double)i)+" Time: "+(System.currentTimeMillis()-st));

			this.BUF.append("Game: "+i+" Win average: " + ((double)wins)/((double)i)+" score: "+result+"\r\n");
		}
		
		// Calc deviation and average
		System.out.println("--------------- Score average: " + average(results) + "\tDeviation: " + deviation(results));
		this.BUF.append("--------------- Score average: " + average(results) + "\tDeviation: " + deviation(results)+"\r\n");
		System.out.println("--------------- Win average: " + ((double)wins)/((double)games));
		this.BUF.append("--------------- Win average: " + ((double)wins)/((double)games)+"\r\n");
		
		OUT.append(BUF.toString());
		BUF = new StringBuffer();
		
		return (float)wins / (float)games;
	}
	
	float DOffGames(Player p1, Player p2, int n, int games) throws Exception{
		String CombatName = "All Dragoon";
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Protoss_Dragoon, n-1);
		//unitsA.put(UnitTypes.Protoss_Zealot, n/2);
		/////////////////////////////
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Protoss_Dragoon, n);
		//unitsB.put(UnitTypes.Protoss_Zealot, n/2);
		
		Constants.Max_Units = n*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		List<Double> results = new ArrayList<Double>();
		int wins = 0;
		float totalScore = 0;
		
		for(int i = 1; i <= games; i++){
			long st = System.currentTimeMillis();
			double result = testGame(p1, p2, unitsA, unitsB); /////////////!!!!!!!!!!!!!!!!!!!
			results.add(result);
			totalScore+=result;
			if (result>0)
				{wins++;}			
			//System.out.println("Score average: " + average(results) + "\tDeviation: " + deviation(results));
			System.out.println("Games: "+i+" Win average: " + ((double)wins)/((double)i)+" Time: "+(System.currentTimeMillis()-st));
		}
		
		// Calc deviation and average
		System.out.println("--------------- Score average: " + average(results) + "\tDeviation: " + deviation(results));
		System.out.println("--------------- Win average: " + ((double)wins)/((double)games));
		
		return totalScore / (float)games;
	}
	
	@Override
	public void connected() {
		/*
		System.out.println("BWAPI connected");
		bwapi.loadTypeData();
		try {
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);		
		
		graphics = true;
		
		Constants.Max_Units = 200;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		//Player p1 = new Player_Kite(0);
		
		//Player p1 = new UctcdLogic(bwapi, new UCTCD(1.6, 20, 1, 0, 500, false), 200);
		GUCTCD guctcdA = new GUCTCD(new UctConfig(0), 
									new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));

		GUCTCD guctcdB = new GUCTCD(new UctConfig(1), 
									new ClusteringConfig(1, 6, new UPGMA()));

		Player p1 = new UctLogic(bwapi, new UCTCD(new UctConfig(0)),40);
		//Player p1 = new UctcdLogic(bwapi, new OLDIUCTCD(1.6, 20, 1, 0, 50000, false),40);
		//Player p1 = new UctLogic(bwapi, guctcdA, 40);
		//Player p1 = new Player_NoOverKillAttackValue(0);
		
		//Player p2 = new UctcdLogic(bwapi, new IUCTCD(new UctConfig(1), new UctStats()),40);
		Player p2 = new Player_NoOverKillAttackValue(1);
		
		
		//oneTypeTest(p1, p2, UnitTypes.Terran_Marine, 25);

		//p2.setID(1);
		//oneTypeTest(p1, p2, UnitTypes.Zerg_Zergling, 10);

		//TODO: Write to file
		
		//PortfolioTest(p1, p2);
		
		//realisticTest(p1, p2, 20);
		
		//dragoonZTest(p1, p2, 20, new int[]{8,16,32,50,75,100});

		dragoonZTest(p1, p2, 2, new int[]{50,75});
		
		//upgmaTest(p1, p2, 100, 25);
		
		//upgmaScenario(p1, p2);
		
		simulatorTest(p1, p2, 1, 250, 10, 100);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	
	@Override
	public void gameStarted() {}

	@Override
	public void gameUpdate() {}

	@Override
	public void gameEnded() {}

	@Override
	public void keyPressed(int keyCode) {}

	@Override
	public void matchEnded(boolean winner) {}

	@Override
	public void playerLeft(int id) {}

	@Override
	public void nukeDetect(int x, int y) {}

	@Override
	public void nukeDetect() {}

	@Override
	public void unitDiscover(int unitID) {}

	@Override
	public void unitEvade(int unitID) {}

	@Override
	public void unitShow(int unitID) {}

	@Override
	public void unitHide(int unitID) {}

	@Override
	public void unitCreate(int unitID) {}

	@Override
	public void unitDestroy(int unitID) {}

	@Override
	public void unitMorph(int unitID) {}
}