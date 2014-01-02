package core;
import javax.swing.SwingUtilities;

import view.MainWindow;


public class GUIStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		Graph g = GraphGenerator.generateProbabilisticGraph(4, 5, 1);
		
		System.out.print("Nodes: ");
		for(int i = 0; i < g.getNumberOfVertices(); i++) {
			System.out.print(g.getVerticesSet().get(i).toString() + " ");
		}
		System.out.println();
		System.out.println("Edges: ");
		for(int i = 0; i < g.getNumberOfEdges(); i++) {
			Edge e = g.getEdgesSet().get(i);
			System.out.println((i+1) + ". " + e + " > Prob: " + g.getProbability(e));
		} 
		System.out.println("\nHas Connection between 1-3: " + g.containsEdge(1, 3));
		*/
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainWindow("Projektarbeit");
			}
		});
	}

}
