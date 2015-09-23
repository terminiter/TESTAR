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

import java.util.HashSet;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

/**
 * Represents a grah state.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class GraphState implements IGraphState{
	
	private static final long serialVersionUID = -979506754547522783L;
	
	//private WeakReference<State> state;
	//private Object stateZipped;
	
	private String stateShotPath;
	
	private String name;
	
	private int count = -1; // number of times state was traversed
	
	private Set<IGraphAction> unexploredActions;
	
	public GraphState(String name){
		this(null,name);	
	}
	
	public GraphState(State state, String name){
		//this.state = new WeakReference<State>((state == null) ? new StdState() : state);
		//this.stateZipped = ZipManager.compress(state);
		//if (this.stateZipped == state)
		//	this.state = null; // compression failed
		this.stateShotPath = null;
		this.name = name;
		count = 1;
		this.unexploredActions = new HashSet<IGraphAction>();
	}
	
	/*@Override
	public State getState(){
		State s = this.state == null ? null : this.state.get();
		if (s != null)
			return s;
		if (this.stateZipped instanceof byte[])
			return (State) ZipManager.uncompress((byte[])this.stateZipped);
		else
			return (State) this.stateZipped;
	}*/

	@Override
	public void setStateshot(String scrShotPath) {
		stateShotPath = scrShotPath;
	}
	
	@Override
	public String getStateshot(){
		return stateShotPath;
	}
	
	@Override
	public int getCount(){
		return count;
	}

	@Override
	public void incCount(){
		count++;
	}

	@Override
	public void updateUnexploredActions(IEnvironment env,
										Set<Action> availableActions, Set<IGraphAction> exploredActions){
		IGraphAction ga;
		for (Action a : availableActions){
			ga = env.convertAction(this,a);
			if (!exploredActions.contains(ga))
				this.unexploredActions.add(ga);
		}
	}
	
	@Override
	public Set<IGraphAction> getUnexploredActions(){
		return this.unexploredActions;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(!(o instanceof GraphState))
			return false;
		return name.equals(((GraphState)o).name);
	}
	
	public String toString(){
		return name;
	}
	
}
