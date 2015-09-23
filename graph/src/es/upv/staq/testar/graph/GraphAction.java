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

import org.fruit.alayer.Action;

/**
 * Represents a graph action.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class GraphAction implements IGraphAction {
	
	private static final long serialVersionUID = 2855327250894909478L;
	
	//private WeakReference<Action> action;
	//private Object actionZipped;
	
	private String stateShotPath;
	
	private String name; // graph action id for the state
	private String type; // graph action type id (without considering the state)
	private String detailedName = "???"; // graph action descriptive representation
	
	private int count = -1; // number of times action was executed
	private String order; // action order in a test sequence (may appear multiple times)
	
	private double actionReward = UNEXPLORED; // selection reward weight		
	
	public GraphAction(String actionName){
		this(null,actionName,"");
	}
	
	public GraphAction(Action action, String stateactionID, String actionID){
		//this.action = new WeakReference<Action>((action == null) ? new NOP() : action);
		//this.actionZipped = ZipManager.compress(action);
		//if (this.actionZipped == action)
		//	this.action = null; // compression failed
		this.stateShotPath = null;
		this.name = stateactionID;
		this.type = actionID;
		count = 1;
		this.order = "";
	}
	
	/*@Override
	public Action getAction(){
		Action a = this.action == null ? null : this.action.get();
		if (a != null)
			return a;
		if (this.actionZipped instanceof byte[])
			return (Action) ZipManager.uncompress((byte[])this.actionZipped);
		else
			return (Action) this.actionZipped;
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
	public String getActionName() {
		return name;
	}
	
	@Override
	public String getActionType() {
		return type;
	}	
	
	@Override
	public String getDetailedName() {
		return detailedName;
	}

	@Override
	public void setDetailedName(String detailedName){
		this.detailedName = detailedName;
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
	public String getOrder(){
		return order;
	}
	
	@Override
	public void addOrder(String order){		
		this.order = this.order + "[" + order + "]";
	}
	
	@Override
	public double getActionReward() {
		return actionReward;
	}

	@Override
	public void setActionReward(double actionReward) {
		this.actionReward = actionReward;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(!(o instanceof GraphAction))
			return false;
		return name.equals(((GraphAction)o).name);
	}
	
	@Override
	public String toString(){
		return name;
	}
	
}