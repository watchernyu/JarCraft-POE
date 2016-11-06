package bwmcts.uct.iuctcd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import bwmcts.uct.UnitState;
import bwmcts.uct.NodeType;
import bwmcts.uct.UnitStateTypes;
import bwmcts.uct.iuctcd.IuctNode;
import bwmcts.uct.rguctcd.RGuctNode;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_KiteDPS;
import bwmcts.sparcraft.players.Player_NOKAVBack;
import bwmcts.sparcraft.players.Player_NOKAVBackClose;
import bwmcts.sparcraft.players.Player_NOKAVBackFar;
import bwmcts.sparcraft.players.Player_NOKAVForward;
import bwmcts.sparcraft.players.Player_NOKAVForwardFar;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;
import bwmcts.sparcraft.players.Player_Pass;
import bwmcts.uct.UCT;
import bwmcts.uct.UctConfig;
import bwmcts.uct.UctNode;
import bwmcts.uct.UctStats;

public class IUCTCD_p extends UCT {
	
	private ArrayList<Player> scripts;
	private String NAME;
	
	public IUCTCD_p(UctConfig config) {
		super(config);
		
		// Add scripts
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(config.getMaxPlayerIndex()));
		scripts.add(new Player_KiteDPS(config.getMaxPlayerIndex()));
		NAME = "scriptUCT2";
	}
	
	
	public void init2scripts(){
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(config.getMaxPlayerIndex()));
		scripts.add(new Player_KiteDPS(config.getMaxPlayerIndex()));
		NAME = "scriptUCT2";
	}
	
	public void init6scripts(){
		scripts = new ArrayList<Player>();
		scripts.add(new Player_NoOverKillAttackValue(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVForward(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVBack(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVForwardFar(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVBackClose(config.getMaxPlayerIndex()));
		scripts.add(new Player_NOKAVBackFar(config.getMaxPlayerIndex()));
		NAME = "scriptUCT6";
	}
	
	@Override
	public List<UnitAction> search(GameState state, long timeBudget){
		
		if (config.getMaxPlayerIndex() == 0 && state.whoCanMove() == Players.Player_Two){
			return new ArrayList<UnitAction>(); 
		} else if (config.getMaxPlayerIndex() == 1 && state.whoCanMove() == Players.Player_One){
			return new ArrayList<UnitAction>(); 
		}
		
		Date start = new Date();
		
		UctNode root = new IuctNode(null, NodeType.ROOT, new ArrayList<UnitState>(), config.getMaxPlayerIndex(), "ROOT");
		root.setVisits(1);
		
		// Reset stats if new game
		if (state.getTime()==0)
			stats.reset();
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			t++;
			
		}
		
		stats.getIterations().add(t);
		//System.out.println("IUCTCD: " + t);
		
		//UctNode best = mostWinningChildOf(root);
		//UctNode best = mostVisitedChildOf(root);
		UctNode best = bestValueChildOf(root);
		//System.out.println(((IuctNode)best).getAbstractMove().size());
		
		if (best == null){
			System.out.println("IUCTCD: NULL MOVE!");
			return new ArrayList<UnitAction>();
		}
		
		if (config.isDebug())
			writeToFile(root.print(0), "tree.xml");
		
		List<UnitAction> actions = statesToActions(((IuctNode)best).getAbstractMove(), state.clone());

		return actions;
		
	}

	private float traverse(UctNode node, GameState state) {
		
		float score = 0;
		if (node.getVisits() == 0){
			node.setMove(statesToActions(((IuctNode)node).getAbstractMove(), state));
			updateState(node, state, true);
			score = evaluate(state.clone());
		} else {
			updateState(node, state, false);
			if (state.isTerminal()){
				score = evaluate(state.clone());
			} else {
				int playerToMove = getPlayerToMove(node, state);
				if (expandable(node, playerToMove))
					generateChildren(node, state, playerToMove);
				score = traverse(selectNode(node), state);
			}
		}
		node.setVisits(node.getVisits() + 1);
		node.setTotalScore(node.getTotalScore() + score);
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
				e.printStackTrace();
			}
			node.setPossibleMoves(map);
			
		}
		
		boolean onlyNok = config.isNokModelling() && playerToMove != config.getMaxPlayerIndex();
		
		String label = "";
		if (node.getChildren().size() < scripts.size() && playerToMove == config.getMaxPlayerIndex()) {
			move.addAll(getAllMove(UnitStateTypes.values()[node.getChildren().size()],
													node.getPossibleMoves()));
			label = UnitStateTypes.values()[node.getChildren().size()].toString();
		} else if (playerToMove == config.getMaxPlayerIndex()) {
			move = getRandomMove(playerToMove, node.getPossibleMoves()); // Possible moves?
			label = "RANDOM";
		}
			
		if (move == null)
			return;
	
		if (uniqueMove(move, (IuctNode)node)){
			IuctNode child = new IuctNode((IuctNode)node, getChildNodeType(node, state), move, playerToMove, label);
			node.getChildren().add(child);
		}
		
	}
	
	private boolean uniqueMove(List<UnitState> move, UctNode node) {

		if(node.getChildren().isEmpty())
			return true;
		
		for (UctNode child : node.getChildren()){
			boolean identical = true;
			for(int i = 0; i < move.size(); i++){
				if (((IuctNode)child).getAbstractMove().get(i).type != move.get(i).type){
					identical = false;
					break;
				}
			}
			if (identical){
				return false;
			}
		}
		
		return true;
		
	}

	private List<UnitState> getAllMove(UnitStateTypes type, HashMap<Integer, List<UnitAction>> map) {

		List<UnitState> states = new ArrayList<UnitState>();
		
		for(Integer i : map.keySet()){
			
			List<UnitAction> actions = map.get(i);
			if (actions.isEmpty())
				continue;
			
			UnitState state = new UnitState(type, actions.get(0)._unit, actions.get(0)._player);
			states.add(state);
			
		}
		
		return states;
	}
	
	private List<UnitState> getRandomMove(int playerToMove, HashMap<Integer, List<UnitAction>> map) {
		
		ArrayList<UnitState> move = new ArrayList<UnitState>();
		
		for(Integer i : map.keySet()){
			
			// Skip empty actions
			List<UnitAction> actions = map.get(i);
			if (actions.isEmpty())
				continue;
			
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
			
			UnitState unitState = new UnitState(type, i, playerToMove);
			
			// Add random possible action
			move.add(unitState);
			
		}
		
		return move;
		
	}

	private List<UnitAction> statesToActions(List<UnitState> move, GameState state) {
		
		int player = 0;
		
		if (move == null || move.isEmpty() || move.get(0) == null)
			return new ArrayList<UnitAction>();
		else
			player = move.get(0).player;
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		try {
			state.generateMoves(map, player);
		} catch (Exception e) {e.printStackTrace();}
		
		ArrayList<ArrayList<Integer>> typeUnits = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < scripts.size(); i++) {
			typeUnits.add(new ArrayList<Integer>());
		}
		
		// Divide units into two groups
		for(UnitState unitState : move){
			
			//System.out.print(unitState.type.toString() + " ");
			
			for (int i = 0; i < scripts.size(); i++) {
				if (unitState.type == UnitStateTypes.values()[i]) {
					typeUnits.get(i).add(unitState.unit);
					break;
				}
			}
			
		}
<<<<<<< HEAD
		//System.out.println();
=======
>>>>>>> dac586a5c88b683a50aff2ffdbaf5195bedafc19
		
		List<UnitAction> allActions = new ArrayList<UnitAction>();
		ArrayList<HashMap<Integer, List<UnitAction>>> typeMap = new ArrayList<HashMap<Integer, List<UnitAction>>>();
		for (int i = 0; i < scripts.size(); i++) {
			typeMap.add(new HashMap<Integer, List<UnitAction>>());
		}
		
		for (int i = 0; i < scripts.size(); i++) {
			for (Integer j : typeUnits.get(i)) {
				if (map.get(j) != null) {
					typeMap.get(i).put(j, map.get(j));
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
	
	public String toString(){
		return NAME+"_IUCTCD - "+this.config.toString();
	}
	
}