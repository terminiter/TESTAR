/******************************************************************************************
 * COPYRIGHT:                                                                             *
 * Universitat Politecnica de Valencia 2013                                               *
 * Camino de Vera, s/n                                                                    *
 * 46022 Valencia, Spain                                                                  *
 * www.upv.es                                                                             *
 *                                                                                        * 
 * D I S C L A I M E R:                                                                   *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)      *
 * in the context of the european funded FITTEST project (contract number ICT257574)      *
 * of which the UPV is the coordinator. As the sole developer of this source code,        *
 * following the signed FITTEST Consortium Agreement, the UPV should decide upon an       *
 * appropriate license under which the source code will be distributed after termination  *
 * of the project. Until this time, this code can be used by the partners of the          *
 * FITTEST project for executing the tasks that are outlined in the Description of Work   *
 * (DoW) that is annexed to the contract with the EU.                                     *
 *                                                                                        * 
 * Although it has already been decided that this code will be distributed under an open  *
 * source license, the exact license has not been decided upon and will be announced      *
 * before the end of the project. Beware of any restrictions regarding the use of this    *
 * work that might arise from the open source license it might fall under! It is the      *
 * UPV's intention to make this work accessible, free of any charge.                      *
 *****************************************************************************************/

/**
 *  @author Sebastian Bauersfeld
 */
package org.fruit.alayer;

import java.io.Serializable;

/**
 * Actions take a system and a state as parameters and operate on the system (e.g. a left click). They
 * usually use the state to find certain values that might be necessary for their 
 * execution. In addition they might use a system's devices (Mouse, Keyboard, ...)
 * in order to execute a specific task.
 * For example: An action could always click on a menu item with the title
 * "Create New Document". Therefore it has to search the system's state
 * in order to find this menu item. It then needs to find out the item's position
 * and obtain the mouse device from the system in order to actually issue the click.
 * If it fails to do any of these tasks, it raises an exception.
 * Like states and systems, actions can have properties attached to them.
 * 
 * An action should be serializable, so that it can be stored and replayed.
 * 
 * @see SUT
 * @see State
 * @author Sebastian Bauersfeld
 */
public interface Action extends Taggable, Serializable {
	
	/**
	 * Takes a system and its state as input and executes a certain action (e.g. a click).
	 * The duration parameter indicates how fast the action should be executed. For example:
	 * If the action moves the cursor on a widget before clicking on it, a high value for
	 * this parameter will cause the mouse movement to be slowed down. However, the value
	 * is only a recommendation.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @param duration the duration of the action in seconds
	 * @throws ActionFailedException
	 */
	void run(SUT system, State state, double duration) throws ActionFailedException;
}