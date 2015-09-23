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

import static org.fruit.alayer.Tags.*;

import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.fruit.Assert;
import org.fruit.UnProc;
import org.fruit.Util;
import org.fruit.alayer.AWTCanvas;
import org.fruit.alayer.ActionBuildException;
import org.fruit.alayer.ActionFailedException;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Action;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Finder;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Point;
import org.fruit.alayer.Rect;
import org.fruit.alayer.Role;
import org.fruit.alayer.Shape;
import org.fruit.alayer.State;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Taggable;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Visualizer;
import org.fruit.alayer.Widget;
import org.fruit.alayer.Roles;
import org.fruit.alayer.StateBuildException;
import org.fruit.alayer.SystemStartException;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.WidgetNotFoundException;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.BriefActionRolesMap;
import org.fruit.alayer.actions.NOP;
import org.fruit.alayer.devices.AWTMouse;
import org.fruit.alayer.devices.Mouse;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.MouseButtons;
import org.fruit.monkey.Main.LogLevel;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import es.upv.staq.testar.CodingManager; 
import es.upv.staq.testar.ScreenshotManager;
import es.upv.staq.testar.graph.Grapher;

import static org.fruit.monkey.Main.logln;
import static org.fruit.monkey.Main.log;

public abstract class AbstractProtocol implements NativeKeyListener, NativeMouseListener, UnProc<Settings> {
	
	public static enum Modes{
		Spy,
		GenerateManual, // by urueda
		Generate, GenerateDebug, Quit, View, AdhocTest, Replay, ReplayDebug;
	}

	Set<KBKeys> pressed = EnumSet.noneOf(KBKeys.class);	
	private Settings settings;
	private Modes mode;
	protected Mouse mouse = AWTMouse.build();
	private boolean saveStateSnapshot,
					markParentWidget = false; // by urueda
	int actionCount, sequenceCount;
	double startTime;
	
	// begin by urueda
	private ScreenshotManager scrshotManager;	
	private Object[] userEvent = null;
	private Action lastExecutedAction = null;
	private ServerSocket adhocTestServerSocket = null;
	private Socket adhocTestSocket = null;
	private BufferedReader adhocTestServerReader = null;
	private BufferedWriter adhocTestServerWriter = null;
	// end by urueda
	
	// by urueda
	private void startAdhocServer() {
		new Thread(){
			public void run(){
				int port = 47357;
				try {
					adhocTestServerSocket = new ServerSocket(port);
					System.out.println("AdhocTest Server started @" + port);
					adhocTestSocket = adhocTestServerSocket.accept();
					System.out.println("AdhocTest Client engaged");
					adhocTestServerReader = new BufferedReader(new InputStreamReader(adhocTestSocket.getInputStream()));
					adhocTestServerWriter = new BufferedWriter(new OutputStreamWriter(adhocTestSocket.getOutputStream()));
				} catch(Exception e){
					stopAdhocServer();
				}
			}
		}.start();
	}
	
