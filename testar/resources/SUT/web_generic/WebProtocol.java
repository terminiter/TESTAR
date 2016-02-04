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
 
/**
 *  @author (base) Sebastian Bauersfeld
 *  Web protocol (generic) authors: urueda, fraalpe2, mimarmu1
 *  @author Urko Rueda Molina (protocol refactor & cleanup)
 */
import java.io.File;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import org.fruit.Assert;
import org.fruit.Drag; // by urueda
import org.fruit.Pair;
import org.fruit.Util;
import org.fruit.alayer.Action;
import org.fruit.alayer.ActionBuildException;
import org.fruit.alayer.ActionFailedException;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.AbsolutePosition;
import org.fruit.alayer.Point;
import org.fruit.alayer.Role;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Shape;
import org.fruit.alayer.ShapeVisualizer;
import org.fruit.alayer.State;
import org.fruit.alayer.StateBuildException;
import org.fruit.alayer.StrokePattern;
import org.fruit.alayer.SystemStartException;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.CompoundAction;
import org.fruit.alayer.actions.KeyDown;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.actions.Type;
import org.fruit.alayer.devices.KBKeys;

import static org.fruit.monkey.ConfigTags.*;

import es.upv.staq.testar.ClickFilterLayerProtocol; 
import es.upv.staq.testar.NativeLinker;

import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.fruit.alayer.Tags;

import static org.fruit.alayer.Tags.NotResponding;
import static org.fruit.alayer.Tags.IsRunning;
import static org.fruit.alayer.Tags.RunningProcesses;
import static org.fruit.alayer.Tags.SystemActivator;
import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Title;
import static org.fruit.alayer.Tags.Foreground;
import static org.fruit.alayer.Tags.Enabled;

public class WebProtocol extends ClickFilterLayerProtocol {
	
	static double browser_toolbar_filter;
		
	static Role webController;
	static Role webText;
	
	private static boolean firefox; // we expect Mozilla Firefox or Microsoft Internet Explorer (for more browsers code must be changed!)
	
	static double scrollArrowSize = 36; // sliding arrows (iexplorer)
	static double scrollThick = 16; //scroll thickness (iexplorer)
	
	/** 
	 * Called once during the life time of the Rogue User
	 * This method can be used to perform initial setup work
	 * @param   settings   the current Rogue User settings as specified by the user.
	 */
	protected void initialize(Settings settings){
		
		super.initialize(settings);
		initBrowser();
		
	}
	
	// check whether we use Internet Explorer or Firefox (other browser support must be coded)
	private void initBrowser(){
		String sutPath = settings().get(ConfigTags.Executable);
		firefox = !sutPath.contains("iexplore.exe");
		if(firefox)
			setFilterToFirefox();
		else
			setFilterToExplorer();
	}
	
	private void setFilterToExplorer(){
		webController = NativeLinker.getNativeRole("UIADataItem");
		webText = NativeLinker.getNativeRole("UIAText");
	}
	
	private void setFilterToFirefox(){
		webController = NativeLinker.getNativeRole("UIACustomControl");
		webText = NativeLinker.getNativeRole("UIAEdit");
	}	
	
	/**
	 * This method is invoked each time the Rogue User starts to generate a new sequence
	 */
	protected void beginSequence(){
		
		super.beginSequence();
		
	}
	
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
		
        SUT sut = super.startSystem();

        if (mode() == Modes.Generate)
        	Util.pause(settings().get(ConfigTags.StartupTime));

