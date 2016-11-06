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

public class TestSymmetric4_POEwithCross implements BWAPIEventListener  {
	
	private static boolean graphics = true;
	private static boolean SHOWALLRESULTS = true;
	private static boolean LOGALLRESULTS = false;
	BufferedWriter OUT;
	JNIBWAPI bwapi;
	StringBuffer BUF;

	//int[] numOfUnitsInTest =  new int[]{4,8,16,32,64,96};
	int[] numOfUnitsInTest =  new int[]{32,8,16,32,64,96};

	int totalRuns = 50;
	
	public static void main(String[] args) throws Exception{
		System.out.println("Create TC instance");
		TestSymmetric4_POEwithCross tc=new TestSymmetric4_POEwithCross();
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

			startt = System.currentTimeMillis();
			tc.poeCross();
			tc.giveTime(startt);

			System.out.println("All experiments finished!");
			//tc.PGSvPGSEN(tc);
			return;
		} //use to skip all the code follows
////////IN BETWEEN SET EXPERIMENTS///////////////////////////////////////////////////////////
		
	}
	
	public void giveTime(long start){
		System.out.println("=================================");
		long timeUsed = (System.currentTimeMillis()-start)/60000; //will print out time used in min
		System.out.println("One experiment ended. Minutes: "+timeUsed);
		System.out.println("=================================");
	}
	
	////////////////////////////////////////////////////////
	
	public void poe_cross_VS_UCT_cluster(){
		Player p1= new Player_POE_final_with_cross(0);
		Player_POE_final_with_cross pe1 = (Player_POE_final_with_cross) p1;
		pe1.init6scripts();
		
		//Player p2= new Player_NoOverKillAttackValue(1);
		
		GUCTCD guctcdB = new GUCTCD(new UctConfig(1), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		Player p2 = new UctLogic(bwapi, guctcdB, 20);
		
		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.Test);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public void poeCross(){
		Player p1= new Player_POE_final_with_cross(0);
		Player_POE_final_with_cross pe1 = (Player_POE_final_with_cross) p1;
		pe1.init2scripts();
		
		//Player p2= new Player_NoOverKillAttackValue(1);
		
		Player p2= new Player_POE_final_no_cross(1);
		Player_POE_final_no_cross pe2 = (Player_POE_final_no_cross) p2;
		pe2.init2scripts();
		
		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.Test);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public void evo_vs_nokav_testonly(){
		Player p1= new Player_Evolution(0);
		Player_Evolution pe = (Player_Evolution) p1;
		pe.init6scripts();
		
		Player p2= new Player_NoOverKillAttackValue(1);

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	
	public void evo_vs_nokav(){
		Player p1= new Player_Evolution(0);
		Player_Evolution pe = (Player_Evolution) p1;
		pe.init2scripts();
		
		Player p2= new Player_NoOverKillAttackValue(1);

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public void pgs_vs_suct(){
		Player p1= new Player_PGS(0);
		Player_PGS ppgs = (Player_PGS) p1;
		ppgs.init2scripts();

		Player p2= new UctLogic(bwapi, new IUCTCD(new UctConfig(1)),20);

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public void pgs_vs_cuct(){
		Player p1= new Player_PGS(0);
		Player_PGS ppgs = (Player_PGS) p1;
		ppgs.init2scripts();

		GUCTCD guctcdB = new GUCTCD(new UctConfig(1), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		Player p2 = new UctLogic(bwapi, guctcdB, 20);

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}

	public void evo_vs_pgs(){
		Player p1= new Player_Evolution(0);
		Player_Evolution pe = (Player_Evolution) p1;
		pe.init2scripts();
		
		Player p2= new Player_PGS(1);
		Player_PGS ppgs = (Player_PGS) p2;
		ppgs.init2scripts();

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public void evo_vs_suct(){
		Player p1= new Player_Evolution(0);
		Player_Evolution pe = (Player_Evolution) p1;
		pe.init2scripts();
		
		Player p2= new UctLogic(bwapi, new IUCTCD(new UctConfig(1)),20);

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public void evo_vs_cuct(){
		Player p1= new Player_Evolution(0);
		Player_Evolution pe = (Player_Evolution) p1;
		pe.init2scripts();
		
		GUCTCD guctcdB = new GUCTCD(new UctConfig(1), 
				new ClusteringConfig(1, 6, new DynamicKMeans(30.0)));
		Player p2 = new UctLogic(bwapi, guctcdB, 20);

		try{
		newTest(p1, p2, totalRuns,numOfUnitsInTest,TestSetting.DZ);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of Experiment");
	}
	
	public enum TestSetting{
		D,DZ,Test; //dragoon, zergling, dragoon+zealot, dragoon+marine, marine+ling
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
				case Test:
					result = OnlyTestGames(p1, p2, (int)i, runs);
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
		if(p instanceof Player_POE_final_no_cross){
			Player_POE_final_no_cross pw = (Player_POE_final_no_cross) p;
			pw.setNumUnit(i);
		}
		if(p instanceof Player_POE_final_with_cross){
			Player_POE_final_with_cross pw = (Player_POE_final_with_cross) p;
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
			xran[i] = ran.nextInt(20);
			yran[i] = ran.nextInt(20);
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
	
	float OnlyTestGames(Player p1, Player p2, int n, int games) throws Exception{
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