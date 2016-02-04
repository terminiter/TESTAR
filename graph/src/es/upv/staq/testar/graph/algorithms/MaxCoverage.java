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

package es.upv.staq.testar.graph.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import es.upv.staq.testar.graph.Grapher;
import es.upv.staq.testar.graph.IEnvironment;
import es.upv.staq.testar.graph.IGraphAction;
import es.upv.staq.testar.graph.IGraphState;

/**
 * A walker that tries to explore the complete SUT IU space.
 * 
 * Status: experimental && under dev.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class MaxCoverage extends AbstractWalker {
	
	private Random rnd;
	
	public MaxCoverage(Random rnd){
		this.rnd = rnd;
	}

	@Override
	public Action selectAction(IEnvironment env, State state, Set<Action> actions) {
		Grapher.syncMovements(); // synchronize graph movements consumption for up to date rewards and states/actions exploration
		Set<Action> actionsCopy = new HashSet<Action>(actions);
		Set<IGraphAction> unexplored = env.getUnexploredActions(state);
		if (unexplored.isEmpty()){ // return max rewarded action
			IGraphAction[] sortedActions = env.getSortedStateActionsByDecReward(env.convertState(state));
			if (sortedActions != null){
				HashMap<IGraphAction,Action> graphActions = new HashMap<IGraphAction,Action>(actions.size());
				for (Action a : actions)
					graphActions.put(env.convertAction(state,a),a);
				for (IGraphAction a : sortedActions){
					if (graphActions.keySet().contains(a)){
						System.out.println("SELECT ACTION - explored: <" + a.toString() + ">");
						return graphActions.get(a);
					}
				}
			}
			// no scanned actions did match the state' actions
			Action retA = new ArrayList<Action>(actions).get(rnd.nextInt(actions.size()));	// randomly select a state action
			//System.out.println("SELECT ACTION - explored BUT NOT SCANNED (random ...)!!! <" + env.convertAction(state,retA).toString() + ">");
			return retA;
		}else{ // return a random unexplored action
			for (Action a : actionsCopy){
				if (!unexplored.contains(env.convertAction(state,a)))
					actionsCopy.remove(a);
			}
			if (actionsCopy.size() > 0){
				Action retA = new ArrayList<Action>(actionsCopy).get(rnd.nextInt(actionsCopy.size()));
				//System.out.println("SELECT ACTION - un_explored: <" + env.convertAction(state,retA).toString() + ">");
				return retA;
			}else{
				Action retA = new ArrayList<Action>(actions).get(rnd.nextInt(actions.size()));
				//System.out.println("SELECT ACTION - un_explored BUT NOT SCANNED (random ...)!!! <" + env.convertAction(state,retA).toString() + ">");
				return retA;
			}
		}
	}

	@Override
	public double calculateReward(IEnvironment env, IGraphState state, IGraphAction action) {
		int[] actionWCount = env.getWalkedCount(action);
		double actionReward = degradeReward(actionWCount[0],1.0) * degradeReward(actionWCount[1],1.0);
		double targetStateReward = env.getStateReward(env.getTargetState(action));
		//System.out.println("REWARD for <" + state.toString() + "," + action.toString() + "> = (" + actionReward + "," + targetStateReward + ")");
		return actionReward + targetStateReward;
	}

}
