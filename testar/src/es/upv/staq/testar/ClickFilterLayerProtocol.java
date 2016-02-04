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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.Action;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Point;
import org.fruit.alayer.ShapeVisualizer;
import org.fruit.alayer.State;
import org.fruit.alayer.StrokePattern;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.DefaultProtocol;

/**
 * Testing protocol enhancements to ease tester work.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 * Experimental: date type text-fields
 * To be developed: actions ordering
 *
 */

public class ClickFilterLayerProtocol extends DefaultProtocol {

	public static final int WIDGET_DATE_FORMAT = 0473;
	
	public static final Tag[] WHITE_TABU_TAGS = new Tag[]{Tags.Role,Tags.Title};
	public static final Tag[] DATE_FORMAT_TAGS = new Tag[]{Tags.Role,Tags.Title,Tags.Shape}; // experimental (todo: discard non-stable properties like widget position/size)
	
    private boolean displayWhiteTabu = false;
    private boolean whiteTabuMode = false; // true => white, false = tabu
    private boolean ctrlPressed = false, shiftPressed = false;
    
    public static final String PROTOCOL_FILTER_FILE = "ProtocolFilter.idx";
    
    private Set<String> whiteList = new HashSet<String>(),
    					tabuList = new HashSet<String>(),
    					dateList = new HashSet<String>();
    
    private final static Pen WhitePen = Pen.newPen().setColor(Color.LimeGreen).
			setFillPattern(FillPattern.None).setStrokePattern(StrokePattern.Solid).build();
    private final static Pen BlackPen = Pen.newPen().setColor(Color.Black).
			setFillPattern(FillPattern.None).setStrokePattern(StrokePattern.Solid).build();

    /**
     * Constructor.
     */
	public ClickFilterLayerProtocol(){
		super();
		loadLists();
	}
	
	private void saveLists(){
		File f = new File(PROTOCOL_FILTER_FILE);
		try {
			FileOutputStream fs = new FileOutputStream(f);
			BufferedOutputStream bs = new BufferedOutputStream(new GZIPOutputStream(fs));
			ObjectOutputStream stream = new ObjectOutputStream(bs);
			stream.writeObject(whiteList);
			stream.writeObject(tabuList);
			stream.writeObject(dateList);
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadLists(){
		File f = new File(PROTOCOL_FILTER_FILE);
		if (f.exists()){
			ObjectInputStream stream = null;
			try {
				FileInputStream fs = new FileInputStream(f);
				BufferedInputStream bs = new BufferedInputStream(new GZIPInputStream(fs));
				stream = new ObjectInputStream(bs);
				whiteList = (Set<String>) stream.readObject();
				tabuList = (Set<String>) stream.readObject();
				dateList = (Set<String>) stream.readObject();
			} catch (EOFException e) {
				System.out.println(f.getAbsolutePath() + " seems outdated");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
	}
	
    @Override
    protected void keyDown(KBKeys key) {
        super.keyDown(key);
        if (mode() == Modes.Spy){ 
        	if (key == KBKeys.VK_SHIFT)
        		shiftPressed = true;
        	else if (key == KBKeys.VK_CAPS_LOCK)
        		displayWhiteTabu = !displayWhiteTabu;
	    	else if (key == KBKeys.VK_CONTROL)
	    		ctrlPressed = true;
	    	else if (key == KBKeys.VK_D && ctrlPressed && shiftPressed){
	    		manageDateLists();
	    		ctrlPressed = false;
	    		shiftPressed = false;
	    	}
        }
    }

    @Override
    protected void keyUp(KBKeys key) {
    	super.keyUp(key);
        if (mode() == Modes.Spy){ 
	    	if (key == KBKeys.VK_SHIFT)
	    		shiftPressed = false;
	    	else if (key == KBKeys.VK_CONTROL && ctrlPressed){
	    		ctrlPressed = false; whiteTabuMode = shiftPressed;
	    		manageWhiteTabuLists();
	    	}
        }
    }
    
    private Widget getWidgetUnderCursor(){
    	if (state == null) return null;
		Point cursor = mouse.cursor();
        Assert.notNull(cursor);
		return Util.widgetFromPoint(this.state, cursor.x(), cursor.y(), null);
    }

    private void manageWhiteTabuLists(){
    	Widget cursorWidget = getWidgetUnderCursor();
        if (cursorWidget != null) {
        	String widgetID = CodingManager.codifyLimitedTo(cursorWidget,WHITE_TABU_TAGS);
        	if (whiteTabuMode){ // white mode enabled
            	// switch order: tabuList -> rules -> whiteList
            	if (tabuList.contains(widgetID))
            		tabuList.remove(widgetID);
            	else
            		whiteList.add(widgetID);
        		
        	} else{ // tabu mode eneabled
            	// switch order: whiteList -> rules -> tabuList
            	if (whiteList.contains(widgetID))
            		whiteList.remove(widgetID);
            	else
            		tabuList.add(widgetID);        		
        	}
        	saveLists();
		}    	
    }

    private void manageDateLists(){
    	Widget cursorWidget = getWidgetUnderCursor();
        if (cursorWidget != null) {
        	String widgetID = CodingManager.codifyLimitedTo(cursorWidget,DATE_FORMAT_TAGS);
        	if (dateList.contains(widgetID))
        		dateList.remove(widgetID);
        	else
        		dateList.add(widgetID);
        	saveLists();
		}    	
    }
    
    @Override
	protected void visualizeActions(Canvas canvas, State state, Set<Action> actions){
		super.visualizeActions(canvas, state, actions);
    	if(displayWhiteTabu && (mode() == Modes.Spy || mode() == Modes.GenerateDebug) && settings().get(ConfigTags.VisualizeActions)){
    		String wid;
    		Visualizer v;
    		for(Widget w : state){
				wid = CodingManager.codifyLimitedTo(w,WHITE_TABU_TAGS);
				v = null;
				if (whiteList.contains(wid))
					v = new ShapeVisualizer(WhitePen, w.get(Tags.Shape), "", 0.5, 0.5);
				else if (tabuList.contains(wid))
					v = new ShapeVisualizer(BlackPen, w.get(Tags.Shape), "", 0.5, 0.5);
				wid = CodingManager.codifyLimitedTo(w,DATE_FORMAT_TAGS);
				if (dateList.contains(wid))
					v = new ShapeVisualizer(BluePen, w.get(Tags.Shape), "", 0.5, 0.5);					
				if (v != null)
					v.run(state, canvas, Pen.IgnorePen);
    		}
		}    	
	}
    
    protected boolean blackListed(Widget w){
		return tabuList.contains(CodingManager.codifyLimitedTo(w,WHITE_TABU_TAGS));  	
    }

    protected boolean whiteListed(Widget w){
    	return whiteList.contains(CodingManager.codifyLimitedTo(w,WHITE_TABU_TAGS));
    }
    
    protected int widgetFormat(Widget w){
    	if (dateList.contains(CodingManager.codifyLimitedTo(w,DATE_FORMAT_TAGS)))
    		return WIDGET_DATE_FORMAT;
    	else
    		return 0;
    }
    
}