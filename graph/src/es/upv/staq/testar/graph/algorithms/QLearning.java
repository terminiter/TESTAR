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

import java.util.Random;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import es.upv.staq.testar.graph.IEnvironment;
import es.upv.staq.testar.graph.IGraphAction;
import es.upv.staq.testar.graph.IGraphState;

/**
 * Q-learning walker.
 * 
 * Status: experimental
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class QLearning extends AbstractWalker { // Q = Reward
	
	private double maxReward;
	private double discount;
	
	public QLearning(double discount, double maxReward){
		this.maxReward = maxReward;
		this.discount = discount;
	}

	// warning: if graph movements consuming is out of sync with TESTAR movements producing ...
	// ... then, we do not have op to date graph information (i.e. rewards, states/actions explored)
	@Override
	public Action selectAction(IEnvironment env, State state, Set<Action> actions) {
		return selectProportionate(env, state, actions); // selectMax(env, state, actions);
	}

	@Override
	public double calculateReward(IEnvironment env, IGraphState state, IGraphAction action) {
		int[] actionWCount = env.getWalkedCount(action);
		double actionReward = degradeReward(actionWCount[0],maxReward) * degradeReward(actionWCount[1],maxReward);
		IGraphState gs = env.getTargetState(action);
		double reward = actionReward +
						((gs == null) ? maxReward // state not explored
									  : discount * env.getMaxReward(gs));
		return reward;
	}
	
	private Action selectProportionate(IEnvironment env, State state, Set<Action> actions){
		double sum = .0, rew, r = new Random(System.currentTimeMillis()).nextDouble();
		for (Action a : actions){
			rew = env.convertAction(state,a).getActionReward();			
			if (rew == IGraphAction.UNEXPLORED)
				rew = this.maxReward;
			sum += rew;
		}
		
		double frac = 0.0, q;
		Action selection = null;
		for (Action a : actions){
			rew = env.convertAction(state,a).getActionReward();
			if (rew == IGraphAction.UNEXPLORED)
				rew = this.maxReward;
			q = rew;
			if((frac / sum <= r) && ((frac + q) / sum >= r)){
				selection = a;
				break;
			}
			frac += q;			
		}
		
		return selection; // revise: may return null in some case?
	}

	/*private Action selectMax(IEnvironment env, State state, Set<Action> actions){
		double maxDesirability = 0.0;
		double q;
		Action selection = null;
		for(Action a : actions){
			q = env.convertAction(state,a).getActionReward();
			if(q > maxDesirability){
				maxDesirability = q;
				selection = a;
			}
		}
		return selection;
	}*/
	
}
