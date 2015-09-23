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

package es.upv.staq.testar.graph.reporting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.fruit.alayer.actions.BriefActionRolesMap;

import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.graph.Grapher;
import es.upv.staq.testar.graph.IEnvironment;
import es.upv.staq.testar.graph.IGraphAction;
import es.upv.staq.testar.graph.IGraphState;
import es.upv.staq.testar.graph.TESTAREnvironment;
import es.upv.staq.testar.graph.TESTARGraph;

/**
 * Graoh reporting utility.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public class GraphReporter {

	private static long usingGraphTime = -1;

	private static String testSequenceFolder = null;
	
	private static final String OUT_DIR = "output/graphs/";
		
	public static void useGraphData(long graphTime, String testSequencePath){		
		GraphReporter.testSequenceFolder = testSequencePath.replaceAll(".*(sequence[0-9]*)", "$1");		
		usingGraphTime = graphTime;
		(new File(OUT_DIR + testSequenceFolder)).mkdirs();
	}
	
	// [0] = detailed/screenshoted, [1] = minimal graph (no details, no screenshots)
	private static String[] buildVertexGraph(TESTARGraph g){
		int unx = 1;
		String label;
		StringBuffer sb0 = new StringBuffer(), sb1 = new StringBuffer(), unxBuffer = new StringBuffer();;
		IGraphState verdictVertex = null;
		int unxC;
		for(IGraphState vertex : g.vertexSet()){
			if (vertex.toString().equals("PASS") || vertex.equals("FAIL"))
				verdictVertex = vertex;
			else{
				String nodeLabel = vertex.toString() + " (" + vertex.getCount() + ")";
				if (vertex.getStateshot() != null)
					label = "<<TABLE border=\"0\" cellborder=\"1\" color='#cccccc'><TR><TD><IMG SRC=\"" +
							vertex.getStateshot().replace("output","../..") +
							"\"/></TD></TR><TR><TD>" + nodeLabel +
							"</TD></TR></TABLE>>";
				else
					label = "\"" + nodeLabel + "\"";
				
				sb0.append(vertex.toString() + " [label=" + label + "];\n");
				sb1.append(vertex.toString() + " [label=\"" + nodeLabel + "\"];\n");
				
				unxC = vertex.getUnexploredActions().size();
				if (unxC > 0)
					unxBuffer.append(vertex.toString() + " -> unexplored_" + unx + " [color=pink, fontcolor=pink, label=\"unexplored(" + unxC + ")\", style=dashed];\n");
			}
			unx++;
		}
		if (unxBuffer.length() > 0){
			sb0.append("node [fixedsize=false, shape=ellipse, style=dashed, color=pink, fontcolor=pink, height=0.8];\n");
			sb1.append("node [fixedsize=false, shape=ellipse, style=dashed, color=pink, fontcolor=pink, height=0.8];\n");
			sb0.append(unxBuffer.toString());
			sb1.append(unxBuffer.toString());
		}
		if (verdictVertex != null){
			String verdictColor = (verdictVertex.toString().equals("PASS")) ? "green" : "red";
			String verdictFontColor = (verdictVertex.toString().equals("PASS")) ? "black" : "white";
			sb0.append("node [fixedsize=false, shape=doublecircle, style=filled, color=" + verdictColor + ", fontcolor=" + verdictFontColor + ", height=0.8];\n");
			sb1.append("node [fixedsize=false, shape=doublecircle, style=filled, color=" + verdictColor + ", fontcolor=" + verdictFontColor + ", height=0.8];\n");
			sb0.append(verdictVertex + ";\n");
			sb1.append(verdictVertex + ";\n");
		}
		return new String[]{sb0.toString(),sb1.toString()};
	}
	
	// [0] = detailed/screenshoted, [1] = minimal graph (no details, no screenshots)	
	private static String[] buildEdgeGraph(TESTARGraph g){
		String label, color, detailedS, linkLabel;
		StringBuffer sb0 = new StringBuffer(), sb1 = new StringBuffer();
		for(IGraphAction edge : g.edgeSet()){
			sb0.append(g.getEdgeSource(edge) + " -> " + g.getEdgeTarget(edge));
			sb1.append(g.getEdgeSource(edge) + " -> " + g.getEdgeTarget(edge));
			if (edge.getCount() == 1)
				color = "grey";
			else if (edge.getCount() < 5)
				color = "black";
			else if (edge.getCount() < 10)
				color = "blue";
			else if (edge.getCount() < 25)
				color = "green";
			else if (edge.getCount() < 50)
				color = "yellow";
			else
				color = "red";
			//detailedS = ai.getDetailedName() == null ? "" : "\n{" + ai.getDetailedName() + "}";
			detailedS = "";
			linkLabel = edge.getActionName() + " " + edge.getOrder() + " (" + edge.getCount() + ")" + detailedS;
			if (edge.getStateshot() != null)
				label = "<<TABLE border=\"0\" cellborder=\"1\" color='#cccccc'><TR><TD><IMG SRC=\"" +
						edge.getStateshot().replace("output","../..") +
					"\"/></TD></TR><TR><TD>" +
					//linkLabel.replaceAll("\n", "<br/>") +
					linkLabel +
					"</TD></TR></TABLE>>";
			else
				label = "\"" + linkLabel + "\"";						
			sb0.append(" [color=" + color + ", label=" + label + ", style=solid];\n");

			//label = label.replaceAll("(.*->.*)((?:\\n|<br/>)\\{(?s)[^\\}]*(?-s)\\})?(.*;)","$1$2"); // no detailed edges/actions
			sb1.append(" [color=" + color + ", label=\"" + linkLabel + "\", style=solid];\n");
		}
		return new String[]{sb0.toString(),sb1.toString()};
	}
	
	// [0] = detailed/screenshoted, [1] = minimal graph (no details, no screenshots)	
	public static String[] buildGraph(TESTARGraph g){
		StringBuilder sb0 = new StringBuilder(), sb1 = new StringBuilder();
		sb0.append("digraph TESTAR {\n");
		sb0.append("rankdir=LR;\n");
		sb0.append("node [shape=point];\n");
		sb0.append("ENTRY;\n");
		sb0.append("node [fixedsize=false, shape=rect, style=solid, color=black, fontcolor=black, height=0.8];\n");
		sb1.append(sb0.toString());

		String[] vertexGraph = buildVertexGraph(g);
		sb0.append(vertexGraph[0]);
		sb1.append(vertexGraph[1]);
		String[] edgeGraph = buildEdgeGraph(g);
		sb0.append(edgeGraph[0]);
		sb1.append(edgeGraph[1]);
		
		sb0.append("}\n");
		sb1.append("}\n");
		
		return new String[]{sb0.toString(),sb1.toString()};
	}
	
	private static void dotConverter(String nullshotDotPath,
									 String scrshotedDotPath,
									 String minimalDotPath){
		// save as SVG/PNG (tool: http://www.graphviz.org/)
		File outdirF = new File(OUT_DIR + testSequenceFolder + "/");
		try {
			Process p1 = Runtime.getRuntime().exec("dot.exe -Tsvg " + nullshotDotPath + " -o " + nullshotDotPath + ".svg",
									              null, outdirF);
			Process p2 = Runtime.getRuntime().exec("dot.exe -Tsvg " + scrshotedDotPath + " -o " + scrshotedDotPath + ".svg",
									  null, outdirF);
			Process p3 = Runtime.getRuntime().exec("dot.exe -Tsvg " + minimalDotPath + " -o " + minimalDotPath + ".svg",
					  null, outdirF);
			try {
				int p1Status = p1.waitFor();
				if (p1Status != 0)
					System.out.println("WARNING: dot2svg exit value = " + p1Status);
				int p2Status = p2.waitFor();
				if (p2Status != 0)
					System.out.println("WARNING: dot2svg (scrshoted) exit value = " + p2Status);
				int p3Status = p3.waitFor();
				if (p3Status != 0)
					System.out.println("WARNING: dot2svg (minimal) exit value = " + p3Status);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// PNG version discarded (due to large explored spaces)
		} catch (IOException e) {
			System.out.println("Unable to convert graphs from .dot to .svg ... is tools\\graphviz-2.38\\release\\bin at PATH environment variable?");
		}		
	}
	
	public static void saveGraph(TESTARGraph g){
		String[] graphString = buildGraph(g);
		String scrshotedGraph = graphString[0];		
		String nullshotGraph = graphString[1];
		String minimalGraph = nullshotGraph.replaceAll("(.*)label=\".*[(](.*)[).*]\"(.*;)", "$1label=\"$2\"$3")
										   .replaceAll("(.*)label=\"unexplored_.*\"(.*;)", "$1label=\"\"$2")
										   .replaceAll("shape=rect(.*)height=0.8];","shape=rect$1height=0.3];")
										   .replaceAll("shape=ellipse(.*)height=0.8];","shape=point$1height=0.3];");
						
		PrintWriter writer;
        final String scrshotedDotPath = "graph_" + usingGraphTime + "_scrshoted.dot";
        final String nullshotDotPath = "graph_" + usingGraphTime + ".dot";
        final String minimalDotPath = "graph_" + usingGraphTime + "_minimal.dot";
		try {
			writer = new PrintWriter(OUT_DIR + testSequenceFolder + "/" + scrshotedDotPath, "UTF-8");
			writer.println(scrshotedGraph);
			writer.close();
			writer = new PrintWriter(OUT_DIR + testSequenceFolder + "/" + nullshotDotPath, "UTF-8");
			writer.println(nullshotGraph);
			writer.close();
			writer = new PrintWriter(OUT_DIR + testSequenceFolder + "/" + minimalDotPath, "UTF-8");
			writer.println(minimalGraph);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		new Thread(){
			public void run(){
				dotConverter(nullshotDotPath, scrshotedDotPath, minimalDotPath);
			}
		}.start();
	}
	
	private static String reportGraphStats(IEnvironment env, TESTARGraph tGraph){
		StringBuffer report = new StringBuffer();
		
		report.append("\n-- TESTAR graph report ... --\n");
		IGraphAction[] orderedActions = env.getSortedActionsByOrder(Integer.MIN_VALUE, Integer.MAX_VALUE);
		int SEQUENCE_LENGTH = (int)Math.log10((double)orderedActions.length) + 1;
		if (SEQUENCE_LENGTH < 4)
			SEQUENCE_LENGTH = 4; // minimum column width
		reportDetailed(report,env,tGraph,orderedActions,SEQUENCE_LENGTH);
		reportSummary(report,env,tGraph,orderedActions,SEQUENCE_LENGTH);
		report.append("\n\n-- ... report end --");
		
		return report.toString();
	}
	
	private static void reportDetailed(StringBuffer report, IEnvironment env, TESTARGraph tGraph,
									   IGraphAction[] orderedActions, int SEQUENCE_LENGTH){
		report.append("\n\tACTION_TYPES:\n");
		for (String actionRole : BriefActionRolesMap.map.keySet())
			report.append("\t\t" + BriefActionRolesMap.map.get(actionRole) + " = " + actionRole + "\n");

		report.append("\n=== Ordered test sequence actions list ===\n");
		int ID_LENGTH = CodingManager.ID_LENTGH;
		String tableHead = String.format("%1$" + SEQUENCE_LENGTH + "s %9$" + SEQUENCE_LENGTH + "s %2$" + ID_LENGTH + "s %3$" + SEQUENCE_LENGTH + "s %4$" + ID_LENGTH + "s %5$" + SEQUENCE_LENGTH + "s %6$" + ID_LENGTH + "s %7$" + SEQUENCE_LENGTH + "s %8$s",
				"#",
				"FROM",
				"x",
				"TO",
				"x",
				"ACTION",
				"x",
				"ACTION_TYPE ( (WIDGET,ROLE,TITLE)[parameter*] )+ ",
				"Sync");
		for (int i=0; i<tableHead.length(); i++)
			report.append("-");
		report.append("\n" + tableHead + "\n");
		for (int i=0; i<tableHead.length(); i++)
			report.append("-");
		report.append("\n");
		
		int i=1;
		IGraphState from, to;
		List<Integer> movesSync = Grapher.getMovementsSync();
		for (IGraphAction edge : orderedActions){
			from = env.getSourceState(edge);
			to =  env.getTargetState(edge);			
			String actionList = String.format("%1$" + SEQUENCE_LENGTH + "d %9$" + SEQUENCE_LENGTH + "d %2$" + ID_LENGTH + "s %3$" + SEQUENCE_LENGTH + "d %4$" + ID_LENGTH + "s %5$" + SEQUENCE_LENGTH + "d %6$" + ID_LENGTH + "s %7$" + SEQUENCE_LENGTH + "d %8$s",
					i,
					from.toString(),
					from.getCount(),
					to.toString(),
					to.getCount(),
					edge.getActionName(),
					edge.getCount(),
					edge.getDetailedName(),
					movesSync.get(i-1));
			report.append(actionList + "\n");
			i++;
		}
	}
	
	private static void reportSummary(StringBuffer report, IEnvironment env, TESTARGraph tGraph,
									  IGraphAction[] orderedActions, int SEQUENCE_LENGTH){
		int unxStates = 0, unxActions = 0,
			totalStates = -1; // discard start node
		String verdict = null;
		for (IGraphState vertex : tGraph.vertexSet()){
			if (vertex.getUnexploredActions().size() > 0)
				unxStates++;
			unxActions += vertex.getUnexploredActions().size();;
			if (vertex.toString().equals("FAIL"))
				verdict = "FAIL";
			else if (vertex.toString().equals("PASS"))
				verdict = "PASS";
			else
				totalStates += vertex.getCount();					
		}
		
		report.append("\n=== SUT UI space explored ===\n");
		String statsMetahead = String.format("%1$23s %2$23s",
				"_________________STATES",
				"_________________ACTIONS");
		report.append(statsMetahead + "\n");
		String statsHead = String.format("%1$5s %2$6s %3$10s %4$5s %5$6s %6$10s ... %7$7s",
				"total",
				"unique",
				"unexplored",
				"total",
				"unique",
				"unexplored",
				"verdict");		
		report.append(statsHead + "\n");
		String stats = String.format("%1$5s %2$6s %3$10s %4$5s %5$6s %6$10s ... %7$4s",
				totalStates,
				tGraph.vertexSet().size() - 2, // without start/end states
				">" + unxStates + " <?",
				orderedActions.length,
				tGraph.edgeSet().size() - 2, // without start/end edges
				unxActions,
				verdict == null ? "????" : verdict);
		report.append(stats + "\n");		
		
		report.append("\n=== Test generator ===\n");
		report.append("Name: " + Grapher.testGenerator + "\n");
		if (Grapher.testGenerator.equals(Grapher.QLEARNING_GENERATOR)){
			report.append("DISCOUNT: " + Grapher.QLEARNING_DISCOUNT_PARAM + "\n");
			report.append("MAXREWARD: " + Grapher.QLEARNING_MAXREWARD_PARAM);
		}
		
		/*report.append("\n=== Stats report ===\n");

		double sequenceLength = orderedActions.length;
		int count;
		double frac, idealFrac = 1.0 / tGraph.edgeSet().size();
		double dist = 0;
		for (IGraphAction ga : tGraph.edgeSet()){
			count = ga.getCount();;
			frac = count / sequenceLength;
			if (frac < idealFrac)
				dist += (count > 0 ? idealFrac / frac : 100 * (idealFrac / (1.0 / sequenceLength))) - 1.0; 
		}
		//dist = Math.sqrt(dist);
		
		double E = sequenceLength / tGraph.edgeSet().size();				
		report.append("Mean: " + E + "\n");
		report.append("Distance: " + dist +"\n");*/		
	}	

	public static String PrintResults(TESTAREnvironment env, TESTARGraph g){
		if (g.vertexSet().isEmpty() || g.edgeSet().isEmpty())
			return "EMPTY GRAPH";
				
		saveGraph(g);
		String report = reportGraphStats(env,g);

		/*String reportPath = OUT_DIR + testSequenceFolder + "/" + 
				 			"graph_" + usingGraphTime + "_report.txt";
		try {
			PrintWriter writer = new PrintWriter(reportPath, "UTF-8");
			writer.println(report);
			writer.close();
			System.out.println("Graph report saved to: " + reportPath);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
					
		return report;
	}
	
}
