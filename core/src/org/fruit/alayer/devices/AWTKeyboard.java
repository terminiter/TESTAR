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
package org.fruit.alayer.devices;

import java.awt.AWTException;
import java.awt.Robot;

import org.fruit.FruitException;

public final class AWTKeyboard implements Keyboard {
	public static AWTKeyboard build() throws FruitException{ return new AWTKeyboard(); }
	private final Robot robot;
	
	private AWTKeyboard(){
		try{
			robot = new Robot();
		}catch(AWTException awte){
			throw new FruitException(awte);
		}
	}
	
	public String toString() { return "AWT Keyboard"; }
	public void press(KBKeys k) { robot.keyPress(k.code());	}	
	public void release(KBKeys k) { robot.keyRelease(k.code());	}

	public void isPressed(KBKeys k) {
		throw new UnsupportedOperationException("Unfortunately AWT Keyboard cannot poll the keyboard's state!");
	}
}