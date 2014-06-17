package view.resultviewer.tableview;

import graph.core.Vertex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jfree.ui.RefineryUtilities;

import util.GuiConstants;
import util.Sampler;
import view.resultviewer.ResultVisualizer;
import view.resultviewer.chartview.ChartConstants;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public abstract class AbstractTableWindow extends JFrame implements ActionListener, ResultVisualizer {
	private static final long serialVersionUID = 1L;
	
	protected String title;
	
	private JButton closeButton, startStopButton, infoButton;
	protected JLabel status;
	protected DecimalFormat df;
	
	protected HashMap<Object, Integer> results;
	protected SwingWorker<Object, Void> worker, updater;
	protected int samplesGenerated;
	
	DefaultTableModel tableModel;
	Vector<Object> tableData;
	private JTable table;
	
	private boolean useRelative;
	
	protected Vector<String> columnNames;
	
	private Vector<Integer> greenRows = new Vector<Integer>();
	protected int ALT_COLUMN;
	
	// graph stuffs..
	protected Vertex start  = null;
	protected int k;
	
	public AbstractTableWindow(int start, int k, boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.start = Sampler.getInstance().getVertex(start);
		this.k = k;		
		this.useRelative = useRelative;
		init();
	}
	
	public AbstractTableWindow(int start, boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.start = Sampler.getInstance().getVertex(start);
		this.useRelative = useRelative;
		init();
	}
	
	public AbstractTableWindow(boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.useRelative = useRelative;
		init();
	}
	
	private void init() {
		results = new HashMap<Object, Integer>();
		setGlobalTitle();
		createContent();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		setGreenColumnLocation();
		initCalculation();
		start();
	}

	protected abstract void setGreenColumnLocation();
	protected abstract void setGlobalTitle();
	
	private void createContent() {
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout());
		status = new JLabel();
		south.add(status, BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel();
		closeButton = new JButton(ChartConstants.CLOSE, new ImageIcon(getClass().getResource("/images/close.png")));
		infoButton = new JButton(ChartConstants.CHART_INFO_BUTTON, new ImageIcon(getClass().getResource("/images/info.png")));
		startStopButton = new JButton(ChartConstants.STOP, new ImageIcon(getClass().getResource("/images/stop.png")));
		closeButton.addActionListener(this);
		infoButton.addActionListener(this);
		startStopButton.addActionListener(this);
		buttonPanel.add(infoButton);
		buttonPanel.add(closeButton);
		buttonPanel.add(startStopButton);
		south.add(buttonPanel, BorderLayout.CENTER);
		    
		createTable();

	    df = new DecimalFormat("#.#####");
	    
	    setSize(400, 320);
	    getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
	    getContentPane().add(south, BorderLayout.SOUTH);
	}
	
	protected void start() {
		if(worker != null && updater != null) {
			worker.execute();
			updater.execute();
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected abstract void addResults(SortedMap sortedData, Vector<Object> data);
	
	protected void initCalculation() {
		initUpdater();
		
		initWorker();
	}
	
	private void initUpdater() {
		updater = new SwingWorker<Object, Void>() {
			private int counter = 0;
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Object doInBackground() {
				while(true && !isCancelled()) {
					try {
						Thread.sleep(200);

						HashMap<Object, Integer> tmp = (HashMap<Object, Integer>) results.clone();
						int total = samplesGenerated;
						Vector<Object> data = new Vector<Object>();
						SortedMap sortedData = new TreeMap(new ValueComparer(tmp));
						sortedData.putAll(tmp);
						addResults(sortedData, data);
						
						tableData.clear();
						tableData.addAll(data);
						tableModel.fireTableDataChanged();
						
						if(counter % GuiConstants.GREENMARKER_STEPCOUNT == 0) {
							util.Log.appendln("*** START GREEN CELL CALCULATION! ***");

//							greenRows.clear();
							int row = 0;
							for (Iterator<Integer> iter = sortedData.keySet().iterator(); iter.hasNext();) {
								Integer key = (Integer) iter.next();
								
								double aFreq = Double.parseDouble((sortedData.get(key)).toString());
								double cFreq = aFreq / total;
								double std = Math.sqrt(cFreq * (1.0 - cFreq) * total);
								double untere, obere;
								if(useRelative) {
									// relativ
									untere = aFreq * (1 - ChartConstants.CONFIDENCE_STD_VAL);
									obere  = aFreq * (1 + ChartConstants.CONFIDENCE_STD_VAL);
								} else {
									//absolut
									untere = aFreq - (ChartConstants.CONFIDENCE_STD_VAL * total);
									obere  = aFreq + (ChartConstants.CONFIDENCE_STD_VAL * total);
								}
								
								
								Normal nor = new Normal(aFreq, std, RandomEngine.makeDefault());
								double ergebnis = nor.cdf(obere) - nor.cdf(untere); 
								
								if(ergebnis >= ChartConstants.SIGNIFICANCE_STD_VAL) { 
									if(!greenRows.contains(row)) {
										greenRows.add(row);
									}
								}
								util.Log.appendln("Column= " + key + "\tResult= " + ergebnis + 
										           "\t[Max. Dev @ " + ChartConstants.CONFIDENCE_STD_VAL + " ; Sig. Level @ " + 
										           ChartConstants.SIGNIFICANCE_STD_VAL + "]");
								row++;
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
	}
	
	protected abstract void initWorker();

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeButton) {
			stopSampling();
			dispose();
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
			showInfo();
		}
	}
	
	protected abstract void createColumns();
	
	private void createTable() {
		createColumns();
		
		tableData = new Vector<Object>();
		tableModel = new DefaultTableModel(tableData, columnNames);
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer
            	(TableCellRenderer renderer,int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				//even index, selected or not selected
				if (row % 2 == 0 && !isCellSelected(row, col)) {
					comp.setBackground(new Color(237, 243, 254));
				} 
				else {
					comp.setBackground(Color.white);
				}
				
				if(greenRows.contains(row) && col == ALT_COLUMN) {
					comp.setBackground(Color.green);
				}
				return comp;
			}
		};
		table.setRowHeight(25);
		table.setEnabled(false);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(true);
		table.setGridColor(Color.LIGHT_GRAY);
	    table.setFillsViewportHeight(true);
	    
	    // edit header font & alignment
	 	DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
	 	renderer.setHorizontalAlignment(JLabel.CENTER);
	 	Font bold = new Font(renderer.getFont().getName(), Font.BOLD, renderer.getFont().getSize());
	 	table.getTableHeader().setFont(bold);
	 	
	 	TableColumn column = null;
	 	for (int i = 0; i < columnNames.size(); i++) {
	 	    column = table.getColumnModel().getColumn(i);
	 	   util.Log.appendln(column.getWidth());
	 	    if (i == 0) {
	 	        column.setPreferredWidth(30);
	 	    } 
	 	}
	 	
	 	DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	 	centerRenderer.setHorizontalAlignment( JLabel.CENTER );
	 	for(int i = 0; i < columnNames.size(); i++) {
	 		table.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
	 	}
	 	
	}
	
	private void showInfo() {
		final JFrame frame = new JFrame();
		frame.setTitle(ChartConstants.CHART_WINDOW_TITLE);
		JTextPane pane = new JTextPane();
      	pane.setEditable(false);
      	pane.setContentType("text/html");
		pane.setText(generateResultInfo());
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
	
	protected abstract String generateResultInfo();
	
	@SuppressWarnings("rawtypes")
	private static class ValueComparer implements Comparator {
		private Map data = null;
		public ValueComparer (Map data){
			super();
			this.data = data;
		}
		
         @SuppressWarnings({"unchecked" })
		public int compare(Object o2, Object o1) {
        	 Comparable value1 = (Comparable) data.get(o1);
        	 Comparable value2 = (Comparable) data.get(o2);
        	 int c = value1.compareTo(value2);
        	 if (0 != c)
        	 return c;
        	 Integer h1 = o1.hashCode(), h2 = o2.hashCode();
        	 return h1.compareTo(h2);
         }
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startSampling() {
		initCalculation();
		start();
	}

	@Override
	public void stopSampling() {
		worker.cancel(false);
		updater.cancel(false);
	}
}
