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

import java.util.List;
import java.util.Set;
import org.fruit.Assert;
import org.fruit.FruitException;
import org.fruit.alayer.SUTBase;
import org.fruit.alayer.SystemStartException;
import org.fruit.alayer.SystemStopException;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.Util;
import org.fruit.alayer.devices.AWTKeyboard;
import org.fruit.alayer.devices.AWTMouse;
import org.fruit.alayer.devices.Keyboard;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.Mouse;

public final class WinProcess extends SUTBase {

	public static void toForeground(long pid) throws WinApiException{
		toForeground(pid, 0.3, 100);
	}

	public static void toForeground(long pid, double foregroundEstablishTime, int maxTries) throws WinApiException{
		Keyboard kb = AWTKeyboard.build();

		int cnt = 0;
		while(!isForeground(pid) && cnt < maxTries && isRunning(pid)){
			cnt++;
			kb.press(KBKeys.VK_ALT);

			for(int i = 0; i < cnt && isRunning(pid); i++){
				kb.press(KBKeys.VK_TAB);
				kb.release(KBKeys.VK_TAB);
			}
			kb.release(KBKeys.VK_ALT);
			Util.pause(foregroundEstablishTime);
		}	

		if(!isForeground(pid) && isRunning(pid))
			throw new WinApiException("Unable to bring process to foreground!");
	}

	public static WinProcess fromPID(long pid) throws SystemStartException{
		try{
			long hProcess = Windows.OpenProcess(Windows.PROCESS_QUERY_INFORMATION, false, pid);
			WinProcess ret = new WinProcess(hProcess, false);
			ret.set(Tags.Desc, procName(pid) + " (pid: " + pid + ")");
			return ret;
		}catch(FruitException fe){
			throw new SystemStartException(fe);
		}
	}

	public static WinProcess fromProcName(String processName) throws SystemStartException{
		Assert.notNull(processName);
		for(WinProcHandle wph : runningProcesses()){
			if(processName.equals(wph.name()))
				return fromPID(wph.pid());
		}
		throw new SystemStartException("Process '" + processName + "' not found!");
	}

	public static WinProcess fromExecutable(String path) throws SystemStartException{
		try{
			Assert.notNull(path);
			long handles[] = Windows.CreateProcess(null, path, false, 0, null, null, null, "unknown title", new long[14]);
			long hProcess = handles[0];
			long hThread = handles[1];
			Windows.CloseHandle(hThread);

			WinProcess ret = new WinProcess(hProcess, true);
			ret.set(Tags.Desc, path);
			return ret;
		}catch(FruitException fe){
			throw new SystemStartException(fe);
		}
	}

	public static boolean isForeground(long pid){
		long hwnd = Windows.GetForegroundWindow();
		return !Windows.IsIconic(hwnd) && (Windows.GetWindowProcessId(hwnd) == pid);
	}

	public static boolean isRunning(long pid){
		long hProcess = Windows.OpenProcess(Windows.PROCESS_QUERY_INFORMATION, false, pid);
		boolean ret = Windows.GetExitCodeProcess(hProcess) == Windows.STILL_ACTIVE;
		Windows.CloseHandle(hProcess);
		return ret;
	}

	public static void killProcess(long pid) throws SystemStopException{
		try{
			long hProcess = Windows.OpenProcess(Windows.PROCESS_TERMINATE, false, pid);
			Windows.TerminateProcess(hProcess, -1);
			Windows.CloseHandle(hProcess);
		}catch(WinApiException wae){
			throw new SystemStopException(wae);
		}
	}

	public static String procName(long pid) throws WinApiException{
		long hProcess = Windows.OpenProcess(Windows.PROCESS_QUERY_INFORMATION | Windows.PROCESS_VM_READ, false, pid);
		long[] hm = Windows.EnumProcessModules(hProcess);
		String ret = null;
		if(hm.length == 0)
			throw new WinApiException("Unable to retrieve process name!");
		ret = Windows.GetModuleBaseName(hProcess, hm[0]);
		Windows.CloseHandle(hProcess);
		return ret;
	}

	public static List<WinProcHandle> runningProcesses(){
		List<WinProcHandle> ret = Util.newArrayList();
		for(long pid : Windows.EnumProcesses()){
			if(pid != 0)
				ret.add(new WinProcHandle(pid));
		}
		return ret;
	}

	long hProcess;
	final boolean stopProcess;
	final Keyboard kbd = AWTKeyboard.build();
	final Mouse mouse = AWTMouse.build();
	final long pid;
	
	private WinProcess(long hProcess, boolean stopProcess){
		this.hProcess = hProcess;
		this.stopProcess = stopProcess;
		pid = pid();
	}

	public void finalize(){ stop(); }

	public void stop() throws SystemStopException {
		try{
			if(hProcess != 0){
				if(stopProcess)
					Windows.TerminateProcess(hProcess, 0);
				Windows.CloseHandle(hProcess);
				hProcess = 0;
			}
		}catch(WinApiException wae){
			throw new SystemStopException(wae);
		}
	}

	public boolean isRunning() {
		return hProcess != 0 && 
				Windows.GetExitCodeProcess(hProcess) == Windows.STILL_ACTIVE;
	}

	public String toString(){
		return this.get(Tags.Desc, "Windows Process");
	}

	public long pid(){
		if(!isRunning())
			throw new IllegalStateException();
		return Windows.GetProcessId(hProcess);
	}

	public boolean isForeground(){ return isForeground(pid()); }
	public void toForeground(){ toForeground(pid()); }
	
	@SuppressWarnings("unchecked")
	protected <T> T fetch(Tag<T> tag){		
		if(tag.equals(Tags.StandardKeyboard))
			return (T)kbd;
		else if(tag.equals(Tags.StandardMouse))
			return (T)mouse;
		else if(tag.equals(Tags.PID))
			return (T)(Long)pid;
		else if(tag.equals(Tags.ProcessHandles))
			return (T)runningProcesses().iterator();
		else if(tag.equals(Tags.SystemActivator))
			return (T) new WinProcessActivator(pid);
		return null;
	}
	
	protected Set<Tag<?>> tagDomain(){
		Set<Tag<?>> ret = Util.newHashSet();
		ret.add(Tags.StandardKeyboard);
		ret.add(Tags.StandardMouse);
		ret.add(Tags.ProcessHandles);
		ret.add(Tags.PID);
		ret.add(Tags.SystemActivator);
		return ret;
	}
}