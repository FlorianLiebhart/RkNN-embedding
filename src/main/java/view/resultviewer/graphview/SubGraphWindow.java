package view.resultviewer.graphview;

import graph.core.Graph;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.ui.RefineryUtilities;

import view.GraphVisualizer;

public class SubGraphWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private Graph sGraph;
	
	private JButton closeButton;
	
	public SubGraphWindow(Graph g, String title) {
		super(title);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			setDefaultLookAndFeelDecorated(true);
	    }
	    catch (ClassNotFoundException e) {
	    	setDefaultLookAndFeelDecorated(true);
	    }
	    catch (InstantiationException e) {
	    	setDefaultLookAndFeelDecorated(true);
	    }
	    catch (IllegalAccessException e) {
	    	setDefaultLookAndFeelDecorated(true);
	    }
		
		sGraph = g;
		createContents();
		setSize(540, 480);
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}
	
	private void createContents() {
		JPanel southPanel = new JPanel();
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		southPanel.add(closeButton);
		
		GraphVisualizer gPanel = new GraphVisualizer(false);
		gPanel.setGraph(sGraph);
		
		this.getContentPane().add(gPanel, BorderLayout.CENTER);
		this.getContentPane().add(southPanel, BorderLayout.SOUTH);
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeButton) {
			processEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
}