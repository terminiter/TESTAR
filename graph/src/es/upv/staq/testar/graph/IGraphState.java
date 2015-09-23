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

import java.io.Serializable;
import java.util.Set;

import org.fruit.alayer.Action;

public interface IGraphState extends Serializable {
	
	//public State getState();
	
	public void setStateshot(String scrShotPath);
	public String getStateshot();

	public int getCount();
	public void incCount();
	
	public void updateUnexploredActions(IEnvironment env,
										Set<Action> availableActions,
										Set<IGraphAction> exploredActions);
	
	public Set<IGraphAction> getUnexploredActions();
	
}
