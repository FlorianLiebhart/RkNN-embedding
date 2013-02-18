package view.resultviewer.tableview;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;

import javax.swing.SwingWorker;

import util.GraphAlgorithm;
import util.Sampler;

public class RnnTableWindow extends AbstractTableWindow {
	private static final long serialVersionUID = 1L;
	
	public RnnTableWindow(int start, boolean useRelative) {
		super(start, useRelative);
	}

	@Override
	protected void setGreenColumnLocation() {
		ALT_COLUMN = columnNames.size() - 1;
	}

	@Override
	protected void setGlobalTitle() {
		this.setTitle("Reverse Nearest Neighbors from Node \"" + start + "\"");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void addResults(SortedMap sortedData, Vector<Object> data) {
		int rank = 1;
		for (Iterator<Integer> iter = sortedData.keySet().iterator(); iter.hasNext();) {
			Integer key = (Integer) iter.next();
			double persen = Double.parseDouble((sortedData.get(key)).toString()) / samplesGenerated;
			Vector<Object> row = new Vector<Object>();
			row.add(rank);
			row.add(Sampler.getInstance().getVertex(key).getObjectId()); 
			row.add("Node " + key);
			row.add(df.format(persen));
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
					Vector<Integer> val = GraphAlgorithm.getReverseNearestNeighbors(start);
					for(Integer v : val) {
						if(results.containsKey(v)) {
							results.put(v, results.get(v) + 1);
						} else {
							results.put(v, 1);
						}
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
		columnNames.add("Object-ID");
		columnNames.add("Location");
		columnNames.add("Relative Frequency");
	}

	@Override
	protected String generateResultInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>" + title + "</b><br /><br />");
		sb.append("<table border=\"1\"><tr><th>Object-ID</th><th>Node-ID</th><th>Relative Frequency</th></tr>");
		Set<Object> t = results.keySet();
		Object[] sorted = t.toArray();
		Arrays.sort(sorted);
		DecimalFormat df = new DecimalFormat("#.#####");
		for(Object sortedVal : sorted) {
			double val    = (double) results.get(sortedVal);
			double persen = val / samplesGenerated;
			int nodeId = Integer.parseInt(sortedVal.toString());
			sb.append("<tr><td align=\"center\">" + Sampler.getInstance().getVertex(nodeId).getObjectId() + "</td><td align=\"center\">" + nodeId + 
					  "</td><td align=\"center\">" + df.format(persen) + "</td></tr>");
		}
		sb.append("</table><br /><i>(" + NumberFormat.getNumberInstance(Locale.GERMAN).format(samplesGenerated) + " Samples)</i></html>");
		return sb.toString();
	}

}
