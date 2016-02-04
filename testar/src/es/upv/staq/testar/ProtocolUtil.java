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

package es.upv.staq.testar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.Finder;
import org.fruit.alayer.Point;
import org.fruit.alayer.Position;
import org.fruit.alayer.Role;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.ActionRoles;

/**
 * Utility enhancements for the testing protocol.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */

public class ProtocolUtil {

	// --------------------------------------
	// AUTOMATED TEXTS FOR TEXT-INPUT FIELDS
	// --------------------------------------
	
	// random text inputs
    private static final int MAX_TEXT_LENGTH = 64,
    						 LETTER_COUNT = 'z' - 'a' + 1;
    
    private static final String[] DATE_SAMPLES = new String[]{
    	"11-27-2015", "11/27/2015", "27/11/2015", "27-11-2015",
    	"00/00/1900", "12-12-7357", "2015", "November", "1st January",
    	"31 December, 2015", "15/06/2016", "01-01-01", "2001 February"
    };
    
    // utility method for random text inputs
    public static String getRandomText(int widgetFormat){
    	if (widgetFormat == ClickFilterLayerProtocol.WIDGET_DATE_FORMAT)    		
    		return DATE_SAMPLES[new Random(System.currentTimeMillis()).nextInt(DATE_SAMPLES.length)];
    	else {
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

    // -----------------------
    // ACTIONS CATEGORIZATION
    // -----------------------
    
    // filter actions that do type
    public static Set<Action> getTypingActions(Set<Action> actions){
    	Set<Action> typingActions = new HashSet<Action>();
    	Role r;
    	for (Action a : actions){
    		r = a.get(Tags.Role, null);
    		if (r != null && r.name().equals(ActionRoles.ClickTypeInto.name()))
    			typingActions.add(a);
    	}
    	return typingActions;
    }
    
    // get the target widgets of typing actions    
    public static HashMap<Widget,Action> getTypeableWidgets(State state, Set<Action> actions){
    	HashMap<Widget,Action> widgets = new HashMap<Widget,Action>();
    	List<Finder> targets;
		Set<Action> typingActions = getTypingActions(actions); 
		for (Action a : typingActions){
			targets = a.get(Tags.Targets,null);
			if (targets != null){
				widgets.put(targets.get(0).apply(state),a); // typing actions has exactly one target
			}
		}
		return widgets;
    }
    
    public static Set<Action> getSlides(Set<Action> actions){
    	Set<Action> slideActions = new HashSet<Action>();
    	Position[] slider;
    	for (Action a : actions){
    		slider = a.get(Tags.Slider, null);
    		if (slider != null)
    			slideActions.add(a);
    	}
    	if (slideActions.isEmpty())
    		return null;
    	else
    		return slideActions;
    }
	
    // ----------
    // AUXILIARY
    // ----------
    
    private static String getSlideID(State state, Position[] slide){
		return (slide[0].apply(state)).toString() + // from
			   (slide[1].apply(state)).toString(); // to
    }
    
    // ------------------------------------
    // TEXT-INPUT FIELDS FILLING FOR FORMS
    // ------------------------------------
    
    // typing actions prioritizing
	private static final int WIDGET_TYPING_COUNTDOWN_THRESHOULD = 100; // how many executed actions to wait for before retyping takes place?
    private static HashMap<String,Integer> widgetsTypingHistory =
    		new HashMap<String, 	// widget-id
    					Integer>(); // 0 => widget to be typed; >0 => widget typed with count-down on the #executed_ations for retyping 
    private static Tag<?>[] typingWidgetsAbstraction = new Tag<?>[]{Tags.Role,Tags.Shape};
    
    // slides actions prioritizing (to reveal extra text-input fields in forms)
    private static HashMap<String,Integer> slidingHistory =
    		new HashMap<String, 	// slide from->to 
    					Integer>(); // 0 => slide-to missing; >0 => slided with count-down on the #executed_ations for re-sliding
    
    // prioritize typing actions for text inputs dependent behaviors
    public static Set<Action> filterFormActions(State state, Set<Action> actions){
    	Set<Action> returnActions = new HashSet<Action>();
    	String widgetID;
    	Integer status;
    	HashMap<Widget,Action> typeableWidgets = getTypeableWidgets(state,actions); 
    	for (Widget w : typeableWidgets.keySet()){
    		widgetID = CodingManager.codifyLimitedTo(w,typingWidgetsAbstraction);
    		status = widgetsTypingHistory.get(widgetID);
    		if (status == null){ // new typeable widget?
    			status = new Integer(0);
    			widgetsTypingHistory.put(widgetID,status);
    		}
			if (status.intValue() <= 0) // widget to be typed
				returnActions.add(typeableWidgets.get(w));
    	}
    	if (returnActions.isEmpty()){
    		String slideS;
    		Point from, to;
    		Position[] slide;
    		Set<Action> slides = getSlides(actions); 
    		if (slides != null){
	    		for (Action s : slides){
	    			slide = s.get(Tags.Slider);
	    			slideS = getSlideID(state,slide);
	    			status = slidingHistory.get(slideS);
	    			if (status == null){ // new slide?
	    				status = new Integer(0);
	    				slidingHistory.put(slideS,status);
	    			}
	    			if (status.intValue() <= 0) // slide to performed
	    				returnActions.add(s);
	    		}
    		}
    		if (returnActions.isEmpty())
    			return actions;
    	}
    	return returnActions;
    }    
    
    // update typing actions management
    public static void updateFormActions(State state, Action selectedAction){
		Set<Action> as = new HashSet<Action>();
		as.add(selectedAction);
		HashMap<Widget,Action> tw = getTypeableWidgets(state,as);
		if (tw.size() == 1){ // widget typed?
			widgetsTypingHistory.put(CodingManager.codifyLimitedTo(tw.keySet().iterator().next(),typingWidgetsAbstraction),
									new Integer(WIDGET_TYPING_COUNTDOWN_THRESHOULD + 1));
		}
		Set<Action> slides = getSlides(as);
		if (slides != null) // slide performed=
			slidingHistory.put(getSlideID(state,slides.iterator().next().get(Tags.Slider)),
							   new Integer(WIDGET_TYPING_COUNTDOWN_THRESHOULD + 1));
		Integer cd;
		for (String w : widgetsTypingHistory.keySet()){
			cd = widgetsTypingHistory.get(w);
			widgetsTypingHistory.put(w,new Integer(cd--));
		}
		for (String s : slidingHistory.keySet()){
			cd = slidingHistory.get(s);
			slidingHistory.put(s,new Integer(cd--));
		}
    }
    
    // ---------------------------
    // ANY MORE ENHANCEMENTS? ...
    // ---------------------------
    
}
