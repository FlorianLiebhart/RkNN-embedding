package view.resultviewer.tableview.old_NOTUSED;

import graph.ProbabilisticGraph;
import graph.core.Graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jfree.ui.RefineryUtilities;

import util.GraphAlgorithm;
import util.GuiConstants;
import view.resultviewer.ResultVisualizer;
import view.resultviewer.chartview.ChartConstants;
import view.resultviewer.tableview.NodeListViewer;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class LccTableWindow extends JFrame implements ActionListener, ResultVisualizer {
	private static final long serialVersionUID = 1L;
	
	protected String title;
	
	private JButton closeButton, startStopButton;
	private JLabel status;
	private DecimalFormat df;
	
	private HashMap<Graph, Integer> results;
	private SwingWorker<Object, Void> worker, updater;
	private int samplesGenerated;
	
	Vector<String> columnNames;
	private final int ALT_COLUMN;
	KnnTableModel tableModel;
	Vector<Vector<Object>> tableData;
	private JTable table;
	
	private boolean useRelative;
	private DefaultTableCellRenderer renderer;
	private Vector<Integer> greenRows = new Vector<Integer>();
	
	// graph stuffs..
	protected final ProbabilisticGraph graph;
	
	public LccTableWindow(ProbabilisticGraph graph, boolean useRelative) {
		super(ChartConstants.CHART_WINDOW_TITLE);
		this.graph = graph;
		this.useRelative = useRelative;
		results = new HashMap<Graph, Integer>();
		title = "Largest Connected Component";
		setTitle(title);
		createContent();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		ALT_COLUMN = columnNames.size() - 2;
		init();
		start();
	}
	
	private void createContent() {
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout());
		status = new JLabel();
		south.add(status, BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel();
		closeButton = new JButton(ChartConstants.CLOSE, new ImageIcon(getClass().getResource("/images/close.png")));
		startStopButton = new JButton(ChartConstants.STOP, new ImageIcon(getClass().getResource("/images/stop.png")));
		closeButton.addActionListener(this);
		startStopButton.addActionListener(this);
		buttonPanel.add(closeButton);
		buttonPanel.add(startStopButton);
		south.add(buttonPanel, BorderLayout.CENTER);
		    
		createTable();

	    df = new DecimalFormat("#.#####");
	    
	    setSize(640, 360);
	    getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
	    getContentPane().add(south, BorderLayout.SOUTH);
	}
	
	protected void start() {
		if(worker != null && updater != null) {
			worker.execute();
			updater.execute();
		}
	}
	
	protected void init() {
		updater = new SwingWorker<Object, Void>() {
			private int counter = 10;
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Object doInBackground() {
				while(true && !isCancelled()) {
					try {
						Thread.sleep(200);

						HashMap<Graph, Integer> tmp = (HashMap<Graph, Integer>) results.clone();
						int total = samplesGenerated;
						Vector<Vector<Object>> data = new Vector<Vector<Object>>();
						SortedMap sortedData = new TreeMap(new ValueComparer(tmp));
						sortedData.putAll(tmp);
						int rank = 1;
						for (Iterator<Graph> iter = sortedData.keySet().iterator(); iter.hasNext();) {
							Graph g = (Graph) iter.next();
							double persen = Double.parseDouble((sortedData.get(g)).toString()) / total;
							Vector<Object> row = new Vector<Object>();
							row.add(rank);
							row.add(g);
							row.add(g.getNumberOfVertices());
							row.add(df.format(persen));
							row.add(new JButton("Show Nodes")); 
							data.add(row);
							rank++;
						}
						
						
						tableData.clear();
						tableData.addAll(data);
						tableModel.fireTableDataChanged();
						
						if(counter % GuiConstants.GREENMARKER_STEPCOUNT == 0) {
							System.out.println("*** START GREEN CELL CALCULATION! ***");

//							greenRows.clear();
							int row = 0;
							for (Iterator<Graph> iter = sortedData.keySet().iterator(); iter.hasNext();) {
								Graph g = (Graph) iter.next();
								
								double aFreq = Double.parseDouble((sortedData.get(g)).toString());
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
								System.out.println("Row= " + g.toString() + "\tResult= " + ergebnis + 
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
		
		worker = new SwingWorker<Object, Void>() {
			@Override
			public Object doInBackground() {
				while(true && !isCancelled()) {
					Graph sGraph = getValue();
					
					boolean found = false;
					for(Graph g : results.keySet()) {
						if(sGraph.equalsIgnoreEdges(g)) {
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
	
	protected Graph getValue() {
		return GraphAlgorithm.getLargestConnectedComponent();
	}

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
			
		} 
	}
	
	private void createTable() {
		columnNames = new Vector<String>();
		columnNames.add("Rank");
		columnNames.add("Graph-ID");
		columnNames.add("#Nodes");
		columnNames.add("Relative Frequency");
		columnNames.add("Viewer"); 
		
		tableData = new Vector<Vector<Object>>();

		tableModel = new KnnTableModel(tableData, columnNames);
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer
            	(TableCellRenderer renderer,int Index_row, int Index_col) {
				Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
				//even index, selected or not selected
				if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
					comp.setBackground(new Color(237, 243, 254));
				} 
				else {
					comp.setBackground(Color.white);
				}  
				
				if(greenRows.contains(Index_row) && Index_col == ALT_COLUMN) {
					comp.setBackground(Color.green);
				}
				return comp;
			}
		};
		table.setEnabled(false);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(true);
		table.setGridColor(Color.LIGHT_GRAY);
	    table.setFillsViewportHeight(true);
	    
	    // edit header font & alignment TODO
	 	renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
	    table.setDefaultRenderer(Object.class, renderer);
	 	renderer.setHorizontalAlignment(JLabel.CENTER);
	 	Font bold = new Font(renderer.getFont().getName(), Font.BOLD, renderer.getFont().getSize());
	 	table.getTableHeader().setFont(bold);
	 	table.setRowHeight(25);
	 	DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	 	centerRenderer.setHorizontalAlignment( JLabel.CENTER );
	 	for(int i = 0; i < columnNames.size(); i++) {
	 		table.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );	
	 	}
	 	
	 	TableColumn column = null;
	 	for (int i = 0; i < columnNames.size(); i++) {
	 	    column = table.getColumnModel().getColumn(i);
	 	    if (i == 0) {
	 	        column.setPreferredWidth(20);
	 	    } else if(i == 1) {
	 	        column.setPreferredWidth(120);
	 	    } else if(i == 2) {
	 	    	column.setPreferredWidth(20);
	 	    	
	 	    }
	 	}

	 	TableCellRenderer buttonRenderer = new JTableButtonRenderer();
		table.getColumn(columnNames.elementAt(columnNames.size()-1)).setCellRenderer(buttonRenderer);
	 	
	 	table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());

				if(col == columnNames.size()-1) {
					try {
						Object val = table.getValueAt(row, 1);
						if(val instanceof Graph) {
							new NodeListViewer((Graph) val);
						}
					} catch(Exception ex) {
						// ...
					}	
				}
			}
		});
		
	}
	
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
	
	private class KnnTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private Vector<Vector<Object>> data;
		private Vector<String> columnNames;
		
		public KnnTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
			this.data = data;
			this.columnNames = columnNames;
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.size();
		}

		@Override
		public int getRowCount() {
			return data.size();
		}
		
		public String getColumnName(int col) {
	        return columnNames.elementAt(col);
	    }

		@Override
		public Object getValueAt(int arg0, int arg1) {
			return data.get(arg0).get(arg1);
		}
		
	}
	
	private class JTableButtonRenderer implements TableCellRenderer {		
		  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    JButton button = (JButton)value;
		    if (isSelected) {
		      button.setForeground(table.getSelectionForeground());
		      button.setBackground(table.getSelectionBackground());
		    } else {
		      button.setForeground(table.getForeground());
		      button.setBackground(UIManager.getColor("Button.background"));
		    }

		    return button;	
		  }
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startSampling() {
		init();
		start();
	}

	@Override
	public void stopSampling() {
		worker.cancel(false);
		updater.cancel(false);
	}
}
