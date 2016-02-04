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

import static org.fruit.alayer.Tags.IsRunning;
import static org.fruit.alayer.Tags.Title;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.Pair;
import org.fruit.Util;
import org.fruit.alayer.Action;
import org.fruit.alayer.ActionBuildException;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Role;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Shape;
import org.fruit.alayer.ShapeVisualizer;
import org.fruit.alayer.State;
import org.fruit.alayer.StateBuildException;
import org.fruit.alayer.StateBuilder;
import org.fruit.alayer.StrokePattern;
import org.fruit.alayer.SystemStartException;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.devices.ProcessHandle;

import es.upv.staq.testar.NativeLinker;

public class DefaultProtocol extends AbstractProtocol{

	protected State state = null; // by urueda
	
	private StateBuilder builder;
	private boolean faultySequence;

	protected final static Pen RedPen = Pen.newPen().setColor(Color.Red).
			setFillPattern(FillPattern.None).setStrokePattern(StrokePattern.Solid).build(),
							   BluePen = Pen.newPen().setColor(Color.Blue).
			setFillPattern(FillPattern.None).setStrokePattern(StrokePattern.Solid).build();

	protected void initialize(Settings settings){
		//builder = new UIAStateBuilder(settings.get(ConfigTags.TimeToFreeze));
		builder = NativeLinker.getNativeStateBuilder(settings.get(ConfigTags.TimeToFreeze)); // by urueda
	}
	
	protected Canvas buildCanvas() {
		//return GDIScreenCanvas.fromPrimaryMonitor(Pen.DefaultPen);
		return NativeLinker.getNativeCanvas(Pen.DefaultPen); // by urueda
	}

	protected void beginSequence(){
		faultySequence = false;
	}

	protected void finishSequence(File recordedSequence){}

	// by urueda
	private SUT getSUT(String windowTitle) throws SystemStartException{
		List<SUT> suts = null;
		State state; Role role; String title;
		long now = System.currentTimeMillis();
		final double MAX_ENGAGE_TIME = settings().get(ConfigTags.StartupTime).doubleValue();
        do{
          Util.pause(1);
		  suts = NativeLinker.getNativeProcesses();
		  if (suts != null){
			  for (SUT theSUT : suts){
				  state = getState(theSUT);
				  if (state.get(Tags.Foreground)){
					  for (Widget w : state){
						  role = w.get(Tags.Role, null);
						  if (role != null)
							  System.out.println("Role: " + role.toString());
						  if (role != null && Role.isOneOf(role, NativeLinker.getNativeRole_Window())){
							  title = w.get(Tags.Title, null);
							  if (title != null)
								  System.out.println("\ttitle: " + title);
							  if (title != null && title.contains(windowTitle)){
								  System.out.println("SUT -" + windowTitle + "- DETECTED!");
								  return theSUT;
							  }
						  }
					  }
				  }
			  }
		  }
        } while (System.currentTimeMillis() - now < MAX_ENGAGE_TIME);
		throw new SystemStartException("SUT not found!: -" + windowTitle + "-");			
	}
	
	// refactored
	protected SUT startSystem() throws SystemStartException{
		return startSystem(null);
	}

	private final String SUT_WINDOW_TITLE = "SUT_WINDOW_TITLE:"; // by urueda
	
	// by urueda
	protected SUT startSystem(String mustContain) throws SystemStartException{
		try{// refactored from "protected SUT startSystem() throws SystemStartException"
			for(String d : settings().get(ConfigTags.Delete))
				Util.delete(d);
			for(Pair<String, String> fromTo : settings().get(ConfigTags.CopyFromTo))
				Util.copyToDirectory(fromTo.left(), fromTo.right());
			//SUT ret = WinProcess.fromExecutable(settings().get(ConfigTags.Executable));
			//sut = NativeLinker.getNativeSUT(settings().get(ConfigTags.Executable)); // by urueda
			//Util.pause(settings().get(ConfigTags.StartupTime));
		}catch(IOException ioe){
			throw new SystemStartException(ioe);
		} // end refactoring
		String sutString = settings().get(ConfigTags.Executable);
		if (sutString.startsWith("SUT_WINDOW_TITLE:") || mustContain != null)
			return getSUT(mustContain == null ? sutString.substring(SUT_WINDOW_TITLE.length()) : mustContain);
		else
			return NativeLinker.getNativeSUT(settings().get(ConfigTags.Executable));
	}

	@Override
	protected State getState(SUT system) throws StateBuildException{
		Assert.notNull(system); // by urueda
		//State state = builder.apply(system);
		state = builder.apply(system); // by urueda
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

	@Override // by urueda
	protected Verdict getVerdict(State state){
		Assert.notNull(state);

		//-------------------
		// ORACLES FOR FREE
		//-------------------		

		// if the SUT is not running, we assume it crashed
		if(!state.get(IsRunning, false))
			return new Verdict(1.0, "System is offline! I assume it crashed!");

		// if the SUT does not respond within a given amount of time, we assume it crashed
		if(state.get(Tags.NotResponding, false))
			return new Verdict(0.8, "System is unresponsive! I assume something is wrong!");

        //------------------------
		// ORACLES ALMOST FOR FREE
        //------------------------
		
		String titleRegEx = settings().get(ConfigTags.SuspiciousTitles);
		// search all widgets for suspicious titles
		for(Widget w : state){
			String title = w.get(Title, "");
			if(title.matches(titleRegEx)){
				Visualizer visualizer = Util.NullVisualizer;

				// visualize the problematic widget, by marking it with a red box
				if(w.get(Tags.Shape, null) != null)
					visualizer = new ShapeVisualizer(RedPen, w.get(Tags.Shape), "Suspicious Title", 0.5, 0.5);
				return new Verdict(1.0, "Discovered suspicious widget title: '" + title + "'.", visualizer);
			}
		}
		
		// if everything was OK ...
		return Verdict.OK;
	}

	protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException{
		Assert.notNull(state);
		Set<Action> actions = new HashSet<Action>();	

		// create an action compiler, which helps us create actions, such as clicks, drag + drop, typing...
		StdActionCompiler ac = new AnnotatingActionCompiler();

		// if there is an unwanted process running, kill it
		String processRE = settings().get(ConfigTags.ProcessesToKillDuringTest);
		for(Pair<Long, String> process : state.get(Tags.RunningProcesses, Collections.<Pair<Long,String>>emptyList())){
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
		
}