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
 *  @author Urko Rueda
 */
package org.fruit.alayer.actions;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.ActionFailedException;
import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.State;
import org.fruit.alayer.SUT;
import org.fruit.alayer.NoSuchTagException;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.Keyboard;

/**
 * An action which presses a given Key on the Keyboard.
 */
public abstract class KeyAction extends TaggableBase implements Action {
	private static final long serialVersionUID = 4379174151668501105L;
	protected final KBKeys key;

	public KeyAction(KBKeys key){
		Assert.notNull(key);
		this.key = key;
	}
	
	public abstract String toString();

	public final void run(SUT system, State state, double duration) {
		try{
			Assert.notNull(system);
			Util.pause(duration);
			if (key.equals(KBKeys.VK_ARROBA) ||
				key.equals(KBKeys.VK_EXCLAMATION_MARK)) // java.awt.Robot throwing "Invalid key code"
				altNumpad(system,new Integer(key.code()).toString());
			else
				keyAction(system,key);
		}catch(NoSuchTagException tue){
			throw new ActionFailedException(tue);
		}
	}
	
	protected abstract void keyAction(SUT system, KBKeys key);
	
	protected void altNumpad(SUT system, String numpadCodes){
	    if (numpadCodes == null || !numpadCodes.matches("^\\d+$")){
	    	System.out.println("Unknown key: " + numpadCodes);
	        return;
	    }               
	    Keyboard keyb = system.get(Tags.StandardKeyboard);
	    keyb.press(KBKeys.VK_ALT);
	    for (char charater : numpadCodes.toCharArray()){
	        KBKeys NUMPAD_KEY = getNumpad(charater);
	        if (NUMPAD_KEY != null){
	        	keyb.press(NUMPAD_KEY);
	        	keyb.release(NUMPAD_KEY);
	        }
	    }
	    keyb.release(KBKeys.VK_ALT);        
	}	

	private KBKeys getNumpad(char numberChar){
		switch (numberChar){
	    case '0' : return KBKeys.VK_NUMPAD0;
	    case '1' : return KBKeys.VK_NUMPAD1;
	    case '2' : return KBKeys.VK_NUMPAD2;
	    case '3' : return KBKeys.VK_NUMPAD3;
	    case '4' : return KBKeys.VK_NUMPAD4;
	    case '5' : return KBKeys.VK_NUMPAD5;
	    case '6' : return KBKeys.VK_NUMPAD6;
	    case '7' : return KBKeys.VK_NUMPAD7;
	    case '8' : return KBKeys.VK_NUMPAD8;
	    case '9' : return KBKeys.VK_NUMPAD9;
	    default  : System.out.println("AltNumpad - not a number 0-9: " + numberChar);
        		   return null;
		}
	}
	
	// by urueda
	@Override
	public String toShortString(){
		Role r = get(Tags.Role, null);
		if (r != null)
			return r.toString();
		else
			return toString();
	}
    
	// by urueda
	@Override
	public String toParametersString(){
		return "(" + key.toString() + ")";
	}
	
}