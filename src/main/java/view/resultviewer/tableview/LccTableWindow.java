package view.resultviewer.tableview;

import graph.core.Graph;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.SwingWorker;

import util.GraphAlgorithm;

public class LccTableWindow extends AbstractTableWindow {
	
	public LccTableWindow(boolean useRelative) {
		super(useRelative);
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void setGreenColumnLocation() {
		ALT_COLUMN = columnNames.size() - 2;
	}

	@Override
	protected void setGlobalTitle() {
		this.setTitle("Largest Connected Component");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void addResults(SortedMap sortedData, Vector<Object> data) {
		int rank = 1;
		for (Iterator<Graph> iter = sortedData.keySet().iterator(); iter.hasNext();) {
			Graph g = (Graph) iter.next();
			double persen = Double.parseDouble((sortedData.get(g)).toString()) / samplesGenerated;
			Vector<Object> row = new Vector<Object>();
			row.add(rank);
			row.add(g);
			row.add(g.getNumberOfVertices());
			row.add(df.format(persen));
			row.add(new JButton("Show Nodes")); 
			data.add(row);
			rank++;
		}
	}
	
	@Override
	protected void initWorker() {
		worker = new SwingWorker<Object, Void>() {
			@Override
			public Object doInBackground() {
				while(true && !isCancelled()) {
					Graph sGraph = GraphAlgorithm.getLargestConnectedComponent();
					
					boolean found = false;
					for(Object g : results.keySet()) {
						if(sGraph.equalsIgnoreEdges((Graph) g)) {
							found = true;
							results.put(g, results.get(g) + 1);
							break;
						}
					}
					if(!found) {
						results.put(sGraph, 1);
					}
					samplesGenerated++;
					status.setText("Samples generated: " + NumberFormat.getNumberInstance(Locale.GERMAN).format(samplesGenerated));
				}
				return null;
			}
		};
	}

	@Override
	protected void createColumns() {
		columnNames = new Vector<String>();
		columnNames.add("Rank");
		columnNames.add("Graph-ID");
		columnNames.add("#Nodes");
		columnNames.add("Relative Frequency");
		columnNames.add("Viewer"); 
	}

	@Override
	protected String generateResultInfo() {
		return "No Info";
	}

}
