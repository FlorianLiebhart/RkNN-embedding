package view.resultviewer.tableview;

import graph.core.Graph;
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

public class NodeListViewer extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Graph graph;
	private JButton closeButton;
	private JTextPane pane;
	
	public NodeListViewer(Graph graph) {
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

					// nodes
					sb.append("<br /><b>Nodes:</b><br />");
					sb.append("<table width=\"100%\" border=\"1\"><tr><th>Nr</th><th>Node-ID</th></tr>");
					int nr = 1;
					for(Vertex v : graph.getAllVertices()) {
						sb.append("<tr><td align=\"center\">" + (nr++) + "</td><td align=\"center\">" + v + "</td></tr>");
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
