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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.fruit.alayer.AWTCanvas;

/**
 * SUT screenshots manager.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class ScreenshotManager extends Thread {
	
	private static final String OUT_DIR = "output/scrshots/";
	
	private static long managingTimestamp = -1; // manages the screenshots of a time stamp session
	private static String testSequenceFolder = null;

	private long timestamp;

	private LinkedList<String> scrshotSavingQueue =  new LinkedList<String>();
	private HashMap<String,AWTCanvas> scrshotBlacklist = new HashMap<String,AWTCanvas>();

	public ScreenshotManager(long timestamp, String testSequenceFolder){
		ScreenshotManager.managingTimestamp = timestamp;
		ScreenshotManager.testSequenceFolder = testSequenceFolder;
		(new File(OUT_DIR + testSequenceFolder)).mkdirs();
		this.timestamp = timestamp;
	}
	
	@Override
	public void run(){		
		while (managingTimestamp == timestamp || !scrshotSavingQueue.isEmpty()){
			while(scrshotSavingQueue.isEmpty())
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {}
			String scrshotPath;
			synchronized(scrshotSavingQueue){
				scrshotPath = scrshotSavingQueue.removeFirst();
			}
			AWTCanvas scrshot = scrshotBlacklist.get(scrshotPath);
			try {
				scrshot.saveAsPng(scrshotPath);
				synchronized(scrshotBlacklist){
					scrshotBlacklist.put(scrshotPath,null);
				}
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("Could not save screenshot to: " + scrshotPath);
			}
		}
		System.out.println("THREAD FINISHED!");
	}
	
	public String saveStateshot(String stateID, AWTCanvas stateshot){
		String statePath = OUT_DIR + testSequenceFolder + "/" + stateID + ".png";
		if (!saved(statePath))
			save(statePath,stateshot);
		return statePath;
	}
	
	public String saveActionshot(String stateID, String actionID, final AWTCanvas actionshot){
		String actionPath = OUT_DIR + testSequenceFolder + "/" + stateID + "_" + actionID + ".png";
		if (!saved(actionPath))
			save(actionPath,actionshot);
		return actionPath;
	}
		
	private boolean saved(String scrshotPath){
		return scrshotBlacklist.containsKey(scrshotPath);
	}

	private void save(String scrshotPath, AWTCanvas scrshot){
		synchronized(scrshotBlacklist){
			scrshotBlacklist.put(scrshotPath,scrshot);
		}
		synchronized(scrshotSavingQueue){
			scrshotSavingQueue.add(scrshotPath);
		}
	}

}
