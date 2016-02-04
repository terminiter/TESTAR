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
 * This graph project is distributed FREE of charge under the TESTAR license, as an open *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package es.upv.staq.testar.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import es.upv.staq.testar.graph.algorithms.IWalker;
import es.upv.staq.testar.graph.algorithms.MaxCoverage;
import es.upv.staq.testar.graph.algorithms.QLearning;
import es.upv.staq.testar.graph.algorithms.RandomWalker;
import es.upv.staq.testar.graph.reporting.GraphReporter;

/**
 * Graphing utility for TESTAR' tests.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 */
public class Grapher implements Runnable {

	public static final String RANDOM_GENERATOR = "random"; // default
	public static final String QLEARNING_GENERATOR = "qlearning";
	public static final String MAXCOVERAGE_GENERATOR = "maxcoverage";
		
	// QLEARNING parameters needs calibration
	// DISCOUNT history on wincalc (999 actions sequence):
		// a) .2 
		// b) .5
		// c) .75
		// d) .95 (sebastian base)
	public static double QLEARNING_DISCOUNT_PARAM = .25; // non fnal to allow calibration
	// MAXREWARD history on wincalc (999 actions sequence)
		// a) 2.0
		// b) 5.0
		// c) 99
		// d) 9999999.0 (sebastian base)
	public static double QLEARNING_MAXREWARD_PARAM = 3.33; // non final to allow calibration
	// SUT UI space exploration capability (note: being worse at exploration might be good at concrete UI parts as these parts are more exercised): 
		// a) => better than random
		// b) => minor states repetition
		// c) => worse than random
		// d) => worse than random, high repetition of states
	private static double MAX_MAXREWARD = 99.9; // higher values to be analysed
	
	public static final boolean QLEARNING_CALIBRATION = false; // how-to retrieve from logs: findstr "CALIBRATION" log_file_name.log
	
	public static String testGenerator = RANDOM_GENERATOR;

	private static Grapher singletonGrapher = new Grapher();	
			
	private static LinkedList<Movement> movementsFIFO = new LinkedList<Movement>();
	private static List<Integer> movementsSync = new ArrayList<Integer>();
		
	private static IEnvironment env = null;
	
	private static String testSequencePath = null;
	
	private static IWalker walker = null;
	private static WalkStopper walkStopper = null;
	
	private static boolean graphing = false; // true while graphing, false otherwise
	
	private static transient ExecutorService exeSrv;
	
	/**
	 * Run a new TESTAR grapher.
	 * @param testGenerator A valid generator is expected.
	 */
	public static void grapher(String testSequencePath,
							   String testGenerator, String maxReward, String discount) {
		try {
			synchronized(env){
				while (env != null){ // wait until a previous test sequence grapher finishes ...
					try {
						env.wait();
					} catch (InterruptedException e) {
						System.out.println("TESTAR grapher sync interruped\n" + e.toString());
					}
				}
			}
		} catch (Exception e){} // env may be set to null when we try to sync on it
		Grapher.testSequencePath = testSequencePath;
		Grapher.testGenerator = testGenerator;
		Grapher.QLEARNING_MAXREWARD_PARAM = new Double(maxReward).doubleValue();
		Grapher.QLEARNING_DISCOUNT_PARAM = new Double(discount).doubleValue();
		exeSrv = Executors.newFixedThreadPool(1);
		exeSrv.execute(singletonGrapher);
	}
	
	/**
	 * Notification about an executed action from a SUT state, which is considered a Movement.
	 * This movement is added to a FIFO queue, which can be consumed synchronously.
	 * @param state SUT state before action execution.
	 * @param stateshotPath SUT state screenshot before action execution.
	 * @param action Executed action.
	 * @param actionshotPath SUT state screenshot after action execution.
	 * @param actionRepresentation A text representation for the action.
	 */
	public static synchronized void notify(State state, String stateshotPath,
										   Action action, String actionshotPath, String actionRepresentation){		
		//System.out.println("TESTAR grapher notified: ACTION_" +
		//				   CodingManager.codify(action) + " (stateaction_" +
		//				   CodingManager.codify(state,action) + ") [state_" +
		//				   CodingManager.codify(state) + "]");
		IGraphState graphState = env.convertState(state);
		IGraphAction graphAction = env.convertAction(state,action);
		graphAction.setDetailedName(actionRepresentation);
		graphState.setStateshot(stateshotPath);
		graphAction.setStateshot(actionshotPath);
		synchronized(movementsFIFO){
			movementsFIFO.add(new Movement(graphState,graphAction)); // Movements PRODUCER
			movementsFIFO.notifyAll(); // awake CONSUMER
		}
	}	
	
