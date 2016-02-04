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
 * This software is distributed FREE of charge under the TESTAR license, as an open      *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package es.upv.staq.testar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.ActionRoles;

/**
 * Core coding manager.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class CodingManager {

	public static final int ID_LENTGH = 12;
	
	private static WeakHashMap<String,String> stateCodingCache = new WeakHashMap<String,String>();
	private static WeakHashMap<String,String> actionCodingCache = new WeakHashMap<String,String>();
	
	private static WeakHashMap<String,String> toAbstractStateCodingCache = new WeakHashMap<String,String>();
	//private static WeakHashMap<String,String> toAbstractActionCodingCache = new WeakHashMap<String,String>();

	public static final Tag<?>[] ABSTRACTION_STATE_TAGS = new Tag<?>[]{Tags.Role};
	public static final Tag<?>[] UNIQUE_STATE_TAGS = new Tag<?>[]{Tags.Role, Tags.Title};
	public static final Role[] ABSTRACTION_ACTION_DISCARD_PARAMETERS = new Role[]{ActionRoles.MouseMove, ActionRoles.Type};
	
	private static HashMap<String,Set<String>> stateClusters = new HashMap<String,Set<String>>(); // clusters of widget-trees and state' widgets
	private static HashMap<String,Set<String>> actionClusters = new HashMap<String,Set<String>>();
	
	//---------------
	// STATES CODING
	//---------------
	
	public static String codify(Widget state){
		String id = codifyLimitedTo(state, UNIQUE_STATE_TAGS);
		craftAbstractionGroup(state,id);
		return id;
	}
	
	public static String codifyLimitedTo(Widget state, Tag<?>... tags){
		return codifyLimitedTo(true,state,tags);
	}

	//----------------
	// ACTIONS CODING
	//----------------	

	public static String codify(Action action){
		return codifyDiscardingParams(action);
	}
	
	public static String codify(Widget state, Action action){
		String id = CodingManager.codifyDiscardingParams(codify(state),action);
		craftAbstractionGroup(CodingManager.codifyLimitedTo(false,state,ABSTRACTION_STATE_TAGS),action,id);
		return id;
	}

	public static String codify(String stateID, Action action){
		return codifyDiscardingParams(stateID,action);
	}
		
	public static String codifyDiscardingParams(Action action, Role... discardParameters){
		String actionS = action.toString(discardParameters);
		String actionCode = actionCodingCache.get(actionS);
		if (actionCode == null){
			actionCode = positiveHash(actionS.hashCode());
			actionCodingCache.put(actionS,actionCode);
		}
		return actionCode;
	}	
	
	public static String codifyDiscardingParams(String stateID, Action action, Role... discardParameters){
		String actionCode = codifyDiscardingParams(action,discardParameters);
		return positiveHash((stateID + actionCode).hashCode());
	}
	
	//-------------------
	// AUXILIARY METHODS
	//-------------------
	
	private static String positiveHash(int hash){
		if (hash > 0)
			return new Integer(hash).toString();
		else if (hash < 0)
			//return "_" + new Integer(Math.abs(hash)).toString();
			return "n" + new Integer(Math.abs(hash)).toString();
		else
			return "0";
	}

	private static String codifyLimitedTo(boolean craftAbstrationGroups, Widget state, Tag<?>... tags){
		String stateS = state.toString(tags);
		String stateCode = stateCodingCache.get(stateS);
		if (stateCode == null){
			stateCode = positiveHash(stateS.hashCode());
			stateCodingCache.put(stateS,stateCode);
		}
		if (craftAbstrationGroups)
			craftAbstractionGroup(state,stateCode);
		return stateCode;		
	}
	
	private static void craftAbstractionGroup(Widget state, String id){
		String abstractID = codifyLimitedTo(false, state, ABSTRACTION_STATE_TAGS);
		synchronized(stateClusters){
			Set<String> ids = stateClusters.get(abstractID);
			if (ids == null){
				ids = new HashSet<String>();
				stateClusters.put(abstractID,ids);
			}
			ids.add(id);		
		}
	}	
	
	private static void craftAbstractionGroup(String abstractStateID, Action action, String id){
		String abstractID = codifyDiscardingParams(abstractStateID,action,ABSTRACTION_ACTION_DISCARD_PARAMETERS);
		synchronized(actionClusters){
			Set<String> ids = actionClusters.get(abstractID);
			if (ids == null){
				ids = new HashSet<String>();
				actionClusters.put(abstractID,ids);
			}
			ids.add(id);		
		}
	}
	
	public static HashMap<String,Set<String>> getStateClusters(){
		HashMap<String,Set<String>> stateClustersCopy = new HashMap<String,Set<String>>();
		synchronized(stateClusters){
			for (String cluster : stateClusters.keySet())
				stateClustersCopy.put(cluster,new HashSet<String>(stateClusters.get(cluster)));
		}
		return stateClustersCopy;
	}
	
	public static HashMap<String,Set<String>> getActionClusters(){
		HashMap<String,Set<String>> actionClustersCopy = new HashMap<String,Set<String>>();
		synchronized(actionClusters){
			for (String cluster : actionClusters.keySet())
				actionClustersCopy.put(cluster,new HashSet<String>(actionClusters.get(cluster)));
		}
		return actionClustersCopy;
	}
	
	public static String toAbstractState(String stateID){
		String absID = toAbstractStateCodingCache.get(stateID);
		if (absID != null)
			return absID;
		else {
			for (String cluster : stateClusters.keySet()){
				if (stateClusters.get(cluster).contains(stateID)){
					toAbstractStateCodingCache.put(stateID, cluster);
					return cluster;
				}
			}
		}
		System.out.println("No abstract coding for state: " + stateID);
		return stateID;
	}
	
	
	/*public static String toAbstractAction(String actionID){
		String absID = toAbstractActionCodingCache.get(actionID);
		if (absID != null)
			return absID;
		else {
			for (String cluster : actionClusters.keySet()){
				if (actionClusters.get(cluster).contains(actionID)){
					toAbstractActionCodingCache.put(actionID, cluster);
					return cluster;
				}
			}
		}
		System.out.println("No abstract coding for action: " + actionID);
		return actionID;
	}*/
	
}
