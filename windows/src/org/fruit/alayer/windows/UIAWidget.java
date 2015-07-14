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
package org.fruit.alayer.windows;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import org.fruit.Util;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Widget;

class UIAWidget implements Widget, Serializable {
	private static final long serialVersionUID = 8840515358018797073L;
	UIAState root;
	UIAWidget parent;
	Map<Tag<?>, Object> tags = Util.newHashMap();
	ArrayList<UIAWidget> children = new ArrayList<UIAWidget>();
	UIAElement element;
		
	protected UIAWidget(UIAState root, UIAWidget parent, UIAElement element){
		this.parent = parent;
		this.element = element;
		this.root = root;
		
		if(parent != null)
			root.connect(parent, this);
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject();
	}

	
	final boolean valid(){ return root != null; }
	final void check(){ if(root == null) throw new IllegalStateException(); }
	
	final public void moveTo(Widget p, int idx) { /*check();*/ root.setParent(this, p, idx); }
	public final UIAWidget addChild() { /*check();*/ return root.addChild(this, null); }
	public final UIAState root() { return root; }
	public final UIAWidget parent() { /*check();*/ return root.getParent(this); }
	public final UIAWidget child(int i) { /*check();*/ return root.getChild(this, i); }
	public final void remove() { /*check();*/ root.remove(this); }
	public final int childCount() { /*check();*/ return root.childCount(this); }

	public final <T> T get(Tag<T> t) { /*check;*/ return root.get(this, t); }
	public final <T> void set(Tag<T> tag, T value) { /*check;*/ root.setTag(this, tag, value); }
	public final <T> T get(Tag<T> tag, T defaultValue) { /*check;*/ return root.get(this, tag, defaultValue); }
	public final Iterable<Tag<?>> tags() { /*check;*/ return root.tags(this); }
	public final void remove(Tag<?> tag) { /*check;*/ root.remove(this, tag); }			
}