	/**
	 * Get notification about the end of a test sequence.
	 * @param status 'true' walk finished OK, 'false' walk suddenly STOPPED.
	 * @param endState Ending SUT state.
	 * @param scrshotPath The state screenshot.
	 */
	public static void walkFinished(final boolean status, final State endState, final String scrshotPath){
		if (walkStopper != null){
			if (endState != null)
				env.convertState(endState).setStateshot(scrshotPath);
			walkStopper.stopWalk(status,endState);
			graphing = false;
			synchronized(movementsFIFO){
				movementsFIFO.notifyAll(); // awake CONSUMER
			}
		}	
	}
	
	/**
	 * Consumes a pair of <State,Action> from a FIFO queue, synchronously.
	 * @return Next non consumed movement.
	 */
	public static Movement getMovement(){
		graphing = false;
		Movement movement = null;
		synchronized(movementsFIFO){
			while(movementsFIFO.isEmpty()){
				try {
					if (walkStopper != null && walkStopper.continueWalking())
						movementsFIFO.wait();
					else
						return null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			movement = movementsFIFO.removeFirst();; // Movements CONSUMER
			graphing = true;
			//System.out.println(movementsFIFO.size() + " pending movements while graphing: " + movement.toString());
			movementsSync.add(movementsFIFO.size());
		}
		return movement;
	}
	
	public static List<Integer> getMovementsSync(){
		return movementsSync;
	}
	
	/**
	 * Sync TESTAR movements productions and graph movements consumption.
	 */
	public static void syncMovements(){
		synchronized(movementsFIFO){
			while(!movementsFIFO.isEmpty()){
				try {
					movementsFIFO.wait();
				} catch (InterruptedException e) {}
			}
		}
		while(graphing){
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {}
		}
		return;
	}
	
	/**
	 * Selects an action to be executed from a set of available actions for a SUT state.
	 * @param state SUT state.
	 * @param actions
	 * @return
	 */
	public static Action selectAction(State state, Set<Action> actions){
		env.notifyEnvironment(state, actions);
		return walker.selectAction(env, state, actions);
	}
	
	@Override
	public void run() {
		long graphTime = System.currentTimeMillis();
		GraphReporter.useGraphData(graphTime,testSequencePath);
		//WalkReport wr = new WalkReport("Q-Learning", 0, 0, 0, 0, 0, 0);
		//System.out.println(wr);
		env = new TESTAREnvironment();
		if (testGenerator.equals(QLEARNING_GENERATOR)){
			if (QLEARNING_CALIBRATION){
				QLEARNING_DISCOUNT_PARAM = Math.random(); // 0.0 .. 1.0
				QLEARNING_MAXREWARD_PARAM = Math.random() * MAX_MAXREWARD; // 0.0 .. MAX_MAXREWARD
			}
			walker = new QLearning(QLEARNING_DISCOUNT_PARAM, QLEARNING_MAXREWARD_PARAM);
			System.out.println("<Q-Learning> test generator enabled (" +
							   "discount = " + QLEARNING_DISCOUNT_PARAM + ", maxReward = " + QLEARNING_MAXREWARD_PARAM + ")");
		} else if (testGenerator.equals(MAXCOVERAGE_GENERATOR)){
			walker = new MaxCoverage(new Random(graphTime));
			System.out.println("<MaxCoverage UI exploration> test generator enabled");			
		} else{ // default: RANDOM_GENERATOR
			walker = new RandomWalker(new Random(graphTime));
			System.out.println("<Random> test generator enabled");			
		}
		walkStopper = new WalkStopper();
		walker.walk(env, walkStopper);
	}
	
	public static String getReport(){
		System.out.println("TESTAR sequence graph dump on way ...");
		String report = env.toString();
		System.out.println("... finished TESTAR sequence graph dump");
		
        // begin - sync with following test sequence grapher (if any)
    	IEnvironment notifyEnv = env;
        env = null; // let next test sequence grapher start
    	synchronized(notifyEnv){
    		notifyEnv.notifyAll();
    	}
    	// end - sync
    	
		resetGrapherFields();
		
		return report;
	}
	
	public static void exit(){
		try {
			synchronized(env){
				while (env != null){
					try {
						env.wait();
					} catch (InterruptedException e) {
						System.out.println("TESTAR grapher exit interrupted");
					}
				}
			}
		} catch (Exception e) {} // env may be set to null when we try to sync on it
	}
	
	private static void resetGrapherFields(){
		System.out.println("TESTAR grapher reset");
		movementsFIFO.clear();
		movementsSync.clear();
		env = null;
		walkStopper = null;		
	}
	
	@Override
	public void finalize(){
		resetGrapherFields();
		if (exeSrv != null){
			exeSrv.shutdown();
			exeSrv = null;
		}
	}
	
}
