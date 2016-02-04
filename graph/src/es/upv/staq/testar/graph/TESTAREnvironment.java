/*****************************************************************************************
 *                                                                                       *
 * COPYRIGHT (2015):                                                                     *
 * Universitat Politecnica de Valencia                                                   *
 * Camino de Vera, s/n                                                                   *
 * 46022 Valencia, Spain                                                                 *
 * www.upv.es                                                                            *
 *                                                                                       * 
 * D I S C L A I M E R:                                                                  *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)     *
 * in the context of the TESTAR Proof of Concept project:                                *
 *               "UPV, Programa de Prueba de Concepto 2014, SP20141402"                  *
 * This graph project is distributed FREE of charge under the TESTAR license, as an open *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package es.upv.staq.testar.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.graph.reporting.GraphReporter;

/**
 * 
 * @author Urko Rueda Molina (urueda)
 *
 */
public class TESTAREnvironment implements IEnvironment {

	private TESTARGraph g;
	
	private HashMap<String,Set<Action>> pendingUpdates;
	
	private WeakHashMap<String,IGraphState> graphStates;
	private WeakHashMap<String,IGraphAction> graphActions;
	
	private List<IGraphState> nongraphStates; // graph states not yet in graph
	private List<IGraphAction> nongraphActions; // graph actions not yet in graph
	
	private List<int[]> explorationCurve; // [0] unique_states, [1] unique_actions, [2] abstract_states, [3] abstract_actions
	
	public TESTAREnvironment(){
		this(TESTARGraph.buildEmptyGraph());
		this.pendingUpdates = new HashMap<String,Set<Action>>();
		this.graphStates = new WeakHashMap<String,IGraphState>();
		this.graphActions = new WeakHashMap<String,IGraphAction>();
		this.nongraphStates = new ArrayList<IGraphState>();
		this.nongraphActions = new ArrayList<IGraphAction>();
		this.explorationCurve = new ArrayList<int[]>();
	}
	
	public TESTAREnvironment(TESTARGraph g){
		this.g = g;
	}
	
	@Override
	public Movement getMovement() {
		return Grapher.getMovement();
	}	
	
	private IGraphState exists(State state){
		String stateID = CodingManager.codify(state);
		return existsState(stateID);
	}
	
	private synchronized IGraphState existsState(String stateID){
		IGraphState graphState = this.graphStates.get(stateID);
		if (graphState != null)
			return graphState;
		for (IGraphState gs : g.vertexSet()){
			if (gs.toString().equals(stateID)){
				this.graphStates.put(stateID, gs);
				return gs;
			}
		}
		for (IGraphState gs : this.nongraphStates){
			if (gs.toString().equals(stateID))
				return gs;
		}
		return null;		
	}
	
	private IGraphAction exists(String stateID, Action action){
		String stateactionID = CodingManager.codify(stateID,action);
		return existsAction(stateactionID);
	}
	
	private synchronized IGraphAction existsAction(String stateactionID){
		IGraphAction graphAction = this.graphActions.get(stateactionID);
		if (graphAction != null)
			return graphAction;
		for (IGraphAction ga : g.edgeSet()){
			if (ga.toString().equals(stateactionID)){
				this.graphActions.put(stateactionID, ga);
				return ga;
			}
		}
		for (IGraphAction ga : this.nongraphActions){
			if (ga.toString().equals(stateactionID))
				return ga;
		}
		return null;	
	}
	
	@Override
	public synchronized IGraphState convertState(State state){
		IGraphState gs = exists(state);
		if (gs == null){
			gs = new GraphState(state,CodingManager.codify(state));
			this.nongraphStates.add(gs);
			return gs;
		} else
			return gs;
	}	
	
	@Override
	public synchronized IGraphAction convertAction(State state, Action action){
		return convertAction(CodingManager.codify(state),action);
	}			
	
	@Override
	public synchronized IGraphAction convertAction(IGraphState graphState, Action action){
		return convertAction(graphState.toString(),action);
	}
	
