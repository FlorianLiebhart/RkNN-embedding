package util;

import graph.core.Vertex;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JOptionPane;

import view.resultviewer.ResultVisualizer;
import view.resultviewer.chartview.ConnectedComponentsChartWindow;
import view.resultviewer.chartview.DiameterChartWindow;
import view.resultviewer.chartview.ReachableNodesChartWindow;
import view.resultviewer.chartview.ShortestPathChartWindow;
import view.resultviewer.tableview.KnnTableWindow;
import view.resultviewer.tableview.LccTableWindow;
import view.resultviewer.tableview.RnnTableWindow;

public class AlgorithmQuery {

	private static AlgorithmQuery instance = null;
	
	private Vector<Integer> nodes;
	private boolean useRelative;
	private String algorithm;
	private String kNumber;
	private Component viewComponent;
	private ResultVisualizer visualizer;
	
	private AlgorithmQuery() {
		
	}
	
	public static AlgorithmQuery getInstance() {
		if(instance == null) {
			instance = new AlgorithmQuery();
		}
		return instance;
	}
	
	public void startSampling(Vector<Integer> vertices) {
		nodes = vertices;
		setupVisualizer();
	}
	public void setComponent(Component comp) {
		viewComponent = comp;
	}
	public void setAlgorithm(String algo) {
		algorithm = algo;
	}
	public void setUseRelative(boolean val) {
		useRelative = val;
	}
	public void setKNumber(String k) {
		kNumber = k;
	}
	
	private void setupVisualizer() {
		if (algorithm == AlgorithmStrings.SHORTEST_PATH) {
			setVisualizer(new ShortestPathChartWindow(nodes.get(0), nodes.get(1), useRelative)); 
		} 
		else if (algorithm == AlgorithmStrings.REACHABLE_NODES) {
			setVisualizer(new ReachableNodesChartWindow(nodes.get(0), useRelative));
		} 
		else if (algorithm == AlgorithmStrings.REVERSE_NN) {
			Vertex selected = Sampler.getInstance().getVertex(
					nodes.get(0));
			if (!selected.containsObject()) {
				JOptionPane
						.showMessageDialog(
								viewComponent,
								"The selected node has to contain an object!",
								"Reverse Nearest Neighbors Query",
								JOptionPane.WARNING_MESSAGE);
			} else {
				setVisualizer(new RnnTableWindow(nodes.get(0), useRelative));
			}
		} 
		else if (algorithm == AlgorithmStrings.KNN) {
			try {
				final int k = Integer.parseInt(kNumber);
//				visualizer = new KnnTableWindow( ALT
//						ProbabilisticGraph.getInstance(),
//						nodes.get(0), k,
//						useRelative);
				setVisualizer(new KnnTableWindow(nodes.get(0), k, useRelative));
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(
						viewComponent,
						"Please enter a number!\n("
								+ ex.getLocalizedMessage() + ")",
						"K-Nearest Neighbours", JOptionPane.ERROR_MESSAGE);
			}
		} 
		else if (algorithm == AlgorithmStrings.CONNECT_COMP) {
			setVisualizer(new ConnectedComponentsChartWindow(useRelative));
		} 
		else if (algorithm == AlgorithmStrings.LARGEST_CONNECT_COMP) {
			setVisualizer(new LccTableWindow(useRelative));
		} 
		else if (algorithm == AlgorithmStrings.DURCHMESSER) {
			setVisualizer(new DiameterChartWindow(useRelative));
		}
//        else if (algorithm == AlgorithmStrings.RKNN_NAIVE) {
//            try {
//                final int k = Integer.parseInt(kNumber);
//                setVisualizer(new RKnnSimpleTableWindow(nodes.get(0), k, useRelative));
//            } catch (NumberFormatException ex) {
//                JOptionPane.showMessageDialog(
//                        viewComponent,
//                        "Please enter a number!\n("
//                                + ex.getLocalizedMessage() + ")",
//                        "Reverse K-Nearest Neighbours", JOptionPane.ERROR_MESSAGE);
//            }
//        }
    }

	public ResultVisualizer getVisualizer() {
		return visualizer;
	}

	public void setVisualizer(ResultVisualizer visualizer) {
		this.visualizer = visualizer;
	}

}
