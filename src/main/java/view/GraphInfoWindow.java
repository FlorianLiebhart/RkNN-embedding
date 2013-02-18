package view;

import graph.ProbabilisticGraph;
import graph.core.Edge;
import graph.core.Vertex;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import org.jfree.ui.RefineryUtilities;

public class GraphInfoWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ProbabilisticGraph graph;
	private JButton closeButton;
	private JTextPane pane;
	
	public GraphInfoWindow(ProbabilisticGraph graph) {
		super("Graph Info");
		this.graph = graph;
		createContents();
	}
	
	private void createContents() {
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		JPanel southPanel = new JPanel();
		southPanel.add(closeButton);
		
		pane = new JTextPane();
      	pane.setEditable(false);
      	pane.setContentType("text/html");
		fillInfos();
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);
		setSize(new Dimension(480, 480));
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}
	
	private void fillInfos() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				StringBuilder sb = new StringBuilder();
				sb.append("<html>");
				if(graph == null) {
					sb.append("<div align=\"center\" color=\"red\"><b>No graph created!</b></div>");
				} else {
					sb.append("<b>Number of Nodes                : " + graph.getNumberOfVertices() + "</b><br />");
					sb.append("<b>Number of Objects              : " + graph.getNumberOfObjectNodes() + "</b><br />");
					sb.append("<b>Number of Edges                : " + graph.getNumberOfEdges() + "</b><br />");
					sb.append("<b>Number of Edges with Probabilty: " + graph.getNumberOfUncertainEdges() + "</b><br />");
					
					//objects
					sb.append("<br /><b>Node Details:</b><br />");
					sb.append("<table width=\"100%\" border=\"1\"><tr><th>Node ID</th><th>Object ID</th></tr>");
					for(Vertex v : graph.getAllVertices()) {
						sb.append("<tr><td align=\"center\">" + v.getId() + 
								  "</td><td align=\"center\">"); 
						int oId = v.getObjectId();
						if(oId == -1) {
							sb.append("-</td></tr>");
						} else {
							sb.append("<b>" + v.getObjectId() + "</b></td></tr>");
						}
					}
					sb.append("</table><br />");
					
					// edges
					sb.append("<br /><b>Edges Details:</b><br />");
					sb.append("<table width=\"100%\" border=\"1\"><tr><th>Nr</th><th>Node 1</th><th>Node 2</th><th>Weight</th><th>Probability</th></tr>");
					int nr = 1;
					for(Edge e : graph.getAllEdges()) {
						sb.append("<tr><td align=\"center\">" + (nr++) + "</td><td align=\"center\">" + e.getSource() + "</td>" + 
								  "<td align=\"center\">" + e.getTarget() + "</td><td align=\"center\">" + e.getWeight() + "</td>");
						if(graph.getProbability(e) < 1.0) {
							sb.append("<td align=\"center\"><font color=\"green\">" + graph.getProbability(e) + "</font></td></tr>");
						} else {
							sb.append("<td align=\"center\">" + graph.getProbability(e) + "</td></tr>");
						}
					}
					sb.append("</table>");
				}
				sb.append("</html>");
				pane.setText(sb.toString());
				pane.setCaretPosition(0);
				
				return null;
			}
			
			@Override	
			protected void done() {
				setCursor(Cursor.getDefaultCursor());
		    }
			
		}.execute();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		processEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
