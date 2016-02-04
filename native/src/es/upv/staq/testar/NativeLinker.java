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
 * This software is distributed FREE of charge under the TESTAR license, as an open        *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package es.upv.staq.testar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fruit.alayer.Canvas;
import org.fruit.alayer.NoSuchTagException;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.StateBuilder;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Widget;
import org.fruit.alayer.windows.GDIScreenCanvas;
import org.fruit.alayer.windows.UIARoles;
import org.fruit.alayer.windows.UIAStateBuilder;
import org.fruit.alayer.windows.UIATags;
import org.fruit.alayer.windows.WinProcess;

import static org.fruit.alayer.windows.UIARoles.*;

/**
 * A native connector.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class NativeLinker {

	public static final String OS_WINDOWS = "WINDOWS";
	public static final String OS_MACOS = "MACOS";
	public static final String OS_ANDROID = "ANDRIOD";
	
	public static String TARGET_OS = OS_WINDOWS;
	
	public static StateBuilder getNativeStateBuilder(Double timeToFreeze){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return new UIAStateBuilder(timeToFreeze);
		//else if (TARGET_OS.equals(OS_MACOS))
			// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
			// TODO
		//else
			// TODO (throw exception)
	}
	
	public static Canvas getNativeCanvas(Pen pen){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return GDIScreenCanvas.fromPrimaryMonitor(pen);		
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)
	}
	
	public static SUT getNativeSUT(String executableCommand){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return WinProcess.fromExecutable(executableCommand);		
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)
	}

	public static List<SUT> getNativeProcesses(){
		return WinProcess.fromAll();
	}
	
	public static SUT getNativeProcess(String processName){
		return WinProcess.fromProcName(processName);		
	}
	
	public static Set<Tag<?>> getNativeTags(){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return UIATags.tagSet();
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)		
	}
	
	public static Tag<?> getNativeTag(String tagName){
		Set<Tag<?>> tags = getNativeTags();
		Tag<?> tag = null, t;
		Iterator<Tag<?>> it = tags.iterator();
		while (tag == null && it.hasNext()){
			t = (Tag<?>) it.next();
			if (t.name().equals(tagName))
				return t;
		}
		return null; // not found
	}

	public static Collection<Role> getNativeRoles(){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return UIARoles.rolesSet();
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)		
	}
	
	public static Role getNativeRole(String roleName){
		Collection<Role> roles = getNativeRoles();
		Role role = null, r;
		Iterator<Role> it = roles.iterator();
		while (role == null && it.hasNext()){
			r = (Role) it.next();
			if (r.name().equals(roleName))
				return r;
		}
		return null; // not found
	}
	
	public static Collection<Role> getNativeRoles(String... roleNames){
		Collection<Role> roles = new ArrayList<Role>(roleNames.length);
		for (String roleName : roleNames){
			roles.add(getNativeRole(roleName));
		}
		return roles;
	}

	public static Role getNativeRole_TitleBar(){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return UIARoles.UIATitleBar;
		//else if (TARGET_OS.equals(OS_MACOS))
			// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
			// TODO
		//else
			// TODO (throw exception)		
	}
	
	public static Role getNativeRole_Window(){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return UIARoles.UIAWindow;
		//else if (TARGET_OS.equals(OS_MACOS))
			// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
			// TODO
		//else
			// TODO (throw exception)		
	}

	public static boolean getNativeProperty(Widget widget, String propertyName) throws NoSuchTagException {
		Tag<Boolean> tag = null;
		try {
			tag = (Tag<Boolean>) getNativeTag(propertyName);
			return widget.get(tag).booleanValue();
		} catch (Exception e) {
			throw new NoSuchTagException(tag);
		}
	}
	
	public static Role[] getNativeUnclickable(){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return new Role[]{UIASeparator, UIAToolBar, UIAToolTip, UIAMenuBar, 
							  UIAMenu, UIAHeader, UIATabControl, UIAPane, UIATree,
							  UIAWindow, UIATitleBar, UIAThumb, UIAEdit, UIAText};
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)		
	}
	
	public static Role[] getNativeTypeable(){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return new Role[]{UIAEdit, UIAText};
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)		
	}
	
	public static boolean isNativeTypeable(Widget w){
		//if (TARGET_OS.equals(OS_WINDOWS))
			return w.get(UIATags.UIAIsKeyboardFocusable);
		//else if (TARGET_OS.equals(OS_MACOS))
		// TODO
		//else if (TARGET_OS.equals(OS_ANDROID))
		// TODO
		//else
		// TODO (throw exception)		
	}

}