	// by urueda
	private void stopAdhocServer(){
		if (adhocTestServerSocket != null){
			try {
				if (adhocTestServerReader != null)
						adhocTestServerReader.close();
				if (adhocTestServerWriter != null)
					adhocTestServerWriter = null;
				if (adhocTestSocket != null)
					adhocTestSocket.close();
				adhocTestServerSocket.close();
				adhocTestServerSocket = null;				
				System.out.println(" AdhocTest Server sttopped  " );		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void keyDown(KBKeys key){
		pressed.add(key);

		// state snapshot
		if(key == KBKeys.VK_UP && pressed.contains(KBKeys.VK_SHIFT))
			saveStateSnapshot = true;

		// change mode with shift + right (forward)
		else if(key == KBKeys.VK_RIGHT && pressed.contains(KBKeys.VK_SHIFT))
			nextMode(true);

		// change mode with shift + left (backward)
		else if(key == KBKeys.VK_LEFT && pressed.contains(KBKeys.VK_SHIFT))
			nextMode(false);

		// quit with shift + down
		else if(key == KBKeys.VK_DOWN && pressed.contains(KBKeys.VK_SHIFT)){
			logln("User requested to stop monkey!", LogLevel.Info);
			mode = Modes.Quit;
			stopAdhocServer(); // by urueda
		}

		// toggle action visualization
		else if(key == KBKeys.VK_1 && pressed.contains(KBKeys.VK_SHIFT))
			settings().set(ConfigTags.VisualizeActions, !settings().get(ConfigTags.VisualizeActions));		

		// toggle widget mark visualization
		else if(key == KBKeys.VK_2 && pressed.contains(KBKeys.VK_SHIFT))
			settings().set(ConfigTags.DrawWidgetUnderCursor, !settings().get(ConfigTags.DrawWidgetUnderCursor));		

		// toggle widget info visualization
		else if(key == KBKeys.VK_3 && pressed.contains(KBKeys.VK_SHIFT))
			settings().set(ConfigTags.DrawWidgetInfo, !settings().get(ConfigTags.DrawWidgetInfo));		
		
		// begin by urueda (method structure changed from if* to <if, elseif*, else>)
		
		else if (key == KBKeys.VK_ENTER && pressed.contains(KBKeys.VK_SHIFT)){
			startAdhocServer();
			mode = Modes.AdhocTest;
			logln("'" + mode + "' mode active.", LogLevel.Info);
		}
		
		else if (!pressed.contains(KBKeys.VK_SHIFT) &&
				mode() == Modes.GenerateManual && userEvent == null){
			System.out.println("USER_EVENT key_down! " + key.toString());
			userEvent = new Object[]{key}; // would be ideal to set it up at keyUp
		}
		
		// end by urueda
		
		markParentWidget = pressed.contains(KBKeys.VK_CONTROL);	// by urueda
	}

	protected void keyUp(KBKeys key){
		pressed.remove(key);
		markParentWidget = pressed.contains(KBKeys.VK_CONTROL);	// by urueda
	}
	
	protected void mouseDown(MouseButtons btn, double x, double y){}
	
	protected void mouseUp(MouseButtons btn, double x, double y){
		// begin by urueda
		if (mode() == Modes.GenerateManual && userEvent == null){
			System.out.println("USER_EVENT mouse_up!");
		    userEvent = new Object[]{
	        	btn,
	        	new Double(x),
	       		new Double(y)
			};
		}
		// end by urueda		
	}

	public final void nativeKeyPressed(NativeKeyEvent e) {
		for(KBKeys key : KBKeys.values())
			if(key.code() == e.getKeyCode())
				keyDown(key);
	}

	public final void nativeKeyReleased(NativeKeyEvent e) {
		for(KBKeys key : KBKeys.values())
			if(key.code() == e.getKeyCode())
				keyUp(key);
	}

	public final void nativeMouseClicked(NativeMouseEvent arg0) {}

	public final void nativeMousePressed(NativeMouseEvent arg0) {
		if(arg0.getButton() == 1)
			mouseDown(MouseButtons.BUTTON1, arg0.getX(), arg0.getY());
		else if(arg0.getButton() == 2)
			mouseDown(MouseButtons.BUTTON3, arg0.getX(), arg0.getY());
		else if(arg0.getButton() == 3)
			mouseDown(MouseButtons.BUTTON2, arg0.getX(), arg0.getY());
	}

	public final void nativeMouseReleased(NativeMouseEvent arg0) {
		if(arg0.getButton() == 1)
			mouseUp(MouseButtons.BUTTON1, arg0.getX(), arg0.getY());
		else if(arg0.getButton() == 2)
			mouseUp(MouseButtons.BUTTON3, arg0.getX(), arg0.getY());
		else if(arg0.getButton() == 3)
			mouseUp(MouseButtons.BUTTON2, arg0.getX(), arg0.getY());
	}

	public final void nativeKeyTyped(NativeKeyEvent e) {}
	public synchronized Modes mode(){ return mode; }

	private synchronized void nextMode(boolean forward){

		if(forward){
			switch(mode){
			//case Spy: mode = Modes.Generate; break;
			case Spy: userEvent = null; mode = Modes.GenerateManual; break; // by urueda
			case GenerateManual: mode = Modes.Generate; break; // by urueda
			case Generate: mode = Modes.GenerateDebug; break;
			case GenerateDebug: mode = Modes.Spy; break;
			case AdhocTest: mode = Modes.Spy; stopAdhocServer(); break; // by urueda
			case Replay: mode = Modes.ReplayDebug; break;
			case ReplayDebug: mode = Modes.Replay; break;
			default: break;
			}		
		}else{
			switch(mode){
			case Spy: mode = Modes.GenerateDebug; break;
			//case Generate: mode = Modes.Spy; break;
			case GenerateManual: mode = Modes.Spy; break; // by urueda
			case Generate: userEvent = null; mode = Modes.GenerateManual; break; // by urueda
			case GenerateDebug: mode = Modes.Generate; break;
			case AdhocTest: mode = Modes.Spy; stopAdhocServer(); break; // by urueda
			case Replay: mode = Modes.ReplayDebug; break;
			case ReplayDebug: mode = Modes.Replay; break;
			default: break;
			}			
		}
		logln("'" + mode + "' mode active.", LogLevel.Info);
	}

	protected final double timeElapsed(){ return Util.time() - startTime; }
	protected final Settings settings(){ return settings; }
	protected void beginSequence() {}
	protected void finishSequence(File recordedSequence) {}
	protected abstract SUT startSystem() throws SystemStartException;
	protected abstract State getState(SUT system) throws StateBuildException;
	protected abstract Set<Action> deriveActions(SUT system, State state) throws ActionBuildException;
	protected abstract Canvas buildCanvas();
	protected abstract boolean moreActions(State state);
	protected abstract boolean moreSequences();
	protected final int actionCount(){ return actionCount; }
	protected final int sequenceCount(){ return sequenceCount; }
	protected void initialize(Settings settings){}
	
	// begin by urueda
	protected LinkedHashMap<String,Color> ancestorsMarkingColors = new LinkedHashMap<String,Color>(){{
		put("1. ===   black",	Color.from(  0,   0,   0, 255));
		put("2. ===   white",	Color.from(255, 255, 255, 255));
		put("3. ===     red",	Color.from(255,   0,   0, 255));
		put("4. ===    blue",	Color.from(  0,   0, 255, 255));
		put("5. ===  yellow",	Color.from(255, 255,   0, 255));
		put("6. ===    cyan",	Color.from(  0, 255, 255, 255));
		put("7. === magenta",	Color.from(255,   0, 255, 255));
		put("8. ===   green",	Color.from(  0, 128,   0, 255));
		put("9. === bluesky",	Color.from(  0, 128, 255, 255));
		put("a. ===  purple",	Color.from(128,   0, 128, 255));
		put("b. ===  orange",	Color.from(255, 128,   0, 255));
		put("c. ===   brown",	Color.from(139,  69,  19, 255));
		put("d. ===  silver",	Color.from(192, 192, 192, 255));
		put("e. ===    navy",	Color.from(  0,   0, 128, 255));
		put("f. ===    gray",	Color.from(128, 128, 128, 255));
	}};
	// end by urueda
	
	// by urueda
	private int markParents(Canvas canvas,Widget w, Iterator<String> it, int lvl){
		Widget parent;
		if (!it.hasNext() || // marking colors exhausted
				(parent = w.parent()) == null)
			return lvl;		
		int margin = 4;
		String colorS = it.next();
		Pen mark = Pen.newPen().setColor(ancestorsMarkingColors.get(colorS))
				.setFillPattern(FillPattern.Stroke).build();						
		Shape shape = parent.get(Tags.Shape, null);
		try{
			shape = Rect.from(shape.x()+lvl*margin, shape.y()+lvl*margin,
				          	  shape.width()-lvl*margin*2, shape.height()-lvl*margin*2);
		}catch(java.lang.IllegalArgumentException e){};
		shape.paint(canvas, mark);
		
		System.out.println("\tAncestor(" + colorS + "): " + w.getRepresentation("\t\t"));
		
		return markParents(canvas,parent,it,lvl+1);
	}
	
	private synchronized void visualizeState(Canvas canvas, State state){
		if((mode() == Modes.Spy || mode() == Modes.ReplayDebug) && settings().get(ConfigTags.DrawWidgetUnderCursor)){
			Point cursor = mouse.cursor();
			Widget cursorWidget = Util.widgetFromPoint(state, cursor.x(), cursor.y(), null);

			if(cursorWidget != null){
				Shape cwShape = cursorWidget.get(Tags.Shape, null);
				if(cwShape != null){
					
					//Pen mark = Pen.newPen().setColor(Color.from(0, 255, 0, 100)).setFillPattern(FillPattern.Solid).build();
					Pen mark = Pen.newPen().setColor(Color.from(0, 255, 0, 100)).setFillPattern(FillPattern.Stroke).build(); // by urueda (RDP dev. environment performance)
					cwShape.paint(canvas, mark);

					Pen rpen = Pen.newPen().setColor(Color.Red).build();
					Pen apen = Pen.newPen().setColor(Color.Black).build();
					Pen wpen = Pen.newPen().setColor(Color.from(255, 255, 255, 170)).setFillPattern(FillPattern.Solid).build();

					canvas.text(rpen, cwShape.x(), cwShape.y(), 0, "Role: " + cursorWidget.get(Role, Roles.Widget).toString());
					canvas.text(rpen, cwShape.x(), cwShape.y() - 20, 0, Util.indexString(cursorWidget));

					// begin by urueda
					if (markParentWidget){
						System.out.println("Parents of: " + cursorWidget.get(Tags.Title));
						int lvls = markParents(canvas,cursorWidget,ancestorsMarkingColors.keySet().iterator(),0);
						if (lvls > 0){
							Shape legendShape = repositionShape(canvas,Rect.from(cursor.x(), cursor.y(), 110, lvls*25));
							canvas.rect(wpen, legendShape.x(), legendShape.y(), legendShape.width(), legendShape.height());
							canvas.rect(apen, legendShape.x(), legendShape.y(), legendShape.width(), legendShape.height());
							int shadow = 2;
							String l;
							Iterator<String> it = ancestorsMarkingColors.keySet().iterator();
							for (int i=0; i<lvls; i++){
								l = it.next();
								Pen lpen = Pen.newPen().setColor(ancestorsMarkingColors.get(l)).build();
								canvas.text(lpen, legendShape.x() - shadow, legendShape.y() - shadow + i*25, 0, l);
								canvas.text(lpen, legendShape.x() + shadow, legendShape.y() - shadow + i*25, 0, l);
								canvas.text(lpen, legendShape.x() + shadow, legendShape.y() + shadow + i*25, 0, l);
								canvas.text(lpen, legendShape.x() - shadow, legendShape.y() + shadow + i*25, 0, l);
								canvas.text(apen, legendShape.x()         , legendShape.y() + i*25         , 0, l);
							}
						}
					}
					int maxAncestorsPerLine = 5;
					double widgetInfoW = 550;
					double widgetInfoH = (Util.size(cursorWidget.tags()) +
										  Util.size(Util.ancestors(cursorWidget)) / maxAncestorsPerLine)
										  * 25;
					cwShape = calculateWidgetInfoShape(canvas,cwShape, widgetInfoW, widgetInfoH);
					// end by urueda
					
					if(settings().get(ConfigTags.DrawWidgetInfo)){
						//canvas.rect(wpen, cwShape.x(), cwShape.y() - 20, 550, Util.size(cursorWidget.tags()) * 25);
						//canvas.rect(apen, cwShape.x(), cwShape.y() - 20, 550, Util.size(cursorWidget.tags()) * 25);
						// begin by urueda
						canvas.rect(wpen, cwShape.x(), cwShape.y() - 20, widgetInfoW, widgetInfoH);
						canvas.rect(apen, cwShape.x(), cwShape.y() - 20, widgetInfoW, widgetInfoH);
						// end by urueda
						
						canvas.text(rpen, cwShape.x(), cwShape.y(), 0, "Role: " + cursorWidget.get(Role, Roles.Widget).toString());
						canvas.text(rpen, cwShape.x(), cwShape.y() - 20, 0, Util.indexString(cursorWidget));
						int pos = 20;
						StringBuilder sb = new StringBuilder();
						sb.append("Ancestors: ");

						//for(Widget p : Util.ancestors(cursorWidget))
						//	sb.append("::").append(p.get(Role, Roles.Widget));							
						//canvas.text(apen, cwShape.x(), cwShape.y() + (pos+=20), 0, sb.toString());
						// begin by urueda (fix too many ancestors)
						int i=0;
						for(Widget p : Util.ancestors(cursorWidget)){
							sb.append("::").append(p.get(Role, Roles.Widget));
							i++;
							if (i > maxAncestorsPerLine){
								canvas.text(apen, cwShape.x(), cwShape.y() + (pos+=20), 0, sb.toString());
								i=0;
								sb = new StringBuilder();
								sb.append("\t");
							}
						}
						if (i > 0)
							canvas.text(apen, cwShape.x(), cwShape.y() + (pos+=20), 0, sb.toString());
						// end by urueda
						
						pos += 20;
						for(Tag<?> t : cursorWidget.tags()){
							canvas.text((t.equals(Tags.Title) || t.equals(Tags.Role)) ? rpen : apen, cwShape.x(), cwShape.y() + (pos+=20), 0, t.name() + ":   " + Util.abbreviate(Util.toString(cursorWidget.get(t)), 50, "..."));
							// begin by urueda (multi-line display without abbreviation)
							/*final int MAX_TEXT = 50;
							String text = Util.abbreviate(Util.toString(cursorWidget.get(t)), Integer.MAX_VALUE, "NO_SENSE");
							int fragment = 0, limit;
							while (fragment < text.length()){
								limit = fragment + MAX_TEXT > text.length() ? text.length() : fragment + MAX_TEXT;
								canvas.text((t.equals(Tags.Title) || t.equals(Tags.Role)) ? rpen : apen, cwShape.x(), cwShape.y() + (pos+=20), 0, t.name() + ":   " +
									text.substring(fragment,limit));
								fragment = limit;
							}*/
							// end by urueda
						}
					}
				}
			}
		}
	}
	
	// by urueda
	private double[] calculateOffset(Canvas canvas, Shape shape){
		return new double[]{
			canvas.x() + canvas.width() - (shape.x() + shape.width()),
			canvas.y() + canvas.height() - (shape.y() + shape.height())
		};
	}
	
	// by urueda
	private Shape calculateInnerShape(Shape shape, double[] offset){
		if (offset[0] > 0 && offset[1] > 0)
			return shape;
		else{
			double offsetX = offset[0] > 0 ? 0 : offset[0];
			double offsetY = offset[1] > 0 ? 0 : offset[1];
			return Rect.from(shape.x() + offsetX, shape.y() + offsetY,
					 		 shape.width(), shape.height());
		}
	}
	
	// by urueda
	private Shape repositionShape(Canvas canvas, Shape shape){
		double[] offset = calculateOffset(canvas,shape); // x,y
		return calculateInnerShape(shape,offset);		
	}
	
	// by urueda (fix WidgetInfo panel outside screen in some cases)
	private Shape calculateWidgetInfoShape(Canvas canvas, Shape cwShape, double widgetInfoW, double widgetInfoH){
		Shape s = Rect.from(cwShape.x(), cwShape.y(), widgetInfoW, widgetInfoH);
		return repositionShape(canvas,s);
	}	

	private void visualizeActions(Canvas canvas, State state, Set<Action> actions){
		if((mode() == Modes.Spy || mode() == Modes.GenerateDebug) && settings().get(ConfigTags.VisualizeActions)){
			for(Action a : actions)
				a.get(Visualizer, Util.NullVisualizer).run(state, canvas, Pen.IgnorePen);
		}
	}

	private void visualizeSelectedAction(Canvas canvas, State state, Action action){
		if(mode() == Modes.GenerateDebug || mode() == Modes.ReplayDebug){
			Pen redPen = Pen.newPen().setColor(Color.Red).setFillPattern(FillPattern.Solid).setStrokeWidth(20).build();
			Visualizer visualizer = action.get(Visualizer, Util.NullVisualizer);

			final int BLINK_COUNT = 3;
			final double BLINK_DELAY = 0.5;
			for(int i = 0; i < BLINK_COUNT; i++){
				Util.pause(BLINK_DELAY);
				canvas.begin();
				visualizer.run(state, canvas, Pen.IgnorePen);
				canvas.end();
				Util.pause(BLINK_DELAY);
				canvas.begin();
				visualizer.run(state, canvas, redPen);
				canvas.end();
			}
		}
	}
	
	// by urueda
	protected Action selectAction(State state, Set<Action> actions){
		Assert.isTrue(actions != null && !actions.isEmpty());
		return Grapher.selectAction(state, actions);
	}

	protected boolean executeAction(SUT system, State state, Action action){
		try{
			action.run(system, state, settings.get(ConfigTags.ActionDuration));
			Util.pause(settings.get(ConfigTags.TimeToWaitAfterAction));
			return true;
		}catch(ActionFailedException afe){
			return false;
		}
	}

	private void saveStateSnapshot(State state){
		try{
			if(saveStateSnapshot){
				//System.out.println(Utils.treeDesc(state, 2, Tags.Role, Tags.Desc, Tags.Shape, Tags.Blocked));
				Taggable taggable = new TaggableBase();
				taggable.set(SystemState, state);
				logln("Saving state snapshot...", LogLevel.Debug);
				File file = Util.generateUniqueFile(settings.get(ConfigTags.OutputDir), "state_snapshot");
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				oos.writeObject(taggable);
				oos.close();
				saveStateSnapshot = false;
				logln("Saved state snapshot to " + file.getAbsolutePath(), LogLevel.Info);
			}
		}catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
	}
	
	// by urueda
	protected String getStateshot(State state){
		if (scrshotManager == null)
			return "";
		Shape viewPort = null;
		if (state.childCount() > 0){
			viewPort = state.child(0).get(Tags.Shape, null);
			if (viewPort != null && (viewPort.width() * viewPort.height() < 1))
				viewPort = null;
		}
		if (viewPort == null)
			viewPort = state.get(Tags.Shape, null); // get the SUT process canvas (usually, full monitor screen)
		AWTCanvas scrshot = AWTCanvas.fromScreenshot(Rect.from(viewPort.x(), viewPort.y(), viewPort.width(), viewPort.height()), AWTCanvas.StorageFormat.PNG, 1);
		return scrshotManager.saveStateshot(CodingManager.codify(state).toString(), scrshot);
	}
	
	// by urueda
	protected String getActionshot(State state, Action action){
		if (scrshotManager == null)
			return "";
		List<Finder> targets = action.get(Tags.Targets, null);
		if (targets != null){
			Widget w;
			Shape s;
			Rectangle r;
			Rectangle actionArea = new Rectangle(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);
			for (Finder f : targets){
				w = f.apply(state);
				s = w.get(Tags.Shape);
				r = new Rectangle((int)s.x(), (int)s.y(), (int)s.width(), (int)s.height());
				actionArea = actionArea.union(r);
			}
			AWTCanvas scrshot = AWTCanvas.fromScreenshot(Rect.from(actionArea.x, actionArea.y, actionArea.width, actionArea.height),
														 AWTCanvas.StorageFormat.PNG, 1);
			return scrshotManager.saveActionshot(CodingManager.codify(state), CodingManager.codify(state,action), scrshot);
		}
		return null;
	}
	
	// by urueda
	private Action mapUserEvent(State state){
		Assert.notNull(userEvent);
		if (userEvent[0] instanceof MouseButtons){ // mouse events
			double x = ((Double)userEvent[1]).doubleValue();
			double y = ((Double)userEvent[2]).doubleValue();			
			Widget w = null;
			try {
				w = Util.widgetFromPoint(state, x, y);
				x = 0.5; y = 0.5;
		        if (userEvent[0] == MouseButtons.BUTTON1) // left click
		        	return (new AnnotatingActionCompiler()).leftClickAt(w,x,y);
		        else if (userEvent[0] == MouseButtons.BUTTON3) // right click     
		        	return (new AnnotatingActionCompiler()).rightClickAt(w,x,y);
			} catch (WidgetNotFoundException we){
				System.out.println("Mapping user event ... widget not found @(" + x + "," + y + ")");
				return null;
			}
		} else if (userEvent[0] instanceof KBKeys) // key events
			return (new AnnotatingActionCompiler()).hitKey((KBKeys)userEvent[0]);
		else if (userEvent[0] instanceof String){ // type events
			if (lastExecutedAction == null)
				return null;
			List<Finder> targets = lastExecutedAction.get(Tags.Targets,null);
			if (targets == null || targets.size() != 1)
				return null;
			try {
				Widget w = targets.get(0).apply(state);
				return (new AnnotatingActionCompiler()).clickTypeInto(w,(String)userEvent[0]);
			} catch (WidgetNotFoundException we){
				return null;
			}
		}
			
		return null;
	}
	
	// by urueda
	private Object[] compileAdhocTestServerEvent(String event){				
		//Pattern p = Pattern.compile(BriefActionRolesMap.LC + "\\((\\d+.\\d+),(\\d+.\\d+)\\)");
		Pattern p = Pattern.compile("LC\\((\\d+.\\d+),(\\d+.\\d+)\\)");
		Matcher m = p.matcher(event);
		if (m.find())
			return new Object[]{MouseButtons.BUTTON1,new Double(m.group(1)),new Double(m.group(2))};
		
		//p = Pattern.compile(BriefActionRolesMap.RC + "\\((\\d+.\\d+),(\\d+.\\d+)\\)");
		p = Pattern.compile("RC\\((\\d+.\\d+),(\\d+.\\d+)\\)");
		m = p.matcher(event);
		if (m.find())
			return new Object[]{MouseButtons.BUTTON3,new Double(m.group(1)),new Double(m.group(2))};

		//p = Pattern.compile(BriefActionRolesMap.T + "\\((.*)\\)");
		p = Pattern.compile("T\\((.*)\\)");
		m = p.matcher(event);
		if (m.find()){
			String text = m.group(1);
			return new Object[]{ KBKeys.contains(text) ? KBKeys.valueOf(text) : text };
		}

		return null;
	}
	
	private static long stampLastExecutedAction = -1; // by urueda
	
	// by urueda (refactor run() method)
	 // return: problems?
	private boolean runAction(Canvas cv, SUT system, State state,
							  Taggable fragment, boolean problems, ObjectOutputStream oos){
		try{
			boolean actionSucceeded = true;
			cv.begin();
			Util.clear(cv);
			visualizeState(cv, state);
			logln("Building action set...", LogLevel.Debug);
			
			// begin by urueda
			Action action = null;
			boolean userEventAction = false;
			while (mode() == Modes.GenerateManual && !userEventAction){
				if (userEvent != null){
					action = mapUserEvent(state);
					userEventAction = (action != null);
					userEvent = null;
				}
				synchronized(this){
					try {
						this.wait(10);
					} catch (InterruptedException e) {}
				}
			}
			if (!userEventAction){
						
				if (mode() == Modes.AdhocTest) {
						while(adhocTestServerReader == null || adhocTestServerWriter == null){
							synchronized(this){
								try {
									this.wait(10);
								} catch (InterruptedException e) {}
							}
						}
						int adhocTestInterval = 10; // ms
						while (System.currentTimeMillis() < stampLastExecutedAction + adhocTestInterval){
							synchronized(this){
								try {
									this.wait(adhocTestInterval - System.currentTimeMillis() + stampLastExecutedAction + 1);
								} catch (InterruptedException e) {}
							}
						}
						do{
							System.out.println("AdhocTest waiting for event ...");
							try{
								adhocTestServerWriter.write("READY\r\n");
								adhocTestServerWriter.flush();
							} catch (Exception e){
								return true; // AdhocTest client disconnected?
							}
							try{
								String socketData = adhocTestServerReader.readLine().trim(); // one event per line
								System.out.println("\t... AdhocTest event = " + socketData);
								userEvent = compileAdhocTestServerEvent(socketData); // hack into userEvent
								if (userEvent == null){
									adhocTestServerWriter.write("???\r\n"); // not found
									adhocTestServerWriter.flush();									
								}else{
									action = mapUserEvent(state);
									if (action == null){
										adhocTestServerWriter.write("404\r\n"); // not found
										adhocTestServerWriter.flush();
									}
								}
								userEvent = null;
							} catch (Exception e){
								userEvent = null;
								return true; // AdhocTest client disconnected?
							}
						} while (action == null);
				} else { // end by urueda
				
					Set<Action> actions = deriveActions(system, state);
					
					if(actions.isEmpty()){
						logln("No available actions to execute! Stopping sequence generation!", LogLevel.Critical);
						return true; // problems found
					}
					
					fragment.set(ActionSet, actions);
					logln("Built action set!", LogLevel.Debug);
					visualizeActions(cv, state, actions);
					cv.end();
			
					if(mode() == Modes.Quit) return problems;
					logln("Selecting action...", LogLevel.Debug);
					action = selectAction(state, actions);
	
					userEventAction = false; // by urueda
				
				}
				
			}
			
			logln("Selected action '" + action + "'.", LogLevel.Debug);
			
			visualizeSelectedAction(cv, state, action);
			if(mode() == Modes.Quit) return problems;
				
			if(mode() != Modes.Spy){
				// begin by urueda
				String[] actionRepresentation = Action.getActionRepresentation(state,action,"\t");
				Grapher.notify(state,state.get(Tags.ScreenshotPath, null),
							   action,getActionshot(state,action),actionRepresentation[1]);
				// end by urueda
				logln(String.format("Executing (%d): %s...", actionCount, action.get(Desc, action.toString())), LogLevel.Debug);
				//if((actionSucceeded = executeAction(system, state, action))){
				if (userEventAction || (actionSucceeded = executeAction(system, state, action))){ // by urueda							
					//logln(String.format("Executed (%d): %s...", actionCount, action.get(Desc, action.toString())), LogLevel.Info);
					// begin by urueda
					logln(String.format("Executed [%d]: %s\n%s",
							actionCount,
							"ACTION_" + CodingManager.codify(action) + " " +
							"(stateaction_" + CodingManager.codify(state, action) + ") [@" +
							"state_" + CodingManager.codify(state) + "]",
							actionRepresentation[0]),
							LogLevel.Info);
					if (mode() == Modes.AdhocTest){
						try {
							adhocTestServerWriter.write("OK\r\n"); // adhoc action executed
							adhocTestServerWriter.flush();
						} catch (Exception e){} // AdhocTest client disconnected?
					}
					// end by urueda

					actionCount++;
					fragment.set(ExecutedAction, action);
					fragment.set(ActionDuration, settings().get(ConfigTags.ActionDuration));
					fragment.set(ActionDelay, settings().get(ConfigTags.TimeToWaitAfterAction));
					logln("Writing fragment to sequence file...", LogLevel.Debug);
					oos.writeObject(fragment);
	
					//if(actionCount % 4 == 0){
					if(stampLastExecutedAction < System.currentTimeMillis() - 10000){  // by urueda (every 10 seconds)
						oos.reset();
						oos.flush();
					}
					stampLastExecutedAction = System.currentTimeMillis();
	
					logln("Wrote fragment to sequence file!", LogLevel.Debug);
				}else{
					logln("Excecution of action failed!");
					try {
						adhocTestServerWriter.write("FAIL\r\n"); // action execution failed
						adhocTestServerWriter.flush();
					} catch (Exception e) {} // AdhocTest client disconnected?
				}				
			}
			
			lastExecutedAction = action; // by urueda
			
			if(mode() == Modes.Quit) return problems;
			if(!actionSucceeded){
				return true;
			}
			
			return problems;
		}catch(IOException ioe){
			logln("Unable to to save action in sequence file!", LogLevel.Critical);
			throw new RuntimeException(ioe);
		}		
	}

	// by urueda (refactor run() method)
	private void runTest(){		
		sequenceCount = 1; // by urueda
		RandomAccessFile raf = null;
		ObjectOutputStream oos = null;
		boolean problems = false;
		try{
			while(mode() != Modes.Quit && moreSequences()){

				String generatedSequence = Util.generateUniqueFile(settings.get(ConfigTags.OutputDir) + File.separator + "sequences", "sequence").getName(); // by urueda

				// begin by urueda
				Grapher.grapher(generatedSequence,settings.get(ConfigTags.TestGenerator));
				scrshotManager = new ScreenshotManager(System.currentTimeMillis(),generatedSequence);
				scrshotManager.start();
				// end by urueda
				
				problems = false;
				//actionCount = 0;
				actionCount = 1; // by urueda
	
				logln("Creating new sequence file...", LogLevel.Debug);
				final File currentSeq = new File(settings.get(ConfigTags.TempDir) + File.separator + "tmpsequence");
				Util.delete(currentSeq);
				//oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(currentSeq), 50000000));
				raf = new RandomAccessFile(currentSeq, "rw");
				//oos = new ObjectOutputStream(new FileOutputStream(raf.getFD()));
				oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(raf.getFD()))); // by urueda				
				logln("Created new sequence file!", LogLevel.Debug);
	
				logln("Building canvas...", LogLevel.Debug);
				Canvas cv = buildCanvas();
				//logln(Util.dateString("dd.MMMMM.yyyy HH:mm:ss") + " Starting system...", LogLevel.Info);
				// begin by urueda
				String dateString = Util.dateString("dd.MMMMM.yyyy HH:mm:ss");
				logln("\n<===================>\n" +
				      dateString + " Starting SUT ...", LogLevel.Info);
				// end by urueda
				SUT system = startSystem();
				//SUT system = WinProcess.fromProcName("firefox.exe");
				//logln("System is running!", LogLevel.Debug);
				logln("SUT is running!", LogLevel.Debug); // by urueda
				//logln("Starting sequence " + sequenceCount, LogLevel.Info);
				logln("Starting sequence " + sequenceCount + " (output as: " + generatedSequence + ")", LogLevel.Info); // by urueda
				beginSequence();
				logln("Obtaining system state...", LogLevel.Debug);
				State state = getState(system);
				logln("Successfully obtained system state!", LogLevel.Debug);
				saveStateSnapshot(state);
				Verdict verdict = state.get(OracleVerdict, Verdict.OK); 
				if(verdict.severity() >= settings().get(ConfigTags.FaultThreshold)){
					problems = true;					
					logln("Detected fault: " + verdict, LogLevel.Critical);
				}
				Taggable fragment = new TaggableBase();
				fragment.set(SystemState, state);
	
				while(mode() != Modes.Quit && moreActions(state)){
					problems = runAction(cv,system,state,fragment,problems,oos);
					if (!problems){					
						logln("Obtaining system state...", LogLevel.Debug);
						state = getState(system);
						logln("Successfully obtained system state!", LogLevel.Debug);
						saveStateSnapshot(state);
						verdict = state.get(OracleVerdict, Verdict.OK); 
						if(verdict.severity() >= settings().get(ConfigTags.FaultThreshold)){							
							problems = true;
							logln("Detected fault: " + verdict, LogLevel.Critical);
						}
						
						fragment = new TaggableBase();
						fragment.set(SystemState, state);		
					}
				}
								
				logln("Writing fragment to sequence file...", LogLevel.Debug);
				oos.writeObject(fragment);
				logln("Wrote fragment to sequence file!", LogLevel.Debug);
				oos.close();
				oos = null;
				raf.close();

				Grapher.walkFinished(!problems,
									 mode() == Modes.Spy ? null : state,
									 getStateshot(state)); // by urueda
				
				logln("Sequence " + sequenceCount + " finished.", LogLevel.Info);
				if(problems)
					logln("Sequence contained problems!", LogLevel.Critical);
				finishSequence(currentSeq);
	
				if(!settings().get(ConfigTags.OnlySaveFaultySequences)){
					//String generatedSequence = Util.generateUniqueFile(settings.get(ConfigTags.OutputDir) + File.separator + "sequences", "sequence").getName();
					logln("Copying generated sequence (\"" + generatedSequence + "\") to output directory...", LogLevel.Info);
					Util.copyToDirectory(currentSeq.getAbsolutePath(), 
							settings.get(ConfigTags.OutputDir) + File.separator + "sequences", 
							generatedSequence);
					logln("Copied generated sequence to output directory!", LogLevel.Debug);					
				}
	
				
				if(problems){
					//String generatedSequence = Util.generateUniqueFile(settings.get(ConfigTags.OutputDir) + File.separator + "error_sequences", "sequence").getName();
					logln("Copying erroneous sequence (\"" + generatedSequence + "\") to error_sequences directory...", LogLevel.Info);
					Util.copyToDirectory(currentSeq.getAbsolutePath(), 
							settings.get(ConfigTags.OutputDir) + File.separator + "error_sequences", 
							generatedSequence);
					logln("Copied erroneous sequence to output directory!", LogLevel.Debug);
				}
	
				logln("Releasing canvas...", LogLevel.Debug);
				cv.release();
				//logln("Shutting down system...", LogLevel.Info);
				logln("Shutting down the SUT...", LogLevel.Info); // by urueda
				system.stop();
				//logln("System has been shut down!", LogLevel.Debug);
				logln("... SUT has been shut down!", LogLevel.Debug); // by urueda
				
				logln(Grapher.getReport(), LogLevel.Info); // by urueda
				
				sequenceCount++;
			}
		}catch(IOException ioe){
			logln("Unable to save sequence file!", LogLevel.Critical);
			throw new RuntimeException(ioe);
		}finally{
			if(oos != null){
				try {
					oos.close();
					raf.close();					
				} catch (IOException e) {
					e.printStackTrace();
				}
				oos = null;
			}
		}
	}
	
	public final void run(final Settings settings) {		
		startTime = Util.time();
		this.settings = settings;
		mode = settings.get(ConfigTags.Mode);
		initialize(settings);

		try {
			logln("Registering keyboard and mouse hooks", LogLevel.Debug);
			GlobalScreen.registerNativeHook();
			GlobalScreen.getInstance().addNativeKeyListener(this);
			GlobalScreen.getInstance().addNativeMouseListener(this);
			logln("Successfully registered keyboard and mouse hooks!", LogLevel.Debug);

			logln("'" + mode() + "' mode active.", LogLevel.Info);

			if(mode() == Modes.View){
				new SequenceViewer(settings).run();
			}else if(mode() == Modes.Replay || mode() == Modes.ReplayDebug){
				replay();
			}else if(mode() == Modes.Generate || mode() == Modes.Spy || mode() == Modes.GenerateDebug){
				runTest();
			}
		} catch (NativeHookException e) {
			logln("Unable to install keyboard and mouse hooks!", LogLevel.Critical);
			throw new RuntimeException("Unable to install keyboard and mouse hooks!", e);
		}finally{
			try{
				logln("Unregistering keyboard and mouse hooks", LogLevel.Debug);
				GlobalScreen.unregisterNativeHook();
				stopAdhocServer(); // by urueda
			}catch(Exception e){}
		}
	}

	private void replay(){
		boolean success = true;
		//actionCount = 0;
		actionCount = 1; // by urueda

		try{
			File seqFile = new File(settings.get(ConfigTags.PathToReplaySequence));
			//ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(seqFile)));
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(seqFile)))); // by urueda

