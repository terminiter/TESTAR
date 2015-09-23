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

/**
 * Graph action/edge.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public interface IGraphAction extends Serializable {

	//public Action getAction();

	public void setStateshot(String scrShotPath);	
	public String getStateshot();

	public String getActionName();
	public String getActionType();
	
	public String getDetailedName();
	public void setDetailedName(String detailedName);

	public int getCount();
	public void incCount();

	public String getOrder();
	public void addOrder(String order);

	public static final double UNEXPLORED = -1.0;

	public double getActionReward();
	public void setActionReward(double actionReward);	
	
}
