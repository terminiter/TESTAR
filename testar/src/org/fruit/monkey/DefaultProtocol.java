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

import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Enabled;
import static org.fruit.alayer.Tags.IsRunning;
import static org.fruit.alayer.Tags.ScreenshotPath;
import static org.fruit.alayer.Tags.Title;
/*import static org.fruit.alayer.windows.UIARoles.UIAEdit;
import static org.fruit.alayer.windows.UIARoles.UIAHeader;
import static org.fruit.alayer.windows.UIARoles.UIAMenu;
import static org.fruit.alayer.windows.UIARoles.UIAMenuBar;
import static org.fruit.alayer.windows.UIARoles.UIAPane;
import static org.fruit.alayer.windows.UIARoles.UIASeparator;
import static org.fruit.alayer.windows.UIARoles.UIATabControl;
import static org.fruit.alayer.windows.UIARoles.UIAText;
import static org.fruit.alayer.windows.UIARoles.UIAThumb;
import static org.fruit.alayer.windows.UIARoles.UIATitleBar;
import static org.fruit.alayer.windows.UIARoles.UIAToolBar;
import static org.fruit.alayer.windows.UIARoles.UIAToolTip;
import static org.fruit.alayer.windows.UIARoles.UIATree;
import static org.fruit.alayer.windows.UIARoles.UIAWindow;*/
import static org.fruit.alayer.windows.UIARoles.*;
import static org.fruit.alayer.windows.UIATags.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.Pair;
import org.fruit.Util;
import org.fruit.alayer.AWTCanvas;
import org.fruit.alayer.ActionBuildException;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Action;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Role;
import org.fruit.alayer.Shape;
import org.fruit.alayer.State;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Rect;
import org.fruit.alayer.StateBuilder;
import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;
import org.fruit.alayer.Roles;
import org.fruit.alayer.ShapeVisualizer;
import org.fruit.alayer.StateBuildException;
import org.fruit.alayer.StrokePattern;
import org.fruit.alayer.SystemStartException;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.ProcessHandle;
import org.fruit.alayer.windows.GDIScreenCanvas;
import org.fruit.alayer.windows.UIAStateBuilder;
import org.fruit.alayer.windows.UIATags;
import org.fruit.alayer.windows.WinProcess;

public class DefaultProtocol extends AbstractProtocol{

	private StateBuilder builder;
	private boolean faultySequence;

	private final static Pen RedPen = Pen.newPen().setColor(Color.Red).
			setFillPattern(FillPattern.None).setStrokePattern(StrokePattern.Solid).build();

	protected void initialize(Settings settings){
		builder = new UIAStateBuilder(settings.get(ConfigTags.TimeToFreeze));
	}
	
	protected Canvas buildCanvas() { return GDIScreenCanvas.fromPrimaryMonitor(Pen.DefaultPen); }

	protected void beginSequence(){
		faultySequence = false;
	}

	protected void finishSequence(File recordedSequence){}

	protected SUT startSystem() throws SystemStartException{
		try{			
			for(String d : settings().get(ConfigTags.Delete))
				Util.delete(d);

			for(Pair<String, String> fromTo : settings().get(ConfigTags.CopyFromTo))
				Util.copyToDirectory(fromTo.left(), fromTo.right());

			SUT ret = WinProcess.fromExecutable(settings().get(ConfigTags.Executable));
			Util.pause(settings().get(ConfigTags.StartupTime));
			return ret;
		}catch(IOException ioe){
			throw new SystemStartException(ioe);
		}
	}

	protected State getState(SUT system) throws StateBuildException{
		Assert.notNull(system); // by urueda
		State state = builder.apply(system);
		Shape viewPort = state.get(Tags.Shape, null);
		if(viewPort != null){
			//AWTCanvas scrShot = AWTCanvas.fromScreenshot(Rect.from(viewPort.x(), viewPort.y(), viewPort.width(), viewPort.height()), AWTCanvas.StorageFormat.PNG, 1);
			state.set(Tags.ScreenshotPath, this.getStateshot(state)); // by urueda
		}
		Verdict verdict = getVerdict(state);
		state.set(Tags.OracleVerdict, verdict);

		List<Pair<Long, String>> runningProcesses = Util.newArrayList();
		for(ProcessHandle ph : Util.makeIterable(system.get(Tags.ProcessHandles, Collections.<ProcessHandle>emptyList().iterator())))
			runningProcesses.add(Pair.from(ph.pid(), ph.name()));
		state.set(Tags.RunningProcesses, runningProcesses);

		if(state.get(Tags.OracleVerdict).severity() >= settings().get(ConfigTags.FaultThreshold))
			faultySequence = true;
		return state;
	}

