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
package org.fruit.monkey;

import java.util.HashSet;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.Point;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.MouseButtons;

/**
 * Utility protocol (provides enhancements).
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class ClickFilterLayerProtocol extends DefaultProtocol {

    private SUT sut = null;

    private boolean vkShiftPressed = false;
    private Set<String> clickFilterOnDemand = new HashSet<String>();
	
    @Override
    protected void keyDown(KBKeys key) {
        super.keyDown(key);
        if (key == KBKeys.VK_SHIFT) {
            vkShiftPressed = true;
        }
    }

    @Override
    protected void keyUp(KBKeys key) {
    	super.keyUp(key);
        if (key == KBKeys.VK_SHIFT) {
            vkShiftPressed = false;
        }
    }
    
    @Override
	protected void mouseDown(MouseButtons btn, double x, double y){
    	super.mouseDown(btn, x, y);
        boolean leftM = btn == MouseButtons.BUTTON1, rightM = btn == MouseButtons.BUTTON3;
		if (vkShiftPressed && (leftM || rightM)) {
			Point cursor = mouse.cursor();
            Assert.notNull(cursor);
			Widget cursorWidget = Util.widgetFromPoint(getState(this.sut), cursor.x(), cursor.y(), null);
            if (cursorWidget != null) {
				String title = cursorWidget.get(Tags.Title, null);
                if (title != null) {
                    if (leftM) {
                        clickFilterOnDemand.add(title);
                    }
                    else if (rightM) {
                        clickFilterOnDemand.remove(title);
                    }
                }
				org.fruit.monkey.Main.logln("ClickFilter = " + printFilter(clickFilterOnDemand));
				//settings().set(ConfigTags.ClickFilter, printFilter(clickFilterOnDemand));		
			}
		}
	}

    private String printFilter(Set<String> set) {
        StringBuffer sb = new StringBuffer();
        for (String s : set) {
            sb.append(s + "|");
        }
        return sb.toString();
    }

}