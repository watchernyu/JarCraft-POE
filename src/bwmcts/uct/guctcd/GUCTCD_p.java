/**
* This file is an extension to code based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.uct.guctcd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bwmcts.clustering.UPGMA;
import bwmcts.uct.UctNode;
import bwmcts.uct.UnitState;
import bwmcts.uct.UnitStateTypes;
import bwmcts.uct.NodeType;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_KiteDPS;
import bwmcts.sparcraft.players.Player_NOKAVBack;
import bwmcts.sparcraft.players.Player_NOKAVBackClose;
import bwmcts.sparcraft.players.Player_NOKAVBackFar;
import bwmcts.sparcraft.players.Player_NOKAVForward;
import bwmcts.sparcraft.players.Player_NOKAVForwardFar;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;
import bwmcts.uct.UCT;
import bwmcts.uct.UctConfig;
import bwmcts.uct.UctStats;
import bwmcts.uct.iuctcd.IuctNode;

public class GUCTCD_p extends UCT {

	private String NAME;
	private ClusteringConfig guctConfig;
	
	private List<List<Unit>> clustersA;
	private List<List<Unit>> clustersB;

	private List<List<Unit>> clusters;
	
	private List<Player> scripts;
	
	public GUCTCD_p(UctConfig uctConfig, ClusteringConfig guctConfig){
		super(uctConfig);
		this.guctConfig = guctConfig;
		
		// Add scripts
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(config.getMaxPlayerIndex()));
		scripts.add(new Player_KiteDPS(config.getMaxPlayerIndex()));
		NAME = "clusterUCT2";
	}
	
	public void init2scripts(){
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(config.getMaxPlayerIndex()));
		scripts.add(new Player_KiteDPS(config.getMaxPlayerIndex()));
		NAME = "clusterUCT2";
	}
	
	public void init6scripts(){
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVForward(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVBack(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVForwardFar(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVBackClose(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVBackFar(config.getMaxPlayerIndex()));
		NAME = "clusterUCT6";
	}

	public List<UnitAction> search(GameState state, long timeBudget){
		
		//System.out.println("Search called");
		
		if (config.getMaxPlayerIndex() == 0 && state.whoCanMove() == Players.Player_Two){
			return new ArrayList<UnitAction>(); 
		} else if (config.getMaxPlayerIndex() == 1 && state.whoCanMove() == Players.Player_One){
			return new ArrayList<UnitAction>(); 
		}
		
		long start = System.currentTimeMillis();
		long startNs = System.nanoTime();
		
		// Get clusters
		clustersA = guctConfig.getClusterAlg().getClusters(state.getAllUnit()[0], 6, guctConfig.getHpMulitplier());
		clustersB = guctConfig.getClusterAlg().getClusters(state.getAllUnit()[1], 6, guctConfig.getHpMulitplier());
		
		// Opponent clustering is empty!
		
		if(config.getMaxPlayerIndex() == 0)
			clusters = clustersA;
		else
			clusters = clustersB;
		
		//System.out.println("Nano time: " + (System.nanoTime() - startNs));
		
		UctNode root = new GuctNode(null, NodeType.ROOT, new ArrayList<UnitState>(), config.getMaxPlayerIndex(), "ROOT");
		root.setVisits(1);
		
		// Reset stats if new game
		if (state.getTime()==0)
			stats.reset();
		
		int t = 0;
		while(System.currentTimeMillis() <= start + timeBudget){
			traverse(root, state.clone());
			t++;
		}
		
		stats.getIterations().add(t);
		//System.out.println("GUCTCD: " + t);
		
		//UctNode best = mostVisitedChildOf(root);
		UctNode best = bestValueChildOf(root);
		//System.out.println(((GuctNode)best).getAbstractMove().size());
			
		if (config.isDebug())
			writeToFile(root.print(0), "tree.xml");
		
		if (best == null)
			return new ArrayList<UnitAction>();
		
		List<UnitAction> actions = best.getMove();
		
		return actions;
		
	}

	private float traverse(UctNode node, GameState state) {
		
		float score = 0;
		if (node.getVisits() == 0){
			if (node.getMove() == null)
				node.setMove(statesToActions(((GuctNode)node).getAbstractMove(), state));
			updateState(node, state, true);
			score = evaluate(state.clone());
		} else {
			int playerToMove = getPlayerToMove(node, state);
			updateState(node, state, false);
			if (state.isTerminal()){
				score = evaluate(state.clone());
			} else {
				if (expandable(node, playerToMove))
					generateChildren(node, state, playerToMove);
				score = traverse(selectNode(node), state);
			}
		}
		
		if (config.isLTD2()){
			node.setTotalScore(node.getTotalScore() + score);
		} else {
			if (score > 0)
				node.setTotalScore(node.getTotalScore() + 1);
			else if (score == 0)
				node.setTotalScore(node.getTotalScore() + 0.5f);
		}
		
		node.setVisits(node.getVisits() + 1);
		
		return score;
	}
	
	private boolean expandable(UctNode node, int playerToMove) {
		
		boolean us = playerToMove == config.getMaxPlayerIndex();
		if (!us && config.isNokModelling() && !node.getChildren().isEmpty())
			return false;
		
		if (node.getVisits() > config.getMaxChildren())
			return false;

		return true;
	}

	private void generateChildren(UctNode node, GameState state, int playerToMove) {

		List<UnitState> move = new ArrayList<UnitState>();
		
		HashMap<Integer, List<UnitAction>> map;
		if (node.getPossibleMoves() == null){

			map = new HashMap<Integer, List<UnitAction>>();
			try {
				state.generateMoves(map, playerToMove);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			node.setPossibleMoves(map);
			
		}
		
		List<List<Unit>> clus = null;
		if (playerToMove == 0)
			clus = clustersA;
		else
			clus = clustersB;
				
		String label = "";
		if (node.getChildren().size() < scripts.size() && playerToMove == config.getMaxPlayerIndex()) {
			move.addAll(getAllMove(UnitStateTypes.values()[node.getChildren().size()], clus));
			label = UnitStateTypes.values()[node.getChildren().size()].toString();
		} else if (playerToMove == config.getMaxPlayerIndex()){
			move = getRandomMove(playerToMove, clus); // Possible moves?
			label = "RANDOM";
		}
		
		if (move == null)
			return;
	
		if (uniqueMove(move, node)){
			GuctNode child = new GuctNode((GuctNode)node, getChildNodeType(node, state), move, playerToMove, label);
			node.getChildren().add(child);
		}
		
	}
	
	private List<List<Unit>> cleanClusters(GameState state, List<List<Unit>> clusters) {
		
		List<List<Unit>> readyClusters = new ArrayList<List<Unit>>();
		
		for(List<Unit> cluster : clusters){
			List<Unit> readyCluster = new ArrayList<Unit>();
			for(Unit unit : cluster){
				if (unit.firstTimeFree() == state.getTime())
					readyCluster.add(unit);
			}
			if (!readyCluster.isEmpty())
				readyClusters.add(readyCluster);
		}
		//System.out.println(clusters.size() + " : " + readyClusters.size());
		return readyClusters;
	}

	private List<UnitState> getAllMove(UnitStateTypes type, List<List<Unit>> clusters) {

		List<UnitState> states = new ArrayList<UnitState>();
		
		int i = 0;
		for(List<Unit> units : clusters){
			
			UnitState state = new UnitState(type, i, units.get(0).player());
			states.add(state);
			i++;
			
		}
		
		return states;
	}

	private boolean uniqueMove(List<UnitState> move, UctNode node) {

		if(node.getChildren().isEmpty())
			return true;
		
		for (UctNode child : node.getChildren()){
			boolean identical = true;
			if (child.getMove().size() != move.size()){
				identical = false;
			} else {
				for(int i = 0; i < move.size(); i++){
					if (!((GuctNode)child).getAbstractMove().get(i).equals(move.get(i))){
						identical = false;
						break;
					}
				}
			}
			if (identical){
				return false;
			}
		}
		
		return true;
		
	}

	private NodeType getChildNodeType(GuctNode parent, GameState prevState) {
		
		if(!prevState.bothCanMove()){
			
			return NodeType.SOLO;
			
		} else { 
			
			if (parent.getType() == NodeType.ROOT)
		
				return NodeType.FIRST;
			
			if (parent.getType() == NodeType.SOLO)
				
				return NodeType.FIRST;
			
			if (parent.getType() == NodeType.SECOND)
				
				return NodeType.FIRST;
			
			if (parent.getType() == NodeType.FIRST)
				
				return NodeType.SECOND;
		}
			
		return NodeType.DEFAULT;
	}

	private List<UnitState> getRandomMove(int playerToMove, List<List<Unit>> clusters) {
		
		List<UnitState> states = new ArrayList<UnitState>();
		
		int i = 0;
		for(List<Unit> units : clusters){
			
			// Random state
//			UnitStateTypes type = UnitStateTypes.ATTACK;
//			if (Math.random() >= 0.5f)
//				type = UnitStateTypes.KITE;
			
			// Get Random State from scripts
			int min = 0;
	        int max = scripts.size() - 1;
	        Random random = new Random();
	        int r = random.nextInt(max) % (max - min + 1) + min;
	        UnitStateTypes type = UnitStateTypes.values()[r];
			
			UnitState state = new UnitState(type, i, units.get(0).player());
			states.add(state);
			i++;
			
		}
		
		return states;
		
	}

	private List<UnitAction> statesToActions(List<UnitState> move, GameState state) {
		
		if (move == null || move.isEmpty() || move.get(0) == null)
			return new ArrayList<UnitAction>();
		
		int player = move.get(0).player;
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		try {
			state.generateMoves(map, player);
		} catch (Exception e) {e.printStackTrace();}
		
		List<List<Unit>> clus = null;
		if (player == 0)
			clus = clustersA;
		else
			clus = clustersB;
		
		ArrayList<ArrayList<Integer>> typeUnits = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < scripts.size(); i++) {
			typeUnits.add(new ArrayList<Integer>());
		}
		
		// Divide units into two groups

		for(UnitState unitState : move){
			
			// Add units in cluster
			for(Unit u : clus.get(unitState.unit)){
				
				if (u.isAlive() && (u.canAttackNow() || u.canMoveNow())){

					for (int i = 0; i < scripts.size(); i++) {
						if (unitState.type == UnitStateTypes.values()[i] && u.isAlive()) {
							typeUnits.get(i).add(u.getId());
							break;
						}
					}
					
				}
				
			}
			
		}
		
		List<UnitAction> allActions = new ArrayList<UnitAction>();
		ArrayList<HashMap<Integer, List<UnitAction>>> typeMap = new ArrayList<HashMap<Integer, List<UnitAction>>>();
		for (int i = 0; i < scripts.size(); i++) {
			typeMap.add(new HashMap<Integer, List<UnitAction>>());
		}
		
		// Loop through the map
		for(Integer i : map.keySet()){
			int u = map.get(i).get(0)._unit;
			int unitId = state.getUnit(player, u).getId();
			for (int j = 0; j < scripts.size(); j++) {
				if (typeUnits.get(j).contains(unitId)) {
					typeMap.get(j).put(i,  map.get(i));
				}
			}
		}
		
		// Add actions
		for (int i = 0; i < scripts.size(); i++) {
			List<UnitAction> act = new ArrayList<UnitAction>();
			scripts.get(i).getMoves(state, typeMap.get(i), act);
			allActions.addAll(act);
		}
		return allActions;
	}

	public List<List<Unit>> getClusters() {
		return clusters;
	}
	
	public String toString(){
		return NAME+"_GUCT_"+this.guctConfig.toString()+ "\t"+this.config.toString();
	}
	
}