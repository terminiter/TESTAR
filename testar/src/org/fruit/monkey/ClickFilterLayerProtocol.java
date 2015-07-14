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

import static org.fruit.alayer.windows.UIARoles.*;

import java.io.File;

import java.util.Collections;

import java.util.List;

import java.util.HashSet;

import java.util.Set;

import org.fruit.Assert;

import org.fruit.Pair;

import org.fruit.Util;

import org.fruit.alayer.Action;

import org.fruit.alayer.ActionBuildException;

import org.fruit.alayer.ActionFailedException;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Point;
import org.fruit.alayer.Role;

import org.fruit.alayer.Roles;

import org.fruit.alayer.State;

import org.fruit.alayer.SUT;

import org.fruit.alayer.ShapeVisualizer;
import org.fruit.alayer.State;

import org.fruit.alayer.StateBuildException;

import org.fruit.alayer.StrokePattern;
import org.fruit.alayer.SystemStartException;

import org.fruit.alayer.Tags;

import org.fruit.alayer.Verdict;

import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;

import org.fruit.alayer.actions.AnnotatingActionCompiler;

import org.fruit.alayer.actions.StdActionCompiler;

import org.fruit.alayer.devices.KBKeys;

import org.fruit.alayer.devices.MouseButtons;

import static org.fruit.alayer.windows.UIATags.*;

import static org.fruit.monkey.ConfigTags.*;

import org.fruit.monkey.DefaultProtocol;

import org.fruit.monkey.Settings;

import org.fruit.monkey.ConfigTags;

import org.fruit.alayer.Tags;
import static org.fruit.alayer.Tags.NotResponding;
import static org.fruit.alayer.Tags.IsRunning;
import static org.fruit.alayer.Tags.RunningProcesses;
import static org.fruit.alayer.Tags.SystemActivator;
import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Title;
import static org.fruit.alayer.Tags.Foreground;
import static org.fruit.alayer.Tags.Enabled;


// by urueda@upvlc
public class ClickFilterLayerProtocol extends DefaultProtocol {

    private SUT sut = null;

    private boolean vkShiftPressed = false;
    private Set<String> clickFilterOnDemand = new HashSet();

	/**
	 * This method is called when the Rogue User starts the System Under Test (SUT). The method should
	 * take care of 
	 *   1) starting the SUT (you can use the Rogue User's settings obtainable from <code>settings()</code> to find
	 *      out what executable to run)
	 *   2) bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 *      the SUT's configuratio files etc.)
	 *   3) waiting until the system is fully loaded and ready to be tested (with large systems, you might have to wait several
	 *      seconds until they have finished loading)
     * @return  a started SUT, ready to be tested.
	 */
	protected SUT startSystem() throws SystemStartException{
        this.sut = super.startSystem();
		return sut;
	}
	
    @Override
    protected void keyDown(KBKeys key) {
        super.keyDown(key);
        if (key == KBKeys.VK_SHIFT) {
            vkShiftPressed = true;
        }
    }

    protected void keyUp(KBKeys key) {
        if (key == KBKeys.VK_SHIFT) {
            vkShiftPressed = false;
        }
    }
    
    @Override
	protected void mouseDown(MouseButtons btn, double x, double y){
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