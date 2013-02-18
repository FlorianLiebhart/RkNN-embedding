package view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.GuiConstants;

import graph.core.Graph;
import graph.core.Vertex;


public class GuiUtil {
	
	public static void setNodesPosition(Graph g) {
	    final int MAX_ROWS   = 7;
	    
		int numberOfNodes = g.getAllVertices().size();
		
		int rowCount = (numberOfNodes / 6) + 1;
		if(rowCount == 1) {
			rowCount = 2; // at least 2 rows
		} else if(rowCount > MAX_ROWS) {
			rowCount = MAX_ROWS;
		}
		
		int xPos = GuiConstants.X_INIT_POS; // initial position
		int yPos = GuiConstants.Y_INIT_POS;
		int currentRow = 0;
		int created = 0;
		for(Vertex v : g.getAllVertices()) {
			if(currentRow != 0 && currentRow % rowCount == 0) {
				xPos += GuiConstants.X_DISTANCE; // back to top
				yPos = GuiConstants.Y_INIT_POS;
				currentRow = 0;
			}
			if(currentRow != 0) {
				yPos = yPos + GuiConstants.Y_DISTANCE;
			}
			
			v.setNodeLocation(xPos, yPos);
			created++;
			currentRow++;
			System.out.println("Setting Nodes Position... (" + (created + 1) + " / " + numberOfNodes + ")");
		}
	}

	/**
	 * Create a panel containing the algorithm's information for the outlook bar
	 * 
	 * @param info : the algorithm's detailed information
	 * @return a JPanel containing the information
	 */
	public static JPanel createInfoPanel(String info) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		
		ta.setText("\n" + info);
		panel.add(new JScrollPane(ta));
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return panel;
	}
}
