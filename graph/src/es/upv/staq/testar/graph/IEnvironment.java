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

import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

/**
 * Graph environment.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public interface IEnvironment {
	
	/**
	 * Retrieves next movement.
	 * @return The movement.
	 */
	public Movement getMovement();
	
	/**
	 * Converts a TESTAR state into a graph state/node.
	 * @param state TESTAR state.
	 * @return Graph state.
	 */
	public IGraphState convertState(State state);
	
	/**
	 * Converts a TESTAR action into a graph action/edge.
	 * @param state The SUT state context for the action.
	 * @param action SUT action for the state.
	 * @return Graph action.
	 */
	public IGraphAction convertAction(State state, Action action);
			
	/**
	 * Converts a TESTAR action into a graph action/edge.
	 * @param graphState The graph state context for the action.
	 * @param action SUT action for the state.
	 * @return Graph action.
	 */
	public IGraphAction convertAction(IGraphState graphState, Action action);

	/**
	 * Populates the graph with movement information.
	 * @param fromState Graph state before action execution.
	 * @param action Executed action, abstracted for graph.
	 * @param toState Graph state after action execution.
	 */
	public void populateEnvironment(IGraphState fromState, IGraphAction action, IGraphState toState);
	
	/**
	 * Retrieves information about an action set for a SUT state.
	 * @param state A SUT state.
	 * @param actions The actions available for the state.
	 */
	public void notifyEnvironment(State state, Set<Action> actions);
		
	/**
	 * Retrieves the source state of an action.
	 * @param action A graph action.
	 * @return The source graph state.
	 */
	public IGraphState getSourceState(IGraphAction action);

	/**
	 * Retrieves the target state of an action.
	 * @param action A graph action.
	 * @return The target graph state.
	 */
	public IGraphState getTargetState(IGraphAction action);
	
	/**
	 * Gets the walked count for a graph action/edge.
	 * @param action The graph action.
	 * @return [0] = The number of times the state' action was walked in the graph.
	 *         [1] = The number of times the action type was walked in the graph.
	 */
	public int[] getWalkedCount(IGraphAction action);	
	
	/**
	 * Retrieves the set of unexplored actions from a SUT state.
	 * @param state The SUT state.
	 * @return The set of unexplored actions.
	 */
	public Set<IGraphAction> getUnexploredActions(State state);	
	
	/**
	 * Computes a graph state reward.
	 * @param graphState A graph state.
	 * @return The reward.
	 */
	public double getStateReward(IGraphState graphState);	
	
	/**
	 * Gets the max reward for a graph state.
	 * @param gs The graph state.
	 * @return The max reward for each graph action reward from the graph state.
	 */
	public double getMaxReward(IGraphState gs);
	
	/**
	 * Retrieves all the scanned actions for a SUT state by decreased reward.
	 * @param state The SUT graph state.
	 * @return Actions osrted by decreased reward.
	 */
	public IGraphAction[] getSortedStateActionsByDecReward(IGraphState state);
	
	/**
	 * Retrieves a list of incrementally ordered actions by their test execution order.
	 * Examples:
	 * 	* Full list: fromOder = 1, toOrder = executed_actions_number
	 *  * Sublist: fromOder = 1 < X < executed_actions_number, toOrder = X < Y < executed_actions_number.
	 *  * Single element: 1 <= fromOrder = toOrder <= executed_actions_number. 
	 * @param fromOrder Retrieves the list from:  1 .. executed_actions_number.
	 * @param toOrder Retrieves the list to: fromOrder .. executed_actions_number.
	 * @return The list of actions.
	 */
	public IGraphAction[] getSortedActionsByOrder(int fromOrder, int toOrder);
	
	/**
	 * Finish the graph environment with ending test sequence. 
	 * @param walkStatus Test verdict: 'true' test OK, 'false' test FAIL.
	 * @param lastState Last grah state.
	 * @param lastAction Last graph action.
	 * @param walkEndState SUT state after executing last action from the last state.
	 */
	public void finishGraph(boolean walkStatus, IGraphState lastState, IGraphAction lastAction, State walkEndState);
	
}