	private IGraphAction convertAction(String stateID, Action action){
		IGraphAction ga = exists(stateID,action);
		if (ga == null){
			ga = new GraphAction(action, CodingManager.codify(stateID,action), CodingManager.codify(action));
			this.nongraphActions.add(ga);
			return ga;
		}else
			return ga;
	}
	
	private void updateAvailableActions(IGraphState graphState){
		Set<Action> actions = pendingUpdates.get(graphState.toString());
		if (actions != null){
			graphState.updateUnexploredActions(this, actions, g.outgoingEdgesOf(graphState));
			pendingUpdates.remove(graphState.toString());
		}		
	}

	private void purgeNongraphState(IGraphState state){
		for (IGraphState gs : this.nongraphStates){
			if (gs.toString().equals(state.toString())){
				this.nongraphStates.remove(gs);
				return;
			}
		}
	}

	private void purgeNongraphAction(IGraphAction action){
		for (IGraphAction ga : this.nongraphActions){
			if (ga.toString().equals(action.toString())){
				this.nongraphStates.remove(ga);
				return;
			}
		}
	}

	@Override
	public void populateEnvironment(IGraphState fromState, IGraphAction action, IGraphState toState) {
		if (g.vertexSet().size() == 0){ // first SUT state
			IGraphState startState = new GraphState("ENTRY");
			g.addVertex(startState);
			g.addVertex(this,fromState);
			purgeNongraphState(fromState);
			updateAvailableActions(fromState);
			g.addEdge(this, startState, fromState, new GraphAction("START")); 
		}
		g.addVertex(this,toState);
		purgeNongraphState(toState);
		updateAvailableActions(toState);
		g.addEdge(this, fromState, toState, action);
		purgeNongraphAction(action);
		sampleExploration();
	}	
	
	@Override
	public void notifyEnvironment(State state, Set<Action> actions){
		if (exists(state) == null)
			pendingUpdates.put(CodingManager.codify(state),actions);
	}
	
	@Override
	public IGraphState getSourceState(IGraphAction action){
		return g.getEdgeSource(action);
	}
	
	@Override
	public IGraphState getTargetState(IGraphAction action){
		return g.getEdgeTarget(action);
	}
	
	@Override
	public int[] getWalkedCount(IGraphAction action) {
		int[] walkC = new int[2];		
		walkC[0] = action.getCount();
		walkC[1] = 0;
		for (IGraphAction ga : g.edgeSet()){
			if (action.getActionType().equals(ga.getActionType()))
				walkC[1] += ga.getCount(); // action type count (for all states)
		}
		return walkC;
	}

	@Override
	public Set<IGraphAction> getUnexploredActions(State state) {
		return this.convertState(state).getUnexploredActions();
	}

	@Override
	public double getStateReward(IGraphState graphState) {
		double reward = graphState.getUnexploredActions().size();
		for (IGraphAction ga : g.outgoingEdgesOf(graphState))
			reward += ga.getActionReward();
		return reward;	
	}		
	
	@Override
	public double getMaxReward(IGraphState graphState){
		double max = 0.0;
		for (IGraphAction ga : g.outgoingEdgesOf(graphState)){
			if (ga.getActionReward() > max)
				max = ga.getActionReward();
		}
		return max;			
	}
	
	@Override
	public IGraphAction[] getSortedStateActionsByDecReward(IGraphState state) {
		Set<IGraphAction> actions = g.outgoingEdgesOf(state);
		IGraphAction[] sortedActions = actions.toArray(new IGraphAction[actions.size()]);
		Arrays.sort(sortedActions, new Comparator<Object>() {
			@Override
			public int compare(Object graphAction1, Object graphAction2) {
				return (new Double(((IGraphAction)graphAction2).getActionReward())).compareTo(
					    new Double(((IGraphAction)graphAction1).getActionReward())); // descending order
				//return (new Double(((IGraphAction)graphAction1).getActionReward())).compareTo(
				//	    new Double(((IGraphAction)graphAction2).getActionReward())); // ascending order
			}
		});
		return sortedActions;
	}
	
