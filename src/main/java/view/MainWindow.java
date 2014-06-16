package view;

import app.RkNNComparator;
import graph.GraphGenerator;
import graph.ProbabilisticGraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import graph.core.Graph;
import org.jfree.ui.RefineryUtilities;

import util.ActionCommands;
import util.AlgorithmQuery;
import util.AlgorithmStrings;
import util.GuiConstants;
import util.XmlUtil;
import view.resultviewer.chartview.ChartConstants;
import view.resultviewer.graphview.SampleGraphWindow;

public class MainWindow extends JFrame implements ActionListener, Observer {
	private static final long serialVersionUID = 1L;

	private boolean justStarted = true;

	private JMenuBar menuBar;
	private JMenu settingsMenu, infoMenu, hypoMenu;
	private JMenuItem saveGraphItem, showEdgeWeightItem, showEdgeProbItem,
			           showNodeIdItem, showObjectIdItem, editItem, 
			           noEditItem, useRelative, useAbsolut;
	private OutlookBar outlookBar;
	private int previousSelectedBar;
	private JLabel statusBar, graphLabel;
	private JFileChooser fc;
	private JButton sampleGraphButton, runButton, saveGraphButton, hypoButton, zoomInButton, zoomOutButton;
	private JToggleButton editModeToggle, showEdgeWeightButton,
			showEdgeProbButton, showNodeIdButton, showObjectIdButton;
	private JTextField kNNField;
	private JTextField rknnSimpleField;
	private JTextField rknnEagerField;
	private JTextField rknnEmbeddedField;
	private JTextField rknnEmbeddedFieldNumRefPoints;
	private GraphVisualizer graphPanel;

	// private ProbabilisticGraph probGraph;

	public MainWindow(String title) {
		super(title);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// setDefaultLookAndFeelDecorated(true);
		} catch (ClassNotFoundException e) {
			// setDefaultLookAndFeelDecorated(true);
		} catch (InstantiationException e) {
			// setDefaultLookAndFeelDecorated(true);
		} catch (IllegalAccessException e) {
			// setDefaultLookAndFeelDecorated(true);
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		createMenus();
		createToolbar();
		createContents();
		setSize(1024, 640);

		// prepare graph algorithm
		AlgorithmQuery.getInstance().setComponent(this);
		
		// setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH);

		RefineryUtilities.centerFrameOnScreen(this);
        setShowEdgeWeight();
        setVisible(true);
	}

	private void createMenus() {
		menuBar = new JMenuBar();

		/***************** FILE MENU *****************/
		JMenu fileMenu = new JMenu(GuiStrings.FILE_MENU);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem openGraphItem = new JMenuItem(GuiStrings.OPEN_GRAPH, new ImageIcon(getClass()
				.getResource("/images/open.png")));
		openGraphItem.setActionCommand(ActionCommands.OPEN_GRAPH_ACTION);
		openGraphItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		openGraphItem.addActionListener(this);

		JMenuItem createGraphItem = new JMenuItem(GuiStrings.CREATE_GRAPH, new ImageIcon(
				getClass().getResource("/images/newfile.png")));
		createGraphItem.setActionCommand(ActionCommands.CREATE_GRAPH_ACTION);
		createGraphItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		createGraphItem.addActionListener(this);

		saveGraphItem = new JMenuItem(GuiStrings.SAVE_GRAPH, new ImageIcon(getClass()
				.getResource("/images/save.png")));
		saveGraphItem.setActionCommand(ActionCommands.SAVE_GRAPH_ACTION);
		saveGraphItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		saveGraphItem.setEnabled(false);
		saveGraphItem.addActionListener(this);

		JMenuItem exitMenuItem = new JMenuItem(GuiStrings.EXIT_MENU);
		exitMenuItem.setActionCommand(ActionCommands.EXIT_APP_ACTION);
		exitMenuItem.addActionListener(this);

		fileMenu.add(createGraphItem);
		fileMenu.add(openGraphItem);
		fileMenu.addSeparator();
		fileMenu.add(saveGraphItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		// END FILE MENU

		/***************** SETTINGS MENU *****************/
		settingsMenu = new JMenu(GuiStrings.SETTINGS_MENU);
		menuBar.add(settingsMenu);

		// EDGE WEIGHT SETTINGS
		JMenu weightMenu = new JMenu(GuiStrings.EDGE_WEIGHT);
		settingsMenu.add(weightMenu);

		ButtonGroup weightGroup = new ButtonGroup();
		JRadioButtonMenuItem weightOneMenuItem = new JRadioButtonMenuItem(GuiStrings.EDGE_ONE);
		weightOneMenuItem.setSelected(false);
		weightOneMenuItem.addActionListener(this);
		weightOneMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		weightOneMenuItem.setActionCommand(ActionCommands.SET_WEIGHT_TO_ONE_ACTION);
		weightGroup.add(weightOneMenuItem);
		weightMenu.add(weightOneMenuItem);

		JRadioButtonMenuItem weightRandomMenuItem = new JRadioButtonMenuItem(GuiStrings.EDGE_RANDOM);
		weightRandomMenuItem.addActionListener(this);
		weightRandomMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
		weightRandomMenuItem.setActionCommand(ActionCommands.SET_WEIGHT_RANDOM_ACTION);
		weightGroup.add(weightRandomMenuItem);
		weightMenu.add(weightRandomMenuItem);
		settingsMenu.addSeparator();
		
		// EDGE LABEL SETTINGS
		JMenu edgeLabelMenu = new JMenu(GuiStrings.EDGE_LABEL);
		settingsMenu.add(edgeLabelMenu);

		ButtonGroup edgeLabelGroup = new ButtonGroup();
		showEdgeProbItem = new JRadioButtonMenuItem(GuiStrings.EDGE_SHOW_PROB);
		showEdgeProbItem.setSelected(false);
		showEdgeProbItem.addActionListener(this);
		showEdgeProbItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.ALT_MASK));
		showEdgeProbItem.setActionCommand(ActionCommands.SHOW_PROB_ACTION);
		edgeLabelGroup.add(showEdgeProbItem);
		edgeLabelMenu.add(showEdgeProbItem);