        return sut;
        
	}

	/**
	 * This method is called when the Rogue User requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine. The state should have attached an oracle 
	 * (TagName: <code>Tags.OracleVerdict</code>) which describes whether the 
	 * state is erroneous and if so why.
	 * @return  the current state of the SUT with attached oracle.
	 */
	protected State getState(SUT system) throws StateBuildException{
		
		State state = super.getState(system);

        for(Widget w : state){
            Role role = w.get(Tags.Role, Roles.Widget);
            if(Role.isOneOf(role, new Role[]{NativeLinker.getNativeRole("UIAToolBar")}))
            	browser_toolbar_filter = w.get(Tags.Shape,null).y() + w.get(Tags.Shape,null).height();
        }
		
		return state;
		
	}

	/**
	 * This is a helper method used by the default implementation of <code>buildState()</code>
	 * It examines the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	protected Verdict getVerdict(State state){
		
		Verdict verdict = super.getVerdict(state); // by urueda
		// system crashes, non-responsiveness and suspicious titles automatically detected!
		
		//-----------------------------------------------------------------------------
		// MORE SOPHISTICATED ORACLES CAN BE PROGRAMMED HERE (the sky is the limit ;-)
        //-----------------------------------------------------------------------------

		// ... YOU MAY WANT TO CHECK YOUR CUSTOM ORACLES HERE ...
		
		return verdict;

	}

	/**
	 * This method is used by the Rogue User to determine the set of currently available actions.
	 * You can use the SUT's current state, analyze the widgets and their properties to create
	 * a set of sensible actions, such as: "Click every Button which is enabled" etc.
	 * The return value is supposed to be non-null. If the returned set is empty, the Rogue User
	 * will stop generation of the current action and continue with the next one.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @return  a set of actions
	 */
	protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException{
		
		Set<Action> actions = super.deriveActions(system,state); // by urueda
		// unwanted processes, force SUT to foreground, ... actions automatically derived!

		// create an action compiler, which helps us create actions, such as clicks, drag&drop, typing ...
		StdActionCompiler ac = new AnnotatingActionCompiler();
		
		//----------------------
		// BUILD CUSTOM ACTIONS
		//----------------------

		// iterate through all widgets
		for(Widget w : state){
			if(w.get(Enabled, true) && !w.get(Blocked, false)){ // only consider enabled and non-blocked widgets

				if (!blackListed(w)){  // do not build actions for tabu widgets  

					// create left clicks
					if(whiteListed(w) || isClickable(w))
						actions.add(ac.leftClickAt(w));
	
					// type into text boxes
					if(isTypeable(w)){
						actions.add(ac.clickTypeInto(w, this.getRandomText(widgetFormat(w))));
					}
					
					// create double left click
					if(isDoubleClickable(w)){
						if(firefox)
							actions.add(ac.leftDoubleClickAt(w));
						else
							actions.add(ac.dropDownAt(w));
					}
					
					// slides
					Drag[] drags = null;
	                if((drags = w.scrollDrags(scrollArrowSize,scrollThick)) != null){
						for (Drag drag : drags){
							actions.add(ac.dragFromTo(
								new AbsolutePosition(Point.from(drag.getFromX(),drag.getFromY())),
								new AbsolutePosition(Point.from(drag.getToX(),drag.getToY()))
							));
						}
	                }
	                
				}

			}
		}

		//----------------------------------
		// THERE MUST ALMOST BE ONE ACTION!
		//----------------------------------

		// if we did not find any actions, then we just hit escape, maybe that works ;-)
		if(actions.isEmpty())
			actions.add(ac.hitKey(KBKeys.VK_ESCAPE));
		
		return actions;
		
	}

	private boolean isClickable(Widget w){
        Role role = w.get(Tags.Role, Roles.Widget);
        if(!Role.isOneOf(role, NativeLinker.getNativeUnclickable())){
            String title = w.get(Title, "");
            String clickFilter = settings().get(ClickFilter);
            if(!title.matches(clickFilter) || clickFilter.equals("")){
                Shape shape = w.get(Tags.Shape,null);
                double hit = 0.5;                                        
                if(Util.hitTest(w, hit, hit)){
                    if (shape != null && shape.y() > browser_toolbar_filter)
                    	return true;
                }
            }
        }
        return false;
	} 

	private boolean isTypeable(Widget w){
		Role r = w.get(Tags.Role, null);
        Boolean b = NativeLinker.isNativeTypeable(w);
        if (b != null && b.booleanValue() &&
        	r != null && Role.isOneOf(r, NativeLinker.getNativeRole("UIAPane"))){	        
        	if(Util.hitTest(w, 0.5, 0.5)){				
				Shape shape = w.get(Tags.Shape,null);
				if (shape != null && shape.y() > browser_toolbar_filter)
					return true;
			}        	
        }
		return false;
	}
	
	private boolean isDoubleClickable(Widget w){
		Role role = w.get(Tags.Role, Roles.Widget);
		String title = w.get(Title, "");
		String clickFilter = settings().get(ClickFilter);			
		if(!title.matches(clickFilter) || clickFilter.equals("")){
			role = w.get(Tags.Role);
			if (Role.isOneOf(role,webText)){
				Widget wParent = w.parent();
				Role roleP = wParent.get(Tags.Role);
				if (Role.isOneOf(roleP,webController)){
			        Shape shape = w.get(Tags.Shape,null);
	            	if(Util.hitTest(w, 0.5, 0.5) &&
					   (shape != null) && (shape.y() > browser_toolbar_filter))
					   return true;
				}
			}
		}
		return false;
	}	
	
	/**
	 * Select one of the possible actions (e.g. at random)
	 * @param state the SUT's current state
	 * @param actions the set of available actions as computed by <code>buildActionsSet()</code>
	 * @return  the selected action (non-null!)
	 */
	protected Action selectAction(State state, Set<Action> actions){
		
		return super.selectAction(state, actions);
		
	}

	/**
	 * Execute the selected action.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @param action the action to execute
	 * @return whether or not the execution succeeded
	 */
	protected boolean executeAction(SUT system, State state, Action action){
		
		return super.executeAction(system, state, action);
		
	}
	
	/**
	 * The Rogue User uses this method to determine when to stop the generation of actions for the
	 * current sequence. You could stop the sequence's generation after a given amount of executed
	 * actions or after a specific time etc.
	 * @return  if <code>true</code> continue generation, else stop
	 */
	protected boolean moreActions(State state) {
		
		return super.moreActions(state);
		
	}


	/** 
	 * This method is invoked each time after the Rogue User finished the generation of a sequence.
	 */
	protected void finishSequence(File recordedSequence){
		
		super.finishSequence(recordedSequence);
		
	}


	/**
	 * The Rogue User uses this method to determine when to stop the entire test.
	 * You could stop the test after a given amount of generated sequences or
	 * after a specific time etc.
	 * @return  if <code>true</code> continue test, else stop	 */
	protected boolean moreSequences() {
		
		return super.moreSequences();
		
	}
		
}