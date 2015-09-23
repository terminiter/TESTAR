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

import java.util.WeakHashMap;

import org.fruit.alayer.Action;
import org.fruit.alayer.Widget;

/**
 * Core coding manager.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class CodingManager {

	public static final int ID_LENTGH = 12;
	
	private static WeakHashMap<Integer,String> stateCodingCache = new WeakHashMap<Integer,String>();
	private static WeakHashMap<Integer,String> actionCodingCache = new WeakHashMap<Integer,String>();
	
	private static String positiveHash(int hash){
		if (hash > 0)
			return new Integer(hash).toString();
		else if (hash < 0)
			//return "_" + new Integer(Math.abs(hash)).toString();
			return "n" + new Integer(Math.abs(hash)).toString();
		else
			return "0";
	}	
	
	public static String codify(Widget state){
		int hashC = state.toString().hashCode();
		String stateCode = stateCodingCache.get(new Integer(hashC));
		if (stateCode == null){
			stateCode = positiveHash(hashC);;
			stateCodingCache.put(new Integer(hashC), stateCode);
		}
		return stateCode;
	}	
	
	public static String codify(Widget state, Action action){
		return CodingManager.codify(codify(state),action);
	}

	public static String codify(String stateID, Action action){
		String actionCode = codify(action);
		return positiveHash((stateID + actionCode).hashCode());		
	}
	
	public static String codify(Action action){
		int hashC = action.toString().hashCode();
		String actionCode = actionCodingCache.get(new Integer(hashC));
		if (actionCode == null){
			actionCode = positiveHash(hashC);
			actionCodingCache.put(new Integer(hashC), actionCode);
		}
		return actionCode;
	}	
	
}