		showEdgeWeightItem = new JRadioButtonMenuItem(GuiStrings.EDGE_SHOW_WEIGHT);
        showEdgeWeightItem.setSelected(true);
		showEdgeWeightItem.addActionListener(this);
		edgeLabelGroup.add(showEdgeWeightItem);
		edgeLabelMenu.add(showEdgeWeightItem);
		showEdgeWeightItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.ALT_MASK));
		showEdgeWeightItem
				.setActionCommand(ActionCommands.SHOW_WEIGHT_ACTION);

		// NODE LABEL SETTINGS
		JMenu nodeLabelMenu = new JMenu(GuiStrings.NODE_LABEL);
		settingsMenu.add(nodeLabelMenu);

		ButtonGroup nodeLabelGroup = new ButtonGroup();
		showNodeIdItem = new JRadioButtonMenuItem(GuiStrings.NODE_SHOW_ID);
		showNodeIdItem.setSelected(true);
		showNodeIdItem.addActionListener(this);
		showNodeIdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.ALT_MASK));
		showNodeIdItem.setActionCommand(ActionCommands.SHOW_NODE_ID);
		nodeLabelGroup.add(showNodeIdItem);
		nodeLabelMenu.add(showNodeIdItem);

		showObjectIdItem = new JRadioButtonMenuItem(GuiStrings.NODE_SHOW_OBJECT);
		showObjectIdItem.addActionListener(this);
		showObjectIdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.ALT_MASK));
		showObjectIdItem.setActionCommand(ActionCommands.SHOW_OBJECT_ID);
		nodeLabelGroup.add(showObjectIdItem);
		nodeLabelMenu.add(showObjectIdItem);

		settingsMenu.addSeparator();
		// EDGE EDITABLE SETTINGS
		JMenu editModeMenu = new JMenu(GuiStrings.SET_EDIT_MODE);
		settingsMenu.add(editModeMenu);

		ButtonGroup edgeEditGroup = new ButtonGroup();
		noEditItem = new JRadioButtonMenuItem(GuiStrings.OFF);
		noEditItem.setSelected(true);
		noEditItem.addActionListener(this);
		noEditItem.setActionCommand(ActionCommands.EDIT_MODE_OFF);
		edgeEditGroup.add(noEditItem);
		editModeMenu.add(noEditItem);

		editItem = new JRadioButtonMenuItem(GuiStrings.ON);
		editItem.setSelected(true);
		editItem.addActionListener(this);
		editItem.setActionCommand(ActionCommands.EDIT_MODE_ON);
		edgeEditGroup.add(editItem);
		editModeMenu.add(editItem);
		// END SETTINGS

		
		
		/***************** HYPOTHESIS PARAMETER MENU *****************/
		hypoMenu = new JMenu(GuiStrings.HYPO_MENU);
		hypoMenu.setToolTipText(GuiStrings.HYPOTHESIS_TOOLTIP);
		menuBar.add(hypoMenu);

		JMenuItem confidenceMenu = new JMenuItem(GuiStrings.SET_CONFIDENCE_MENU);
		confidenceMenu.setToolTipText(GuiStrings.CONFIDENCE_TOOLTIP);
		confidenceMenu.setActionCommand(ActionCommands.CONFIDENCE_ACTION);
		confidenceMenu.addActionListener(this);
		hypoMenu.add(confidenceMenu);

		JMenuItem significanceMenu = new JMenuItem(GuiStrings.SET_SIGNIFICANCE_MENU);
		significanceMenu.setToolTipText(GuiStrings.SIGNIFICANCE_TOOLTIP);
		significanceMenu.setActionCommand(ActionCommands.SIG_LEVEL_ACTION);
		significanceMenu.addActionListener(this);
		hypoMenu.add(significanceMenu);

		hypoMenu.addSeparator();

		ButtonGroup relativeGroup = new ButtonGroup();
		useRelative = new JRadioButtonMenuItem(GuiStrings.RELATIVE);
		useRelative.setSelected(true);
		useAbsolut = new JRadioButtonMenuItem(GuiStrings.ABSOLUTE);
		relativeGroup.add(useRelative);
		relativeGroup.add(useAbsolut);
		hypoMenu.add(useRelative);
		hypoMenu.add(useAbsolut);
		// END HYPOTHESIS PARAMETER SETTINGS

		/***************** INFO MENU *****************/
		infoMenu = new JMenu(GuiStrings.INFO_MENU);
		menuBar.add(infoMenu);

		JMenuItem graphInfoItem = new JMenuItem(GuiStrings.GRAPH_INFO, new ImageIcon(getClass()
				.getResource("/images/info.png")));
		graphInfoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent
				.getKeyText(KeyEvent.VK_F1)));
		graphInfoItem.addActionListener(this);
		graphInfoItem.setActionCommand(ActionCommands.GRAPH_INFO_ACTION);
		infoMenu.add(graphInfoItem);
		// END INFO

		// INITIAL CONFIGURATION
		settingsMenu.setEnabled(false);
		hypoMenu.setEnabled(false);
		infoMenu.setEnabled(false);

		setJMenuBar(menuBar);

		// create file chooser for opening & saving files
		fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".xml");
			}

			@Override
			public String getDescription() {
				return "XMLs";
			}
		});
	}

	private void createToolbar() {
		JToolBar toolbar = new JToolBar();

		JButton create = new JButton("", new ImageIcon(getClass().getResource(
				"/images/newfile.png")));
		create.setActionCommand(ActionCommands.CREATE_GRAPH_ACTION);
		create.setToolTipText(GuiStrings.CREATE_GRAPH_TOOLTIP);
		create.addActionListener(this);
		toolbar.add(create);

		JButton open = new JButton("", new ImageIcon(getClass().getResource(
				"/images/open.png")));
		open.setActionCommand(ActionCommands.OPEN_GRAPH_ACTION);
		open.setToolTipText("Open a graph from a file");
		open.addActionListener(this);
		toolbar.add(open);

		saveGraphButton = new JButton("", new ImageIcon(getClass().getResource(
				"/images/save.png")));
		saveGraphButton.setActionCommand(ActionCommands.SAVE_GRAPH_ACTION);
		saveGraphButton.setToolTipText("Save graph");
		saveGraphButton.addActionListener(this);
		saveGraphButton.setEnabled(false);
		toolbar.add(saveGraphButton);
		toolbar.addSeparator();
		
		zoomInButton = new JButton("", new ImageIcon(getClass().getResource(
				"/images/Zoom In.png")));
		zoomInButton.addActionListener(this);
		zoomInButton.setActionCommand(ActionCommands.ZOOM_IN_ACTION);
		zoomInButton.setToolTipText("Zoom in");
		zoomInButton.setEnabled(false);
		toolbar.add(zoomInButton);
		
		zoomOutButton = new JButton("", new ImageIcon(getClass().getResource(
				"/images/Zoom Out.png")));
		zoomOutButton.addActionListener(this);
		zoomOutButton.setActionCommand(ActionCommands.ZOOM_OUT_ACTION);
		zoomOutButton.setToolTipText("Zoom out");
		zoomOutButton.setEnabled(false);
		toolbar.add(zoomOutButton);
		toolbar.addSeparator();

		editModeToggle = new JToggleButton("", new ImageIcon(getClass()
				.getResource("/images/pencil.png")));
		editModeToggle.setActionCommand(ActionCommands.EDIT_MODE_TOGGLE);
		editModeToggle.setToolTipText(GuiStrings.EDIT_MODE_TOOLTIP);
		editModeToggle.addActionListener(this);
		editModeToggle.setEnabled(false);
		toolbar.add(editModeToggle);

		Font cursiv = new Font("sansserif", Font.ITALIC, 12);
		showEdgeWeightButton = new JToggleButton(GuiStrings.EDGE_WEIGHT_BUTTON);
		showEdgeWeightButton.setFont(cursiv);
		showEdgeWeightButton.setToolTipText(GuiStrings.EDGE_WEIGHT_TOOLTIP);
		showEdgeWeightButton.addActionListener(this);
		showEdgeWeightButton.setEnabled(true);
        showEdgeWeightButton.setSelected(true);
		showEdgeWeightButton
				.setActionCommand(ActionCommands.SHOW_WEIGHT_ACTION);
		toolbar.add(showEdgeWeightButton);

		showEdgeProbButton = new JToggleButton(GuiStrings.EDGE_PROB_BUTTON);
		showEdgeProbButton.setFont(cursiv);
		showEdgeProbButton.setToolTipText(GuiStrings.EDGE_PROB_TOOLTIP);
		showEdgeProbButton.addActionListener(this);
		showEdgeProbButton.setEnabled(false);
		showEdgeProbButton.setSelected(false);
		showEdgeProbButton.setActionCommand(ActionCommands.SHOW_PROB_ACTION);
		toolbar.add(showEdgeProbButton);

		showNodeIdButton = new JToggleButton(GuiStrings.NODE_SHOW_ID_BUTTON);
		showNodeIdButton.setFont(cursiv);
		showNodeIdButton.setActionCommand(ActionCommands.SHOW_NODE_ID);
		showNodeIdButton.addActionListener(this);
		showNodeIdButton.setEnabled(false);
		showNodeIdButton.setToolTipText(GuiStrings.NODE_ID_BUTTON_TOOLTIP);
		toolbar.add(showNodeIdButton);

		showObjectIdButton = new JToggleButton(GuiStrings.NODE_SHOW_OBJECT_BUTTON);
		showObjectIdButton.setFont(cursiv);
		showObjectIdButton.setActionCommand(ActionCommands.SHOW_OBJECT_ID);
		showObjectIdButton.addActionListener(this);
		showObjectIdButton.setEnabled(false);
		showObjectIdButton.setToolTipText(GuiStrings.NODE_SHOW_OBJECT_BUTTON_TOOLTIP);
		toolbar.add(showObjectIdButton);

		toolbar.addSeparator();
		hypoButton = new JButton("", new ImageIcon(getClass().getResource(
				"/images/chart_curve.png")));
		hypoButton.setActionCommand(ActionCommands.HYPO_SET_ACTION);
		hypoButton.addActionListener(this);
		hypoButton.setToolTipText(GuiStrings.HYPO_BUTTON_TOOLTIP);
		hypoButton.setEnabled(false);
		toolbar.add(hypoButton);

		toolbar.setRollover(true);
		toolbar.setFloatable(false);
		getContentPane().add(toolbar, BorderLayout.NORTH);
	}

	private void createContents() {
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		sampleGraphButton = new JButton(GuiStrings.SAMPLE_GRAPH_BUTTON, new ImageIcon(
				getClass().getResource("/images/chemical.png")));
		sampleGraphButton.setEnabled(false);
		sampleGraphButton
				.setToolTipText(GuiStrings.SAMPLE_GRAPH_BUTTON_TOOLTIP);
		sampleGraphButton
				.setActionCommand(ActionCommands.SAMPLE_GRAPH_ACTION);
		sampleGraphButton.addActionListener(this);

		graphLabel = new JLabel("");
		graphLabel.setHorizontalAlignment(JLabel.CENTER);
		JPanel north = new JPanel();
		north.add(sampleGraphButton);
		north.add(graphLabel);

		graphPanel = new GraphVisualizer(true);
		graphPanel.getNodeSelector().addObserver(this);

		statusBar = new JLabel();
		updateStatusBar("<font color=\"green\">" + GuiStrings.READY_STATUS + "</font>");

		outlookBar = new OutlookBar();
		fillOutlookBar();

		runButton = new JButton(
				"<html><font color=\"green\"><b>" + GuiStrings.RUN_BUTTON + "</b></font></html>",
				new ImageIcon(getClass().getResource("/images/run.png")));
		runButton.addActionListener(new RunAlgorithmListener());
		runButton.setToolTipText(GuiStrings.RUN_BUTTON_TOOLTIP);
		runButton.setEnabled(false);
		JPanel outlookPanel = new JPanel();
		outlookPanel.setLayout(new BorderLayout());
		outlookPanel.add(outlookBar, BorderLayout.CENTER);
		outlookPanel.add(runButton, BorderLayout.SOUTH);

		center.add(graphPanel, BorderLayout.CENTER);
		center.add(north, BorderLayout.NORTH);

		JPanel combine = new JPanel();
		combine.setLayout(new BorderLayout());
		combine.add(center, BorderLayout.CENTER);
		combine.add(outlookPanel, BorderLayout.EAST);
		// center.add(outlookPanel, BorderLayout.EAST);

		getContentPane().add(combine, BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
	}
	
	private void fillOutlookBar() {
		outlookBar.addBar(AlgorithmStrings.CONNECT_COMP,
				          GuiUtil.createInfoPanel(AlgorithmStrings.CONNECTED_COMP_INFO),
				          0, true);
		
		outlookBar.addBar(AlgorithmStrings.LARGEST_CONNECT_COMP, 
				          GuiUtil.createInfoPanel(AlgorithmStrings.LARGEST_CONNECTED_COMP_INFO),
				          0, true);
		
		outlookBar.addBar(AlgorithmStrings.DURCHMESSER,
						  GuiUtil.createInfoPanel(AlgorithmStrings.DURCHMESSER_INFO), 0,
						  true);
		
		outlookBar.addBar(AlgorithmStrings.SHORTEST_PATH,
						  GuiUtil.createInfoPanel(AlgorithmStrings.SHORTEST_PATH_INFO),
						  2, false);
		
		outlookBar.addBar(AlgorithmStrings.REACHABLE_NODES,
						  GuiUtil.createInfoPanel(AlgorithmStrings.REACHABLE_NODES_INFO),
						  1, false);
		
		outlookBar.addBar(AlgorithmStrings.KNN, createKnnPanel(), 1, false); 
		
		outlookBar.addBar(AlgorithmStrings.REVERSE_NN,
						  GuiUtil.createInfoPanel(AlgorithmStrings.REVERSE_NN_INFO), 1,
						  false);
        outlookBar.addBar(AlgorithmStrings.RKNN_NAIVE, createRkNNSimplePanel(), 1, false);
        outlookBar.addBar(AlgorithmStrings.RKNN_EAGER, createRkNNEagerPanel(), 1, false);
        outlookBar.addBar(AlgorithmStrings.RKNN_EMBEDDED, createRkNNEmbeddedPanel(), 1, false);
		
		// "DUMMY" is an empty panel, to fill up the space
		outlookBar.addBar("DUMMY", new JPanel(), 0, false);
		outlookBar.addBar("DUMMY", new JPanel(), 0, false);
		outlookBar.addBar("DUMMY", new JPanel(), 0, false);
		outlookBar.addBar("DUMMY", new JPanel(), 0, false);
		outlookBar.setAllDisabled(); // at the beginning: set all disabled
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Object source = e.getSource();
		String action = e.getActionCommand();

		if (action == ActionCommands.EXIT_APP_ACTION) {
			processEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		} 
		else if (action == ActionCommands.CREATE_GRAPH_ACTION) {
			showCreateGraphDialog();
		} 
		else if (action == ActionCommands.OPEN_GRAPH_ACTION) {
			showOpenGraphDialog();
		} 
		else if (action == ActionCommands.SAVE_GRAPH_ACTION) {
			showSaveGraphDialog();
		} 
		else if (action == ActionCommands.GRAPH_INFO_ACTION) {
			new GraphInfoWindow(ProbabilisticGraph.getInstance());
		} 
		else if (action == ActionCommands.SAMPLE_GRAPH_ACTION) {
			new SampleGraphWindow();
		} 
		else if (action == ActionCommands.SET_WEIGHT_TO_ONE_ACTION) {
			ProbabilisticGraph.getInstance().setAllWeight(
					GuiConstants.DEFAULT_NODE_WEIGHT);
			GraphGenerator.getInstance().setWeightOne(true);
			if (showEdgeWeightItem.isSelected()) {
				graphPanel.setShowingEdgeWeight(true);
			}
		} 
		else if (action == ActionCommands.SET_WEIGHT_RANDOM_ACTION) {
			ProbabilisticGraph.getInstance().setAllWeightWithLimit(
					GuiConstants.MAX_EDGE_WEIGHT);
			GraphGenerator.getInstance().setWeightOne(false);
			if (showEdgeWeightItem.isSelected()) {
				graphPanel.setShowingEdgeWeight(true);
			}
		} 
		else if (action == ActionCommands.SHOW_PROB_ACTION) {
			setShowEdgeProbability();
		} 
		else if (action == ActionCommands.SHOW_WEIGHT_ACTION) {
			setShowEdgeWeight();
		} 
		else if (action == ActionCommands.SHOW_NODE_ID) {
			setShowNodeId();
		} 
		else if (action == ActionCommands.SHOW_OBJECT_ID) {
			setShowObjectId();
		} 
		else if (action == ActionCommands.EDIT_MODE_ON) {
			setEditMode(true);
		} 
		else if (action == ActionCommands.EDIT_MODE_OFF) {
			setEditMode(false);
		} 
		else if (action == ActionCommands.EDIT_MODE_TOGGLE) {
			if (editModeToggle.isSelected()) {
				editItem.setSelected(true);
				setEditMode(true);
			} else {
				noEditItem.setSelected(true);
				setEditMode(false);
			}
		} 
		else if (action == ActionCommands.CONFIDENCE_ACTION) {
			showSetConfidenceDialog();
		} 
		else if (action == ActionCommands.SIG_LEVEL_ACTION) {
			showSetSignificanceLevelDialog();
		} 
		else if (action == ActionCommands.HYPO_SET_ACTION) {
			showSetHypoParameterDialog();
		}
		else if(action == ActionCommands.ZOOM_IN_ACTION) {
			graphPanel.zoomInGraph();
		}
		else if(action == ActionCommands.ZOOM_OUT_ACTION) {
			graphPanel.zoomOutGraph();
		}
	}

	private void setEditMode(boolean editMode) {
		if (editMode) {
			graphPanel.setEditMode(true);
			runButton.setEnabled(false);
			editModeToggle.setSelected(true);
			updateStatusBar("<font color=\"red\">EDIT MODE</font>");
		} else {
			graphPanel.setEditMode(false);
			runButton.setEnabled(true);
			editModeToggle.setSelected(false);
			Vector<Integer> nodes = graphPanel.getNodeSelector().getSelection();
			outlookBar.updateBar(nodes.size());
			updateStatusBar(graphPanel.getSelectedNodesMessage());
			updateGraphInfoLabel();
		}
	}

	private void setShowNodeId() {
		showNodeIdItem.setSelected(true);
		showNodeIdButton.setSelected(true);
		showObjectIdButton.setSelected(false);
		graphPanel.setShowingObjectId(false);
	}

	private void setShowObjectId() {
		showObjectIdItem.setSelected(true);
		showObjectIdButton.setSelected(true);
		showNodeIdButton.setSelected(false);
		graphPanel.setShowingObjectId(true);
	}

	private void setShowEdgeProbability() {
        showEdgeWeightButton.setSelected(false);
        showEdgeWeightItem.setSelected(false);
		showEdgeProbItem.setSelected(true);
		showEdgeProbButton.setSelected(true);
		graphPanel.setShowingEdgeWeight(false);
	}

	private void setShowEdgeWeight() {
        showEdgeProbButton.setSelected(false);
        showEdgeProbItem.setSelected(false);
		showEdgeWeightItem.setSelected(true);
		showEdgeWeightButton.setSelected(true);
		graphPanel.setShowingEdgeWeight(true);
	}

	private void showOpenGraphDialog() {
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final String file = fc.getSelectedFile().getAbsolutePath();
			setBusyCursor();
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					XmlUtil.importGraphFromXml(file);
					newGraphCreated();
					updateStatusBar("Graph loaded from XML (" + file + ")");
					return null;
				}

				@Override
				protected void done() {
					setNormalCursor();
				}

			}.execute();
		}
	}

	private void showSaveGraphDialog() {
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String file = fc.getSelectedFile().getAbsolutePath();
			graphPanel.updateNodeModelPositions();
			XmlUtil.saveGraphToXml(ProbabilisticGraph.getInstance(), file);
			updateStatusBar("Graph saved to " + file);
		}
	}

	private void showSetHypoParameterDialog() { 
		JTextField confi = new JTextField("" + ChartConstants.CONFIDENCE_STD_VAL);
		confi.setToolTipText(GuiStrings.CONFIDENCE_TOOLTIP);
		
		JTextField levSig = new JTextField("" + ChartConstants.SIGNIFICANCE_STD_VAL);
		levSig.setToolTipText(GuiStrings.SIGNIFICANCE_TOOLTIP);
		
		ButtonGroup relativeGroup = new ButtonGroup();
		JRadioButtonMenuItem useRelativeTmp = new JRadioButtonMenuItem(GuiStrings.RELATIVE);
		JRadioButtonMenuItem useAbsolutTmp = new JRadioButtonMenuItem(GuiStrings.ABSOLUTE);
		relativeGroup.add(useRelativeTmp);
		relativeGroup.add(useAbsolutTmp);
		if(this.useRelative.isSelected()) {
			useRelativeTmp.setSelected(true);
		} else {
			useAbsolutTmp.setSelected(true);
		}
		
		Object[] message = { "Confidence:", confi,
				             "Level of Significance:", levSig, 
				             useRelativeTmp, useAbsolutTmp};
		Object[] options = { "OK", "Cancel" };
		int result = JOptionPane.showOptionDialog(this, message,
				"Set Hypothesis Parameters", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, "");
		if (result == JOptionPane.OK_OPTION) {
			try {
				double mDev = Double.parseDouble(confi.getText().trim());
				ChartConstants.CONFIDENCE_STD_VAL = mDev;
				
				double lSig = Double.parseDouble(levSig.getText().trim());
				ChartConstants.SIGNIFICANCE_STD_VAL = lSig;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						"Error - Hypothesis Parameter", JOptionPane.ERROR_MESSAGE);
			}
			if(useRelativeTmp.isSelected()) {
				this.useRelative.setSelected(true);
			} else {
				this.useAbsolut.setSelected(true);
			}
		}
	}

	private void showSetConfidenceDialog() {
		JTextField confi = new JTextField("" + ChartConstants.CONFIDENCE_STD_VAL);
		confi.setToolTipText(GuiStrings.CONFIDENCE_TOOLTIP);
		Object[] message = { "Confidence:", confi };
		Object[] options = { "OK", "Cancel" };
		int result = JOptionPane.showOptionDialog(this, message,
				GuiStrings.SET_CONFIDENCE_MENU, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, "");
		if (result == JOptionPane.OK_OPTION) {
			try {
				double mDev = Double.parseDouble(confi.getText().trim());
				ChartConstants.CONFIDENCE_STD_VAL = mDev;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						"Error - Confidence", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void showSetSignificanceLevelDialog() {
		JTextField levSig = new JTextField("" + ChartConstants.SIGNIFICANCE_STD_VAL);
		levSig.setToolTipText(GuiStrings.SIGNIFICANCE_TOOLTIP);
		Object[] message = { "Level of Significance:", levSig };
		Object[] options = { "OK", "Cancel" };
		int result = JOptionPane.showOptionDialog(this, message,
				GuiStrings.SET_SIGNIFICANCE_MENU, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, "");
		if (result == JOptionPane.OK_OPTION) {
			try {
				double lSig = Double.parseDouble(levSig.getText().trim());
				ChartConstants.SIGNIFICANCE_STD_VAL = lSig;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						"Error - Level of Significance",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void showCreateGraphDialog() {
		JTextField numberOfVertex = new JTextField();
		JTextField numberOfObjects = new JTextField();
		JTextField numberOfEdges = new JTextField();
		JTextField numberOfProbEdges = new JTextField();
		Object[] message = { "Number of Vertices:", numberOfVertex,
				"Number of Objects:", numberOfObjects, "Number of Edges:",
				numberOfEdges, "Number of Uncertain Edges:", numberOfProbEdges };
		Object[] options = { "OK", "Cancel" };
		int result = JOptionPane.showOptionDialog(this, message,
				"Create a Probabilistic Graph", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, "");
		if (result == JOptionPane.OK_OPTION) {
			try {
				final int vertex = Integer.parseInt(numberOfVertex.getText());
				final int edges;
				if (!numberOfEdges.getText().isEmpty()) {
					edges = Integer.parseInt(numberOfEdges.getText());
				} else {
					edges = 0;
				}
				final int edgesP;
				if (!numberOfProbEdges.getText().isEmpty()) {
					edgesP = Integer.parseInt(numberOfProbEdges.getText());
				} else {
					edgesP = 0;
				}
				final int objects;
				if (!numberOfObjects.getText().isEmpty()) {
					objects = Integer.parseInt(numberOfObjects.getText());
				} else {
					objects = 0;
				}

				setBusyCursor();
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						try {
							GraphGenerator.getInstance()
									.generateProbabilisticGraph(vertex, edges,
											edgesP, objects);
							newGraphCreated();
							updateStatusBar("Graph created! (" + vertex
									+ " Nodes, " + objects + " Objects, "
									+ edges + " Edges, " + edgesP
									+ " Probabilities)");
						} catch (RuntimeException e) {
							JOptionPane.showMessageDialog(null,
									e.getLocalizedMessage(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}

						return null;
					}

					@Override
					protected void done() {
						setNormalCursor();
					}

				}.execute();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (RuntimeException e) {
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * listen to node selection
	 * 
	 * @param arg0
	 * @param arg1
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		Vector<Integer> nodes = graphPanel.getNodeSelector().getSelection();
		outlookBar.updateBar(nodes.size());

		updateStatusBar(graphPanel.getSelectedNodesMessage());

		if (nodes.size() != previousSelectedBar) {
			outlookBar.setNonSelection();
		}
		previousSelectedBar = nodes.size();
	}

	private void setBusyCursor() {
		final int cursorType = Cursor.WAIT_CURSOR;
		final Component glassPane = ((RootPaneContainer) menuBar
				.getTopLevelAncestor()).getGlassPane();
		glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
		glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
	}

	private void setNormalCursor() {
		final Component glassPane = ((RootPaneContainer) menuBar
				.getTopLevelAncestor()).getGlassPane();
		glassPane.setCursor(Cursor.getDefaultCursor());
	}

	private void updateStatusBar(String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html> ");
		sb.append(msg);
		sb.append("</html>");
		statusBar.setText(sb.toString());
	}

	private JPanel createKnnPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel tmp = new JPanel();
		JLabel l = new JLabel("<html><i>k :</i></html>");
		kNNField = new JTextField(10);
		kNNField.setHorizontalAlignment(JTextField.CENTER);

		l.setLabelFor(kNNField);
		tmp.add(l);
		tmp.add(kNNField);

		panel.add(tmp, BorderLayout.SOUTH);
		panel.add(GuiUtil.createInfoPanel(AlgorithmStrings.KNN_INFO),
				BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return panel;
	}

    private JPanel createRkNNSimplePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel tmp = new JPanel();
        JLabel l = new JLabel("<html><i>k :</i></html>");
        rknnSimpleField = new JTextField(10);
        rknnSimpleField.setHorizontalAlignment(JTextField.CENTER);

        l.setLabelFor(rknnSimpleField);
        tmp.add(l);
        tmp.add(rknnSimpleField);

        panel.add(tmp, BorderLayout.SOUTH);
        panel.add(GuiUtil.createInfoPanel(AlgorithmStrings.RKNN_INFO),
                BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return panel;
    }

    private JPanel createRkNNEagerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel tmp = new JPanel();
        JLabel l = new JLabel("<html><i>k :</i></html>");
        rknnEagerField = new JTextField(10);
        rknnEagerField.setHorizontalAlignment(JTextField.CENTER);

        l.setLabelFor(rknnEagerField);
        tmp.add(l);
        tmp.add(rknnEagerField);

        panel.add(tmp, BorderLayout.SOUTH);
        panel.add(GuiUtil.createInfoPanel(AlgorithmStrings.RKNN_INFO),
                BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return panel;
    }

    private JPanel createRkNNEmbeddedPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel tmp = new JPanel();
        JLabel l = new JLabel("<html><i>k :</i></html>");
        rknnEmbeddedField = new JTextField(2);
        rknnEmbeddedField.setHorizontalAlignment(JTextField.CENTER);
        l.setLabelFor(rknnEmbeddedField);


        JLabel l2 = new JLabel("<html><i>Refspoints :</i></html>");
        rknnEmbeddedFieldNumRefPoints = new JTextField(2);
        rknnEmbeddedFieldNumRefPoints.setHorizontalAlignment(JTextField.CENTER);
        l.setLabelFor(rknnEmbeddedFieldNumRefPoints);

        tmp.add(l);
        tmp.add(rknnEmbeddedField);
        tmp.add(l2);
        tmp.add(rknnEmbeddedFieldNumRefPoints);

        panel.add(tmp, BorderLayout.SOUTH);
        panel.add(GuiUtil.createInfoPanel(AlgorithmStrings.RKNN_INFO),
                BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return panel;
    }


	private void resetOutlookBar() {
		graphPanel.getSelectedNodes().clear();
		outlookBar.updateBar(graphPanel.getSelectedNodes().size());
	}

	private void newGraphCreated() {
		graphPanel.setGraph(ProbabilisticGraph.getInstance());

		if (justStarted) {
			// DEFAULT: reset to show probability
			showEdgeProbItem.setSelected(false);
			showEdgeProbButton.setSelected(false);
			showEdgeWeightButton.setSelected(true);
			showNodeIdButton.setEnabled(true);
			showNodeIdButton.setSelected(true);
			showObjectIdButton.setEnabled(true);
			settingsMenu.setEnabled(true);
			hypoMenu.setEnabled(true);
			infoMenu.setEnabled(true);
			editModeToggle.setEnabled(true);
			showEdgeWeightButton.setEnabled(true);
			showEdgeProbButton.setEnabled(false);
			sampleGraphButton.setEnabled(true);
			saveGraphItem.setEnabled(true);
			saveGraphButton.setEnabled(true);
			hypoButton.setEnabled(true);
			zoomInButton.setEnabled(true);
			zoomOutButton.setEnabled(true);
			runButton.setEnabled(true);

			justStarted = false;
		}

		resetOutlookBar();
		updateGraphInfoLabel();
	}
	
	private void updateGraphInfoLabel() {
		graphLabel.setText("<html>(<font color=\"green\">"
				+ ProbabilisticGraph.getInstance().getNumberOfVertices()
				+ "</font> Nodes, <font color=\"green\">"
				+ ProbabilisticGraph.getInstance().getNumberOfObjectNodes()
				+ "</font> Objects, <font color=\"green\">"
				+ ProbabilisticGraph.getInstance().getNumberOfEdges()
				+ "</font> Edges, <font color=\"green\">"
				+ ProbabilisticGraph.getInstance().getNumberOfUncertainEdges()
				+ "</font> Probabilities" + ")</html>");
	}

	private class RunAlgorithmListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String algo = outlookBar.getSelectedAlgorithm();
			
			if(!algo.equals("")) {
				// run
                if(algo.equals(AlgorithmStrings.RKNN_NAIVE)){
                    Graph jGraph = ProbabilisticGraph.getInstance();
                    int qID = graphPanel.getSelectedNodes().get(0);
                    int k = Integer.parseInt(rknnSimpleField.getText().trim());
                    RkNNComparator.naiveRkNN(jGraph, qID, k);
                }
                else if(algo.equals(AlgorithmStrings.RKNN_EAGER)){
                    Graph jGraph = ProbabilisticGraph.getInstance();
                    int qID = graphPanel.getSelectedNodes().get(0);
                    int k = Integer.parseInt(rknnEagerField.getText().trim());
                    RkNNComparator.eagerRkNN(jGraph, qID, k);
                }
                else if(algo.equals(AlgorithmStrings.RKNN_EMBEDDED)){
                    Graph jGraph = ProbabilisticGraph.getInstance();
                    int qID = graphPanel.getSelectedNodes().get(0);
                    int k = Integer.parseInt(rknnEmbeddedField.getText().trim());
                    int numRefPoints = Integer.parseInt(rknnEmbeddedFieldNumRefPoints.getText().trim());
                    RkNNComparator.embeddedRkNN(jGraph, qID, k, numRefPoints);
                }
                else{
                    Vector<Integer> nodes = graphPanel.getSelectedNodes();
                    AlgorithmQuery.getInstance().setAlgorithm(algo);
                    AlgorithmQuery.getInstance().setUseRelative(useRelative.isSelected());
                    AlgorithmQuery.getInstance().setKNumber(kNNField.getText().trim());
                    AlgorithmQuery.getInstance().startSampling(nodes);
                }
            } else {
             // no action selected
            updateStatusBar("<font color=\"red\">" + GuiStrings.NO_ACTION_MSG + "!</font>");
			}
		}
	}
}