	@Override
	public IGraphAction[] getSortedActionsByOrder(int fromOrder, int toOrder) {
		List<IGraphAction> orderedSequenceActions = g.getOrderedActions();
		int low = fromOrder >= orderedSequenceActions.size() ?
				orderedSequenceActions.size() - 1 :
				fromOrder < 0 ? 0 : fromOrder;
		if (toOrder < fromOrder)
			return null;
		int high = toOrder >= orderedSequenceActions.size() ? orderedSequenceActions.size() - 1 : toOrder;
		int actionsN = high - low + 1;
		IGraphAction[] gas = new IGraphAction[actionsN];
		for (int i=0; i < actionsN; i++)
			gas[i] = orderedSequenceActions.get(low + i);
		return gas;
	}	
	
	private boolean stateAtGraph(String stateID){
		for (IGraphState gs : g.vertexSet()){
			if (gs.toString().equals(stateID))
				return true;
		}
		return false;
	}	
	
	@Override
	public HashMap<String,Set<String>> getGraphStateClusters(){
		Set<String> clusterStates;
		HashMap<String,Set<String>> stateClusters = CodingManager.getStateClusters();
		for (String cluster : stateClusters.keySet().toArray(new String[stateClusters.size()])){
			clusterStates = stateClusters.get(cluster);
			for (String state : clusterStates.toArray(new String[clusterStates.size()])){
				if (!stateAtGraph(state))
					clusterStates.remove(state);
			}
			if (clusterStates.isEmpty())
				stateClusters.remove(cluster);
		}
		return stateClusters;
	}
	
	private boolean actionAtGraph(String stateactionID){
		for (IGraphAction ga : g.edgeSet()){
			if (ga.toString().equals(stateactionID))
				return true;
		}
		return false;
	}
	
	@Override
	public HashMap<String,Set<String>> getGraphActionClusters(){
		Set<String> clusterActions;
		HashMap<String,Set<String>> actionClusters = CodingManager.getActionClusters();
		for (String cluster : actionClusters.keySet().toArray(new String[actionClusters.size()])){
			clusterActions = actionClusters.get(cluster);
			for (String action : clusterActions.toArray(new String[clusterActions.size()])){
				if (!actionAtGraph(action))
					clusterActions.remove(action);
			}
			if (clusterActions.isEmpty())
				actionClusters.remove(cluster);
		}
		return actionClusters;
	}	
	
	@Override
	public void finishGraph(boolean walkStatus,
							IGraphState lastState,
							IGraphAction lastAction,
							State walkEndState) {
		if (lastState == null || lastAction == null || walkEndState == null)
			return;
		IGraphState weState = convertState(walkEndState);
		populateEnvironment(lastState,lastAction,weState);
		IGraphState v = new GraphState(null, walkStatus ? "PASS" : "FAIL");
		g.addVertex(this, v);
		g.addEdge(this, weState, v, new GraphAction("STOP"));
	}
	
	private int sampleExplorationCount = 0;
	private final int EXPLORATION_SAMPLE_INTERVAL = 100;
	private void sampleExploration(){
		sampleExplorationCount++;
		if (sampleExplorationCount % EXPLORATION_SAMPLE_INTERVAL == 0){
			int[] sample = new int[]{
				g.vertexSet().size() - 1, // without start state
				g.edgeSet().size() - 1, // without start edges
				getGraphStateClusters().size(), // abstract states
				getGraphActionClusters().size() // abstract actions
			};
			explorationCurve.add(sample);
			System.out.println("ExplorationCurve (unique states/actions, abstract states/actions): " + 
							   sample[0] + ", " + sample[1] + ", " + sample[2] + ", " + sample[3]);
		}
	}
	
	@Override
	public List<int[]> getExplorationCurve(){
		return explorationCurve;
	}
	
	@Override
	public String toString(){
		return GraphReporter.PrintResults(this, g);
	}
	
}