			SUT system = startSystem();
			Canvas cv = buildCanvas();
			State state = getState(system);

			while(success && mode() != Modes.Quit){
				Taggable fragment;
				try{
					fragment = (Taggable) ois.readObject();
				}catch(IOException ioe){
					success = true;
					break;
				}

				success = false;
				int tries = 0;
				double start = Util.time();

				while(!success && (Util.time() - start < settings.get(ConfigTags.ReplayRetryTime))){
					tries++;
					cv.begin();
					Util.clear(cv);
					visualizeState(cv, state);
					cv.end();

					if(mode() == Modes.Quit) break;
					Action action = fragment.get(ExecutedAction, new NOP());
					visualizeSelectedAction(cv, state, action);
					if(mode() == Modes.Quit) break;

					double actionDuration = settings.get(ConfigTags.UseRecordedActionDurationAndWaitTimeDuringReplay) ? fragment.get(Tags.ActionDuration, 0.0) : settings.get(ConfigTags.ActionDuration);
					double actionDelay = settings.get(ConfigTags.UseRecordedActionDurationAndWaitTimeDuringReplay) ? fragment.get(Tags.ActionDelay, 0.0) : settings.get(ConfigTags.TimeToWaitAfterAction);

					try{
						if(tries < 2){
							log(String.format("Trying to execute (%d): %s...", actionCount, action.get(Desc, action.toString())), LogLevel.Info);
						}else{
							if(tries % 50 == 0)
								logln(".", LogLevel.Info);
							else
								log(".", LogLevel.Info);
						}

						action.run(system, state, actionDuration);
						success = true;
						actionCount++;
						logln("Success!", LogLevel.Info);
					}catch(ActionFailedException afe){}

					Util.pause(actionDelay);

					if(mode() == Modes.Quit) break;
					state = getState(system);
				}

			}

			cv.release();
			ois.close();

		}catch(IOException ioe){
			throw new RuntimeException("Cannot read file.", ioe);
		}catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("Cannot read file.", cnfe);
		}

		if(success)
			logln("Sequence successfully replayed!", LogLevel.Info);
		else
			logln("Failed to replay sequence.", LogLevel.Critical);
	}
}
