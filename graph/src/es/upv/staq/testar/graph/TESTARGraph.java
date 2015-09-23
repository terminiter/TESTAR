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
import java.util.List;

import org.jgrapht.graph.DirectedMultigraph;

/**
 * Graph representation for TESTAR.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class TESTARGraph extends DirectedMultigraph<IGraphState, IGraphAction> {

	private static final long serialVersionUID = -6766749840561297953L;
	
	List<IGraphAction> orderedSequenceActions;
	int actionOrder = -1;

	private TESTARGraph() {
		super(IGraphAction.class);
		orderedSequenceActions = new ArrayList<IGraphAction>();
		actionOrder = 0;
	}
		
	// by urueda
	public static TESTARGraph buildEmptyGraph(){
		return new TESTARGraph();
	}

	/*public static TESTARGraph GenerateGraph(String s){
		TESTARGraph ret = new TESTARGraph();
		
		ret.addVertex(s);
		
		int n = 30;
	    String[] states = new String[n];
	    for(int i = 0; i < n; i++){
	    	states[i] = Integer.toString(i);
	    	ret.addVertex(states[i]);
	    }
	    
	    ret.addEdge(s, s);
	    return ret;
	}*/

	/*public static TESTARGraph LoadGraph(String fileName){
	    FileReader in = null;        
	    
	    try {
	      	File f = new File(fileName);
	        in = new FileReader(f);
	        Parser p = new Parser(in);
	        p.parse(in);
	        Graph gg = p.getGraphs().get(0);
	       	//System.out.println(gg.toString());
	
	       	TESTARGraph ret = new TESTARGraph();
	       	
	       	for(Node n : gg.getNodes(true))
	       		ret.addVertex(n.getId().getId());
	
	       	for(com.alexmerz.graphviz.objects.Edge e : gg.getEdges()){
	       		ret.addEdge(e.getSource().getNode().getId().getId(), e.getTarget().getNode().getId().getId(), new NamedEdge(e.getAttribute("label").toString()));
	       	}
	       	
	       	return ret;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return null;
	}*/
		
	public boolean addVertex(IEnvironment env, IGraphState v){
		if (this.containsVertex(v)){
			v.incCount();
			return false;
		} else {
			return super.addVertex(v);
		}
	}
	
	private void edgeAdded(IGraphAction e){
		actionOrder++;
		e.addOrder(new Integer(actionOrder).toString());
		orderedSequenceActions.add(e);
	}
		
	/**
	 * Note: edge nodes must be added first.
	 * @see public boolean addVertex(IEnvironment env, IGraphState v)
	 */
	public boolean addEdge(IEnvironment env, IGraphState from, IGraphState to, IGraphAction e){
		boolean edgeAtGraph = true;
		if (this.containsEdge(e)){ // the edge already exists at graph
			edgeAtGraph &= this.getEdgeSource(e).toString().equals(from.toString()); // and the edge source state is the same
			edgeAtGraph &= this.getEdgeTarget(e).toString().equals(to.toString()); // but, edge target state might be different (perhaps because the SUT did not react on time to action)
		} else
			edgeAtGraph = false;
		if (edgeAtGraph){
			e.incCount();
			edgeAdded(e);
			return false;			
		} else{
			if (!e.getActionName().equals("START") && !e.getActionName().equals("STOP"))
				edgeAdded(e);
			return super.addEdge(from,to,e);
		}		
	}
	
	public List<IGraphAction> getOrderedActions(){
		return this.orderedSequenceActions;
	}
	
}