	protected Verdict getVerdict(State state){
		Assert.notNull(state);

		if(!state.get(IsRunning, false))
			return new Verdict(1.0, "System is offline! I assume it crashed!");

		if(state.get(Tags.NotResponding, false))
			return new Verdict(0.8, "System is unresponsive! I assume something is wrong!");

		String titleRegEx = settings().get(ConfigTags.SuspiciousTitles);
		// search all widgets for suspicious titles
		for(Widget w : state){
			String title = w.get(Title, "");
			if(title.matches(titleRegEx)){
				Visualizer visualizer = Util.NullVisualizer;
				if(w.get(Tags.Shape, null) != null)
					visualizer = new ShapeVisualizer(RedPen, w.get(Tags.Shape), "Suspicious Title", 0.5, 0.5);
				return new Verdict(1.0, "Discovered suspicious widget title: '" + title + "'.", visualizer);
			}
		}
		return Verdict.OK;
	}

	protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException{
		Assert.notNull(state);
		Set<Action> actions = new HashSet<Action>();	
		StdActionCompiler ac = new AnnotatingActionCompiler();

		// if we have an unwanted process, kill it
		String processRE = settings().get(ConfigTags.ProcessesToKillDuringTest);
		for(Pair<Long, String> process : state.get(Tags.RunningProcesses, Collections.<Pair<Long, String>>emptyList())){
			if(process.right() != null && process.right().matches(processRE)){
				actions.add(ac.killProcessByName(process.right(), 2));
				return actions;
			}
		}

		// if the system is in the background force it into the foreground!
		if(!state.get(Tags.Foreground, true) && system.get(Tags.SystemActivator, null) != null){
			actions.add(ac.activateSystem());
			return actions;
		}

		for(Widget w : state){
			Role role = w.get(Tags.Role, Roles.Widget);

			// left clicks			
			if(w.get(Enabled, true) && !w.get(Blocked, false)){
				if(!Role.isOneOf(role, UIASeparator, UIAToolBar, UIAToolTip, UIAMenuBar, 
						UIAMenu, UIAHeader, UIATabControl, UIAPane, UIATree, UIAWindow, 
						UIATitleBar, UIAThumb, UIAEdit, UIAText)){
					String title = w.get(Title, "");

					if(!title.matches(settings().get(ConfigTags.ClickFilter))){					
						if(Util.hitTest(w, 0.5, 0.5)){
							actions.add(ac.leftClickAt(w));
						}
					}
				}
			}

			// typing
			if(w.get(Enabled, true) && !w.get(Blocked, false)){
				if(Role.isOneOf(role, UIAEdit, UIAText) && w.get(UIATags.UIAIsKeyboardFocusable)){
					if(Util.hitTest(w, 0.5, 0.5)){
						actions.add(ac.clickTypeInto(w, "1234"));
					}
				}
			}
		}

		// if we did not find any actions, then we just hit escape
		if(actions.isEmpty())
			actions.add(ac.hitKey(KBKeys.VK_ESCAPE));

		return actions;
	}

	protected boolean moreActions(State state) {
		return (!settings().get(ConfigTags.StopGenerationOnFault) || !faultySequence) && 
				state.get(Tags.IsRunning, false) && !state.get(Tags.NotResponding, false) &&
				//actionCount() < settings().get(ConfigTags.SequenceLength) &&
				actionCount() <= settings().get(ConfigTags.SequenceLength) && // by urueda
				timeElapsed() < settings().get(ConfigTags.MaxTime);
	}

	protected boolean moreSequences() {	
		//return sequenceCount() < settings().get(ConfigTags.Sequences) &&
		return sequenceCount() <= settings().get(ConfigTags.Sequences) && // by urueda		
				timeElapsed() < settings().get(ConfigTags.MaxTime);
	}
	
    // begin by urueda
    private final int MAX_TEXT_LENGTH = 64;
    private final int LETTER_COUNT = 'z' - 'a' + 1;
    // end by urueda
    
    // by urueda (utility method for random text inputs)
    protected String getRandomText(){
    	Random rd = new Random(System.currentTimeMillis());
    	int textLength = rd.nextInt(MAX_TEXT_LENGTH);
    	StringBuffer sb = new StringBuffer(textLength);
    	for (int i=0; i<textLength; i++){
    		sb.append((char)('a' + rd.nextInt(LETTER_COUNT)));    				
    	}
    	if (sb.length() == 0)
    		sb.append("Text input 123");
    	return sb.toString();
    }
	
}