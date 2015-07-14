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
package org.fruit.alayer.actions;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.AbsolutePosition;
import org.fruit.alayer.Abstractor;
import org.fruit.alayer.Action;
import org.fruit.alayer.Finder;
import org.fruit.alayer.Position;
import org.fruit.alayer.StdAbstractor;
import org.fruit.alayer.Widget;
import org.fruit.alayer.Tags;
import org.fruit.alayer.WidgetPosition;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.MouseButtons;

public class StdActionCompiler {
	Abstractor abstractor;
	private final Action LMouseDown = new MouseDown(MouseButtons.BUTTON1);
	private final Action RMouseDown = new MouseDown(MouseButtons.BUTTON3);
	private final Action LMouseUp = new MouseUp(MouseButtons.BUTTON1);
	private final Action RMouseUp = new MouseUp(MouseButtons.BUTTON3);
	private final Action NOP = new NOP();

	public StdActionCompiler(){	this(new StdAbstractor()); }

	public StdActionCompiler(Abstractor abstractor){
		this.abstractor = abstractor;
	}

	public Action leftClick(){
		return new CompoundAction.Builder().add(LMouseDown, 0)
				.add(LMouseUp, 0).add(NOP, 1).build();
	}

	public Action rightClick(){
		return new CompoundAction.Builder().add(RMouseDown, 0).
				add(RMouseUp, 0).add(NOP, 1).build();
	}

	public Action leftDoubleClick(){
		Action lc = leftClick();
		return new CompoundAction.Builder().add(lc, 0).
				add(lc, 0).add(NOP, 1).build();
	}

	public Action leftClickAt(Position position){
		Assert.notNull(position);
		return new CompoundAction.Builder().add(new MouseMove(position), 1)
				.add(LMouseDown, 0).add(LMouseUp, 0).build();
	}

	public Action leftClickAt(double absX, double absY){
		return leftClickAt(new AbsolutePosition(absX, absY));
	}

	public Action leftClickAt(Widget w){
		return leftClickAt(w, 0.5, 0.5);
	}

	public Action leftClickAt(Widget w, double relX, double relY){
		Finder wf = abstractor.apply(w);
		Action ret = leftClickAt(new WidgetPosition(wf, Tags.Shape, relX, relY, true));
		ret.set(Tags.Targets, Util.newArrayList(wf));
		return ret;
	}

	public Action rightClickAt(Position position){
		Assert.notNull(position);
		return new CompoundAction.Builder().add(new MouseMove(position), 1)
				.add(RMouseDown, 0).add(RMouseUp, 0).build();
	}

	public Action rightClickAt(double absX, double absY){
		return rightClickAt(new AbsolutePosition(absX, absY));
	}

	public Action rightClickAt(Widget w){
		return rightClickAt(w, 0.5, 0.5);
	}

	public Action rightClickAt(Widget w, double relX, double relY){
		Finder wf = abstractor.apply(w);
		Action ret = rightClickAt(new WidgetPosition(wf, Tags.Shape, relX, relY, true));
		ret.set(Tags.Targets, Util.newArrayList(wf));
		return ret;
	}

	public Action leftDoubleClickAt(Position position){
		Assert.notNull(position);
		return new CompoundAction.Builder().add(new MouseMove(position), 1)
				.add(LMouseDown, 0).add(LMouseUp, 0).add(LMouseDown, 0).add(LMouseUp, 0).build();
	}

	public Action leftDoubleClickAt(double absX, double absY){
		return leftDoubleClickAt(new AbsolutePosition(absX, absY));
	}

	public Action leftDoubleClickAt(Widget w){
		return leftDoubleClickAt(w, 0.5, 0.5);
	}

	public Action leftDoubleClickAt(Widget w, double relX, double relY){
		Finder wf = abstractor.apply(w);
		Action ret = leftDoubleClickAt(new WidgetPosition(wf, Tags.Shape, relX, relY, true));
		ret.set(Tags.Targets, Util.newArrayList(wf));
		return ret;
	}

	public Action dragFromTo(Widget from, Widget to){
		return dragFromTo(from, 0.5, 0.5, to, 0.5, 0.5);
	}

	public Action dragFromTo(Widget from, double fromRelX, double fromRelY, Widget to, double toRelX, double toRelY){
		return dragFromTo(new WidgetPosition(abstractor.apply(from), Tags.Shape, fromRelX, fromRelY, true),
				new WidgetPosition(abstractor.apply(to), Tags.Shape, toRelX, toRelY, true));
	}

	public Action dragFromTo(Position from, Position to){
		return new CompoundAction.Builder().add(new MouseMove(from), 1)
				.add(LMouseDown, 0).add(new MouseMove(to), 1)
				.add(LMouseUp, 0).build();		
	}

	public Action clickTypeInto(final Position position, final String text){
		Assert.notNull(position, text);
		return new CompoundAction.Builder().add(leftClickAt(position), 1)
				.add(new Type(text), 1).build();
	}

	public Action clickTypeInto(Widget w, String text){
		return clickTypeInto(w, 0.5, 0.5, text);
	}

	public Action clickTypeInto(Widget w, double relX, double relY, String text){
		Finder wf = abstractor.apply(w);
		Action ret = clickTypeInto(new WidgetPosition(wf, Tags.Shape, relX, relY, true), text);
		ret.set(Tags.Targets, Util.newArrayList(wf));
		return ret;
	}
	
	public Action hitKey(KBKeys key){
		return new CompoundAction.Builder().add(new KeyDown(key), .0)
				.add(new KeyUp(KBKeys.VK_ESCAPE), 1).add(NOP, 1.0).build();
	}
	
	public Action killProcessByPID(long pid){ return killProcessByPID(pid, 0); }
	public Action killProcessByName(String name){ return killProcessByName(name, 0); }
	public Action killProcessByPID(long pid, double timeToWaitBeforeKilling){ return KillProcess.byPID(pid, timeToWaitBeforeKilling); }
	public Action killProcessByName(String name, double timeToWaitBeforeKilling){ return KillProcess.byName(name, timeToWaitBeforeKilling); }
	public Action activateSystem(){	return new ActivateSystem(); }
}