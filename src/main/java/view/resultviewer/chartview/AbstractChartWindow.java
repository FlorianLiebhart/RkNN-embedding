package view.resultviewer.chartview;

import graph.core.Vertex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import util.GuiConstants;
import util.Sampler;
import view.resultviewer.ResultVisualizer;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public abstract class AbstractChartWindow extends JFrame implements ActionListener, ResultVisualizer {
	private static final long serialVersionUID = 1L;
	
	protected String windowTitle;
	protected String barDescription;
	
	private JButton closeButton, saveButton, startStopButton, infoButton;
	private JLabel status;
	
	
	private boolean useRelative;
	
	// chart stuffs..
	private JFreeChart chart;
	private DefaultCategoryDataset dataset;
	private HashMap<Double, Integer> results;
	private SwingWorker<Object, Void> worker, updater;
	private int samplesGenerated;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock(); // writeLock = lock.writeLock();
	
	// graph stuffs..
	protected Vertex start  = null;
	protected Vertex target = null;
	
	private BarRenderer barRenderer;
	@SuppressWarnings("rawtypes")
	private Vector<Comparable> greenColumns = new Vector<Comparable>();
	
	public AbstractChartWindow(int start, int target, boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.start = Sampler.getInstance().getVertex(start);
		this.target = Sampler.getInstance().getVertex(target);
		this.useRelative = useRelative;
		setGlobalTitle();
		setBarDescription();
		createContent();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		
		results = new HashMap<Double, Integer>();
		startCalculation();
		start();
	}
	
	public AbstractChartWindow(int start, boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.start = Sampler.getInstance().getVertex(start);
		this.useRelative = useRelative;
		setGlobalTitle();
		setBarDescription();
		createContent();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		
		results = new HashMap<Double, Integer>();
		startCalculation();
		start();
	}
	
	public AbstractChartWindow(boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.useRelative = useRelative;
		setGlobalTitle();
		setBarDescription();
		createContent();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		
		results = new HashMap<Double, Integer>();
		startCalculation();
		start();
	}
	
	protected abstract void setGlobalTitle();
	
	protected abstract void setBarDescription();
	
	private void createContent() {
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout());
		status = new JLabel();
		south.add(status, BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel();
		closeButton = new JButton(ChartConstants.CLOSE, new ImageIcon(getClass().getResource("/images/close.png")));
		saveButton = new JButton(ChartConstants.SAVE_BUTTON, new ImageIcon(getClass().getResource("/images/save.png")));
		infoButton = new JButton(ChartConstants.CHART_INFO_BUTTON, new ImageIcon(getClass().getResource("/images/info.png")));
		startStopButton = new JButton(ChartConstants.STOP, new ImageIcon(getClass().getResource("/images/stop.png")));
		closeButton.addActionListener(this);
		saveButton.addActionListener(this);
		infoButton.addActionListener(this);
		startStopButton.addActionListener(this);
		buttonPanel.add(infoButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);
		buttonPanel.add(startStopButton);
		south.add(buttonPanel, BorderLayout.CENTER);
		
		dataset = new DefaultCategoryDataset();
		
		boolean show = true;
		boolean toolTips = true;
	    boolean urls = false;
	    
	    chart = ChartFactory.createBarChart("", barDescription, "Relative Frequency", dataset, PlotOrientation.VERTICAL, show, toolTips, urls);
	    
	    CategoryPlot catPlot = chart.getCategoryPlot();
//	    catPlot.getRangeAxis().setRange(0.0, 1.0);  // fixed range
	    
//	    BarRenderer br = (BarRenderer) catPlot.getRenderer();
		barRenderer = new GreenBarRenderer();
		catPlot.setRenderer(barRenderer);
		
	    barRenderer.setMaximumBarWidth(ChartConstants.MAX_CHART_BAR_WIDTH); // set maximum bar width to 5% of chart
	    
	    ChartPanel panel = new ChartPanel(chart);
	    setSize(740, 480);
	    getContentPane().add(panel, BorderLayout.CENTER);
	    getContentPane().add(south, BorderLayout.SOUTH);
	}
	
	protected void start() {
		if(worker != null && updater != null) {
			updater.execute();
			worker.execute();
		}
	}

	protected void startCalculation() {
		System.out.println("Start calculation");
		updater = new SwingWorker<Object, Void>() {
			private int counter = 0; // for green marking
			
			@Override
			public Object doInBackground() {
				while(true && !isCancelled()) {
					try {
						Thread.sleep(ChartConstants.UPDATE_TIME_INTERVAL);
						readLock.lock();
						try {
							dataset.clear(); 
							Set<Double> t = results.keySet();
							Object[] sorted = t.toArray();
							Arrays.sort(sorted);
							
							for(Object sortedVal : sorted) {
								double aFreq = (double) results.get(sortedVal);
								double cFreq = aFreq / samplesGenerated;
								try {
									dataset.setValue(cFreq, barDescription, sortedVal.toString());
								} catch(UnknownKeyException e) {
									dataset.addValue(cFreq, barDescription, sortedVal.toString());
								}
							}
							
							if(counter % GuiConstants.GREENMARKER_STEPCOUNT == 0) {
								System.out.println("*** START GREEN BAR CALCULATION! ***");
								greenColumns.clear();
								for(Object sortedVal : sorted) {
									double aFreq = (double) results.get(sortedVal);
									double cFreq = aFreq / samplesGenerated;
									double std = Math.sqrt(cFreq * (1.0 - cFreq) * samplesGenerated);
									
									double untere, obere;
									if(useRelative) {
										// relativ
										untere = aFreq * (1 - ChartConstants.CONFIDENCE_STD_VAL);
										obere  = aFreq * (1 + ChartConstants.CONFIDENCE_STD_VAL);
									} else {
										//absolut
										untere = aFreq - (ChartConstants.CONFIDENCE_STD_VAL * samplesGenerated);
										obere  = aFreq + (ChartConstants.CONFIDENCE_STD_VAL * samplesGenerated);
									}
									
									
									Normal nor = new Normal(aFreq, std, RandomEngine.makeDefault());
									double ergebnis = nor.cdf(obere) - nor.cdf(untere); 
									
									if(ergebnis >= ChartConstants.SIGNIFICANCE_STD_VAL) { 
										greenColumns.add(sortedVal.toString());
									}
									System.out.println("Col= " + sortedVal + "\tResult= " + ergebnis + 
											           "\t[Max. Dev @ " + ChartConstants.CONFIDENCE_STD_VAL + " ; Sig. Level @ " + 
											           ChartConstants.SIGNIFICANCE_STD_VAL + "]");
								}
								chart.getCategoryPlot().setRenderer(barRenderer);
							}
						} finally {
							readLock.unlock();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					
					counter++;
				}
				return null;
			}
		};
		
		worker = new SwingWorker<Object, Void>() {
			@Override
			public Object doInBackground() {
				while(true && !isCancelled()) {
					double value = getValue();
					if(results.containsKey(value)) {
						results.put(value, results.get(value) + 1);
					} else {
						results.put(value, 1);
					}
					samplesGenerated++;
					status.setText("Samples generated: " + NumberFormat.getNumberInstance(Locale.GERMAN).format(samplesGenerated));
				}
				return null;
			}
		};
	}
	
	public void startSampling() {
		startCalculation();
		start();
	}
	
	public void stopSampling() {
		worker.cancel(false);
		updater.cancel(false);
	}
	
	public void update(Observable o, Object arg) { // TODO: noch nicht benutzt
		double value = Double.parseDouble(arg.toString());
		if(results.containsKey(value)) {
			results.put(value, results.get(value) + 1);
		} else {
			results.put(value, 1);
		}
		samplesGenerated++;
		status.setText("Samples generated: " + NumberFormat.getNumberInstance(Locale.GERMAN).format(samplesGenerated));
	}
	
	protected abstract double getValue();

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeButton) {
			stopSampling();
			dispose();
		} else if(e.getSource() == saveButton) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showSaveDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				String file = fc.getSelectedFile().getAbsolutePath();
				try {
					if(!file.toLowerCase().endsWith(".png")) {
						file += ".png";
					}
					ChartUtilities.saveChartAsPNG(new File(file), chart, 500, 300);
				} catch(IOException ex) {
					JOptionPane.showMessageDialog(this,	ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if(e.getSource() == startStopButton) {
			if(startStopButton.getText() == ChartConstants.STOP) {
				startStopButton.setText(ChartConstants.CONTINUE);
				startStopButton.setIcon(new ImageIcon(getClass().getResource("/images/continue.png")));
				if(worker != null) {
					stopSampling();
				}
			} else {
				startStopButton.setText(ChartConstants.STOP);
				startStopButton.setIcon(new ImageIcon(getClass().getResource("/images/stop.png")));
				startSampling();
			}
			
		} else if(e.getSource() == infoButton) {
			openInfoWindow();
		}
	}
	
	private void openInfoWindow() {
		final JFrame frame = new JFrame();
		frame.setTitle(ChartConstants.CHART_WINDOW_TITLE);
		JTextPane pane = new JTextPane();
      	pane.setEditable(false);
      	pane.setContentType("text/html");
      	StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>" + windowTitle + "</b><br /><br />");
		sb.append("<table border=\"1\"><tr><th>" + barDescription + "</th><th>Relative Frequency</th></tr>");
		readLock.lock();
		try {
			Set<Double> t = results.keySet();
			Object[] sorted = t.toArray();
			Arrays.sort(sorted);
			DecimalFormat df = new DecimalFormat("#.#####");
			for(Object sortedVal : sorted) {
				double val    = (double) results.get(sortedVal);
				double persen = val / samplesGenerated;
				sb.append("<tr><td align=\"center\">" + Double.parseDouble(sortedVal.toString()) + 
						  "</td><td align=\"center\">" + df.format(persen) + "</td></tr>");
			}
		} finally {
			readLock.unlock();
		}
		sb.append("</table><br /><i>(" + NumberFormat.getNumberInstance(Locale.GERMAN).format(samplesGenerated) + " Samples)</i></html>");
		pane.setText(sb.toString());
		pane.setCaretPosition(0);
		frame.getContentPane().add(new JScrollPane(pane));
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		JPanel south = new JPanel();
		south.add(close);
		frame.getContentPane().add(south, BorderLayout.SOUTH);
		frame.setSize(new Dimension(380, 380));
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
	
	private class GreenBarRenderer extends BarRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Paint getItemPaint(int row, int col) {
			Set<Double> t = results.keySet();
			Object[] sorted = t.toArray();
			Arrays.sort(sorted);

			for(int i = 0; i < sorted.length; i++) {
				if(col == i) {
					if(greenColumns.contains(sorted[i].toString())) {
//						System.out.println("GREEN -> column: " + col + " (" + sorted[i].toString() + ")");
						return Color.green;
					}
				}
			}
			
	        return Color.red;
	    }
	}
}
