package app;
import javax.swing.SwingUtilities;

import view.MainWindow;


public class GUIStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		Graph g = GraphGenerator.generateProbabilisticGraph(4, 5, 1);
		
		util.Log.append("Nodes: ");
		for(int i = 0; i < g.getNumberOfVertices(); i++) {
			util.Log.append(g.getVerticesSet().get(i).toString() + " ");
		}
		util.Log.appendln();
		util.Log.appendln("Edges: ");
		for(int i = 0; i < g.getNumberOfEdges(); i++) {
			Edge e = g.getEdgesSet().get(i);
			util.Log.appendln((i+1) + ". " + e + " > Prob: " + g.getProbability(e));
		} 
		util.Log.appendln("\nHas Connection between 1-3: " + g.containsEdge(1, 3));
		*/
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainWindow("Projektarbeit");
			}
		});
	}

}
