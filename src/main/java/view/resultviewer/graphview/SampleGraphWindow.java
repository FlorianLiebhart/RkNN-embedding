package view.resultviewer.graphview;

import graph.core.Graph;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.ui.RefineryUtilities;

import util.Sampler;
import view.GraphVisualizer;

public class SampleGraphWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private Graph sGraph;
	private JLabel probLabel;
	
	private JButton closeButton;
	
	public SampleGraphWindow() {
		super("Sampled Graph");
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
		
		sGraph = Sampler.getInstance().generateSampledGraph();
		createContents();
		setSize(640, 480);
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}
	
	private void createContents() {
		probLabel = new JLabel();
//		DecimalFormat df = new DecimalFormat("#.############");
		String message = "<html>Probabiliy of this graph is <font color=\"green\">" + Sampler.calculateSampleGraphPosibility(sGraph)
				+ "</font></html>";
		probLabel.setText(message);
		JPanel southPanel = new JPanel();
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		southPanel.add(closeButton);
		
		GraphVisualizer gPanel = new GraphVisualizer(false);
		gPanel.setGraph(sGraph);
		gPanel.setShowingEdgeWeight(true);
		
		this.getContentPane().add(gPanel, BorderLayout.CENTER);
		this.getContentPane().add(southPanel, BorderLayout.SOUTH);
		this.getContentPane().add(probLabel, BorderLayout.NORTH);
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeButton) {
			processEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
}
