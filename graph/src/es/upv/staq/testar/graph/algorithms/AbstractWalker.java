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

import es.upv.staq.testar.graph.IEnvironment;
import es.upv.staq.testar.graph.IGraphAction;
import es.upv.staq.testar.graph.IGraphState;
import es.upv.staq.testar.graph.Movement;
import es.upv.staq.testar.graph.WalkStopper;
import es.upv.staq.testar.graph.reporting.WalkReport;

/**
 * An abstract graph walker.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public abstract class AbstractWalker implements IWalker {

	@Override
	final public void walk(IEnvironment env, WalkStopper walkStopper){
		IGraphState lastS = null;
		IGraphAction lastA = null;
		while(walkStopper.continueWalking()){
			Movement m = env.getMovement();
			if (m != null){
				IGraphState s = m.getVertex();
				IGraphAction a = m.getEdge();
				if(lastA != null)
					update(env, lastS, lastA, s);				
				lastS = s;
				lastA = a;
			}
		}
		env.finishGraph(walkStopper.walkStatus(), lastS, lastA, walkStopper.walkEndState());
	}
	
	final protected void update(IEnvironment env, IGraphState s1, IGraphAction a1, IGraphState s2){
		env.populateEnvironment(s1,a1,s2);
		a1.setActionReward(calculateReward(env, s1, a1));
	}	
	
	protected double degradeReward(int reward, double maxReward){
		return (reward > 0 ? 1.0 / reward : maxReward);		
	}		
	
	@Override
	public WalkReport getReport() {
		// TODO Auto-generated method stub
		return null;
	}	
	
}
