package view;

import graph.ProbabilisticGraph;
import graph.core.Edge;
import graph.core.Graph;
import graph.core.Vertex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import util.GuiConstants;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class GraphVisualizer extends JPanel implements ActionListener { 
	private static final long serialVersionUID = 1L;
    
	private static final String NORMAL_EDGE_STYLE = "startArrow=none;endArrow=none;strokeWidth=0.1;strokeColor=#000000";
	private static final String PROB_EDGE_STYLE   = "startArrow=none;endArrow=none;strokeWidth=2.0;strokeColor=#FF7F00";
	private static final String EMPTY_NODE_STYLE  = "emptyNodeStyle";
	private static final String FILLED_NODE_STYLE = "filledNodeStyle";
	
	private Graph graph;
	private mxGraph graphView;
	private Object parent;
	private mxGraphComponent graphComponent;
	
	private HashMap<Integer, Object> nodesView;        // map from model -> view (for drawing edges)
	private HashMap<mxCell, Vertex> nodesViewReverse;  // map from view -> model (catching events)
	private HashMap<mxCell, Edge> edgesView;           // map from view -> model (catching events)
	
	private NodeSelector nodeSelector;
	private NodeSelector editModeNodeSelector;
	private Object previousSelected;
	private Object editModePreviousSelected;
	
	private JPopupMenu popUp;
	private JMenuItem editNodeMenu, editEdgeMenu, removeEdgeMenu, removeNodeMenu, addNodeMenu, createEdgeMenu;
	private mxCell editSelected;
	private MouseEvent editEvent;
	
	private boolean isShowingWeight   = false;
	private boolean isShowingObjectId = false;
	private boolean editMode          = false;
	
	public GraphVisualizer(boolean isEditable) {
		super();
		graphView = new mxGraph() {
			public boolean isCellMovable(Object cell) {
				return !getModel().isEdge(cell); // edges not movable
			}

			public boolean isCellResizable(Object cell) {
			     return !getModel().isVertex(cell); // nodes not resizable
			}
		};
		graphView.setEdgeLabelsMovable(false);
		graphView.setCellsEditable(false);
		graphView.setConnectableEdges(false);
		graphView.setAllowDanglingEdges(false);  // i don't know what this is..
		graphView.setDropEnabled(false);         // allow to "swallow" an edge, if a node is dropped above it
		graphView.setCellsDisconnectable(false); // allow to move edge's end
		
		prepareStyles();
		
		nodeSelector = new NodeSelector();
		editModeNodeSelector = new NodeSelector();
		
		graphComponent = new mxGraphComponent(graphView);
		graphComponent.getViewport().setOpaque(false);
		graphComponent.setBackground(Color.WHITE);
		graphComponent.setConnectable(false);
		graphComponent.setPanning(true);
		
		addZoomFunction();
		if(isEditable) {
			addSelectionAndEditFunction();
		}
		           
		mxCellMarker highlighter = new mxCellMarker(graphComponent, Color.ORANGE);
		graphComponent.getConnectionHandler().setMarker(highlighter);
		
		// create pop up menu and prepare menus
		popUp = new JPopupMenu();
		editNodeMenu	= new JMenuItem("Edit Node", new ImageIcon(getClass().getResource("/images/pencil.png")));
		editEdgeMenu	= new JMenuItem("Edit Edge", new ImageIcon(getClass().getResource("/images/pencil.png")));
		removeEdgeMenu  = new JMenuItem("Remove Edge", new ImageIcon(getClass().getResource("/images/close.png")));
		removeNodeMenu  = new JMenuItem("Remove Node", new ImageIcon(getClass().getResource("/images/close.png")));
		addNodeMenu 	= new JMenuItem("Add a node here", new ImageIcon(getClass().getResource("/images/plus.png")));
		createEdgeMenu  = new JMenuItem("Create Edge", new ImageIcon(getClass().getResource("/images/plus.png")));
		
		editNodeMenu.addActionListener(this);
		editEdgeMenu.addActionListener(this);
		removeEdgeMenu.addActionListener(this);
		removeNodeMenu.addActionListener(this);
		addNodeMenu.addActionListener(this);
		createEdgeMenu.addActionListener(this);
		
		popUp.add(editNodeMenu);
		
		MouseListener popupListener = new PopupListener();
		graphComponent.getGraphControl().addMouseListener(popupListener);
		
		this.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(graphComponent);
		pane.getVerticalScrollBar().setUnitIncrement(10);
		pane.getHorizontalScrollBar().setUnitIncrement(10);
		this.add(pane, BorderLayout.CENTER);

//		setBorder(BorderFactory.createLineBorder(Color.black));
	}
	

	public void setGraph(Graph g) {
		clearGraph();
		this.graph = g;
		drawGraph();
	}
	
	private void clearGraph() {
		if(graph != null) {
			graphView.removeCells(graphView.getChildVertices(graphView.getDefaultParent()));
			graphComponent.refresh();
		}
	}
	
	private void drawGraph() {		
		parent = graphView.getDefaultParent();
		
		graphView.getModel().beginUpdate();
		try {
			nodesView        = new HashMap<Integer, Object>();
			nodesViewReverse = new HashMap<mxCell, Vertex>();
			edgesView        = new HashMap<mxCell, Edge>();
			
			// draw the nodes first 
			Vector<Vertex> vertices = graph.getAllVertices();
			int drawed = 1;
			int size = vertices.size();
			for(Vertex v : graph.getAllVertices()) {
				Point location = v.getNodeLocation();
				String style;
				if(v.getObjectId() >= 0) {
					style = FILLED_NODE_STYLE;
				} else {
					style = EMPTY_NODE_STYLE;
				}
				mxCell cell = (mxCell) graphView.insertVertex(parent, null, null, location.x , location.y, 
						                          GuiConstants.NODE_WIDTH, GuiConstants.NODE_HEIGHT, style);
				if(isShowingObjectId) {
					int objectId = v.getObjectId();
					if(objectId == Vertex.NO_OBJECT) {
						cell.setValue(null);
					} else {
						cell.setValue(v.getObjectId());
					}
				} else {
					cell.setValue(v.getId());
				}
				nodesView.put(v.getId(), cell);
				nodesViewReverse.put(cell, v);
//				util.Log.appendln("Drawing Nodes...(" + drawed++ + " / " + size + ")");
			}
			
			// draw the edges
			Vector<Edge> edges = graph.getAllEdges();
			size = edges.size();
			drawed = 1;
			for(Edge e : edges) {
				Vertex start  = e.getSource();
				Vertex target = e.getTarget();

				mxCell cell = (mxCell) graphView.insertEdge(parent, null, null, nodesView.get(start.getId()), 
						                                    nodesView.get(target.getId()), null);
				if(graph instanceof ProbabilisticGraph) {
					double p = ((ProbabilisticGraph) graph).getProbability(e);
					if(p == 1.0) {	
						cell.setStyle(NORMAL_EDGE_STYLE);
					} else {
						cell.setStyle(PROB_EDGE_STYLE);
					}
					if(isShowingWeight) {
						cell.setValue(e.getWeight());
					} else {
						if(p != 1.0) {
							cell.setValue(p);
						}
					}
				} else {
					// graph is a sample graph
					cell.setStyle(NORMAL_EDGE_STYLE);
					if(isShowingWeight) {
						cell.setValue(e.getWeight());
					} 
				}
				
				edgesView.put(cell, e);
//				util.Log.appendln("Drawing Edges...(" + drawed++ + " / " + size + ")");
			}
		} finally {
			graphView.getModel().endUpdate();
		}
		this.repaint();
		
	}
	
	public void setShowingObjectId(boolean showObject) {
		if(showObject) {
			showAllObjectId();
			isShowingObjectId = true;
		} else {
			showAllNodeId();
			isShowingObjectId = false;
		}
	}
	
	private void showAllNodeId() {
		if(graph != null) {
			for(mxCell node : nodesViewReverse.keySet()) {
				node.setValue(nodesViewReverse.get(node).getId());
			}
		}
		refresh();
	}
	
	private void showAllObjectId() {
		if(graph != null) {
			for(mxCell node : nodesViewReverse.keySet()) {
				int val = nodesViewReverse.get(node).getObjectId();
				if(val == Vertex.NO_OBJECT) {
					// no object -> null
					node.setValue(null);
				} else {
					node.setValue(val);
				}
			}
		}
		refresh();
	}
	
	public void setShowingEdgeWeight(boolean showWeight) {
		if(showWeight) {
			showAllEdgeWeight();
			isShowingWeight = true;
		} else {
			showAllEdgeProbability();
			isShowingWeight = false;
		}
	}
	
	private void showAllEdgeWeight() {
		if(graph != null) {
			for(mxCell edge : edgesView.keySet()) {
				edge.setValue(edgesView.get(edge).getWeight());
			}
		}
		refresh();
	}
	
	private void showAllEdgeProbability() {
		if(graph != null && graph instanceof ProbabilisticGraph) {
			for(mxCell edge : edgesView.keySet()) {
				double p = ((ProbabilisticGraph) graph).getProbability(edgesView.get(edge));
				if(p == 1.0) {
					edge.setValue(null);	
					edge.setStyle(NORMAL_EDGE_STYLE);
				} else {
					edge.setValue(p);
					edge.setStyle(PROB_EDGE_STYLE);
				}
			}
		}
		refresh();
	}
	
	private void addZoomFunction() {
		graphComponent.addMouseWheelListener(new MouseWheelListener() { 
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) {
                    	zoomInGraph();
                    }
                    else {
                    	zoomOutGraph();
                    }
                }
            }
        });
		
		
		graphComponent.addKeyListener(new KeyAdapter() {
			 public void keyPressed(KeyEvent e) {
				 if(e.isControlDown()) {
					 if(e.getKeyCode() == KeyEvent.VK_PLUS) {
						 zoomInGraph();
					 } else if(e.getKeyCode() == KeyEvent.VK_MINUS) {
						 zoomOutGraph();
					 }
				 }
			 }
		});
	}
	
	public void zoomInGraph() {
		graphComponent.zoomIn();
	}
	
	public void zoomOutGraph() {
		graphComponent.zoomOut();
	}
	
	private void addSelectionAndEditFunction() {
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(graph != null) { 
					mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
					if(cell != null) {
						if(!editMode) {
							if(cell.isVertex()) {
								try {
									Vertex selected = nodesViewReverse.get(cell);
									int id = selected.getId();
									if(nodeSelector.alreadyExists(id)) {
										// set back to normal
										if(selected.getObjectId() >= 0) {
											graphView.setCellStyle(FILLED_NODE_STYLE, new Object[]{cell});
										} else {
											graphView.setCellStyle(EMPTY_NODE_STYLE, new Object[]{cell});
										}
									} else {
										if(nodeSelector.getSelection().size() == NodeSelector.MAX_SELECTION) { 
											// set previous selected node back to normal style
											Vertex prev = nodesViewReverse.get(previousSelected);
											if(prev.getObjectId() >= 0) {
												graphView.setCellStyle(FILLED_NODE_STYLE, new Object[]{previousSelected});
											} else {
												graphView.setCellStyle(EMPTY_NODE_STYLE, new Object[]{previousSelected});
											}
										}

										nodeSelector.addValue(id);
										previousSelected = cell;
										graphView.setCellStyles(mxConstants.STYLE_FILLCOLOR, "green", new Object[]{cell});
									}
								} catch(NumberFormatException ex) {
									// do nothing.. 
								}
							}
						} else {
							/***** EDIT MODE *****/
							if(cell.isVertex()) {
								try {
									Vertex selected = nodesViewReverse.get(cell);
									int id = selected.getId();
									if(editModeNodeSelector.alreadyExists(id)) {
										// set back to normal
										if(selected.getObjectId() >= 0) {
											graphView.setCellStyle(FILLED_NODE_STYLE, new Object[]{cell});
										} else {
											graphView.setCellStyle(EMPTY_NODE_STYLE, new Object[]{cell});
										}
									} else {
										if(editModeNodeSelector.getSelection().size() == NodeSelector.MAX_SELECTION) { 
											// set previous selected node back to normal style
											Vertex prev = nodesViewReverse.get(editModePreviousSelected);
											if(prev.getObjectId() >= 0) {
												graphView.setCellStyle(FILLED_NODE_STYLE, new Object[]{editModePreviousSelected});
											} else {
												graphView.setCellStyle(EMPTY_NODE_STYLE, new Object[]{editModePreviousSelected});
											}
										}

										editModeNodeSelector.addValue(id);
										editModePreviousSelected = cell;
										graphView.setCellStyles(mxConstants.STYLE_FILLCOLOR, "yellow", new Object[]{cell});
									}
								} catch(NumberFormatException ex) {
									// do nothing.. 
								}
							} 
						}
					}
				}
			}
		});
	}
	
	public void updateNodeModelPositions() {
		for(mxCell node : nodesViewReverse.keySet()) {
			int newX  = (int) node.getGeometry().getX();
			int newY  = (int) node.getGeometry().getY();
			nodesViewReverse.get(node).setNodeLocation(newX, newY);
		}
	}
	
	public void setEditMode(boolean editable) {
		editMode = editable;
		if(editMode) {
			editModeNodeSelector.clear();
			for(Integer nodeId : nodeSelector.getSelection()) { // return green (selected) nodes back to normal
				mxCell cell = ((mxCell) nodesView.get(nodeId));
				if(graph.getVertex(nodeId).getObjectId() >= 0) {
					graphView.setCellStyle(FILLED_NODE_STYLE, new Object[]{cell});
				} else {
					graphView.setCellStyle(EMPTY_NODE_STYLE, new Object[]{cell});
				}
			}
		} else {
			for(Integer nodeId : editModeNodeSelector.getSelection()) { // return yellow (selected) nodes back to normal
				mxCell cell = ((mxCell) nodesView.get(nodeId));
				if(graph.getVertex(nodeId).getObjectId() >= 0) {
					graphView.setCellStyle(FILLED_NODE_STYLE, new Object[]{cell});
				} else {
					graphView.setCellStyle(EMPTY_NODE_STYLE, new Object[]{cell});
				}
			}
			
			// set selected back to green
			@SuppressWarnings("unchecked")
			Vector<Integer> selected = (Vector<Integer>) nodeSelector.getSelection().clone(); // important: clone!
			for(int i=0; i < selected.size(); i++) {
				// check if selected was deleted
				if(graph.getVertex(selected.get(i)) == null) {
					nodeSelector.removeSelection(selected.get(i));
				} else {
					graphView.setCellStyles(mxConstants.STYLE_FILLCOLOR, "green", new Object[]{((mxCell) nodesView.get(selected.get(i)))});
				}
				
			}
		}
	}
	
	public void refresh() {
		graphComponent.refresh();
	}

	public Vector<Integer> getSelectedNodes() {
		return this.nodeSelector.getSelection();
	}
	
	public String getSelectedNodesMessage() {
		Vector<Integer> nodes = nodeSelector.getSelection();
		StringBuilder sb = new StringBuilder();
		if(nodes.size() == 0) {
			sb.append("Nodes selected: <font color=\"red\">-</font>");
		} else {
			sb.append("Nodes selected: ");
			for(Integer node : nodes) {
				sb.append("Node \"<font color=\"green\">" + node + "</font>\" ");
				
			}
		}
		return sb.toString();
	}
	
	public NodeSelector getNodeSelector() {
		return this.nodeSelector;
	}
	
	private void showEditNodeDialog(mxCell cell) {
		Vertex selected = nodesViewReverse.get(cell);
		JTextField id = new JTextField();
		id.setHorizontalAlignment(JTextField.CENTER);
		id.setEditable(false);
		id.setText(selected.toString());
		
		final JTextField objectId = new JTextField();
		objectId.setHorizontalAlignment(JTextField.CENTER);
		objectId.setToolTipText("Enter a new Object ID");
		int oId = selected.getObjectId();
		if(oId >= 0) {
			objectId.setText(oId + "");
		}
		
		JButton generateId = new JButton("Generate Object ID");
		generateId.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(objectId.getText().isEmpty()) {
					objectId.setText(graph.generateObjectId(-1) + "");
				} else {
					objectId.setText(graph.generateObjectId(Integer.parseInt(objectId.getText().trim())) + "");
				}
			}
		});
		
		Object[] message = {"Node ID:", id,
				    	    "Object ID:", objectId, generateId};
		Object[] options = {"OK","Cancel","Clear Node"};
		int result = JOptionPane.showOptionDialog(null, 
												  message, 
												  "Edit Node: " + selected, 
												  JOptionPane.OK_CANCEL_OPTION, 
												  JOptionPane.QUESTION_MESSAGE, 
												  null, 
												  options, "");
		if(result == JOptionPane.OK_OPTION) {
			try {
				String text = objectId.getText().trim();
				if(text.isEmpty()) {
					selected.setObjectId(Vertex.NO_OBJECT);
					if(isShowingObjectId) {
						cell.setValue(null);
					}
					cell.setStyle(EMPTY_NODE_STYLE);
				} else {
					// set new object id for this node
					int newObjectId = Integer.parseInt(text);
					if(newObjectId < 0) {
						JOptionPane.showMessageDialog(null,	
							     "Object ID: " + newObjectId + " has to be a positive number!", 
			                     "Error - Edit Node: " + selected, JOptionPane.ERROR_MESSAGE);
					} else {
						if(!graph.containObject(newObjectId)) {
							selected.setObjectId(newObjectId);
							cell.setStyle(FILLED_NODE_STYLE);
							if(isShowingObjectId) {
								cell.setValue(newObjectId);
							}
						} else {
							JOptionPane.showMessageDialog(null,	
													     "Object ID: " + newObjectId + " already exists!", 
									                     "Error - Edit Node: " + selected, JOptionPane.ERROR_MESSAGE);	
						}
					}
				}
				if(editModeNodeSelector.isSelected(selected.getId())) {
					graphView.setCellStyles(mxConstants.STYLE_FILLCOLOR, "yellow", new Object[]{cell});
				}
//				util.Log.appendln("Node " + selected.toString() + " edited.");
				refresh();
			} catch(NumberFormatException ex) {
				JOptionPane.showMessageDialog(null,	
						                      "Object ID hast to be a number\n" + ex.getLocalizedMessage(), 
						                      "Error - Edit Node: " + selected, JOptionPane.ERROR_MESSAGE);
			}
		} else if(options[result] == "Clear Node") {
			selected.setObjectId(Vertex.NO_OBJECT);
			cell.setStyle(EMPTY_NODE_STYLE);
			if(isShowingObjectId) {
				cell.setValue(null);
			}
			if(editModeNodeSelector.isSelected(selected.getId())) {
				graphView.setCellStyles(mxConstants.STYLE_FILLCOLOR, "yellow", new Object[]{cell});
			}
//			util.Log.appendln("Node " + selected.toString() + " edited.");
			refresh();
		}
	}
	
	/** Show a dialog to allow user to edit the edge's weight and probability
	 * @param cell the graphical representation of the edge
	 */
	private void showEditEdgeDialog(mxCell cell) {
		Edge selected = edgesView.get(cell);
		JTextField weight      = new JTextField();

		weight.setHorizontalAlignment(JTextField.CENTER);
		weight.setToolTipText("Enter new weight");
		weight.setText(selected.getWeight() + "");
		
		final JTextField probability = new JTextField();
		probability.setToolTipText("Enter new probability (0 < p <= 1.0)");
		probability.setText(((ProbabilisticGraph) graph).getProbability(selected) + "");
		probability.setHorizontalAlignment(JTextField.CENTER);
		
		JButton randomButton = new JButton("Generate Random Probability");
		randomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Random randomer = new Random(System.currentTimeMillis());
				double randomProb = randomer.nextDouble();
				DecimalFormat df = new DecimalFormat("#.##");
				String randomPString = df.format(randomProb);
				while(randomPString.matches("0") || randomPString.matches("1")) {
					randomProb = randomer.nextDouble(); // probability can't be zero or one
					randomPString = df.format(randomProb);
				}
				probability.setText(Double.valueOf(randomPString.replaceAll(",", ".")) + "");
			}
		});
		
		Object[] message = {"Weight:", weight, 
				            "Probability:", probability, randomButton};
		Object[] options = {"OK", "Cancel", "Set as Certain" };
		int result = JOptionPane.showOptionDialog(null, 
				                                  message, 
				                                  "Edit Edge: " + selected, 
				                                  JOptionPane.OK_CANCEL_OPTION, 
				                                  JOptionPane.QUESTION_MESSAGE, 
				                                  null, 
				                                  options, 
				                                  "");
		if(result == JOptionPane.OK_OPTION) {
			try {
				double p = Double.parseDouble(probability.getText());
				double w = Double.parseDouble(weight.getText());
				if(p <= 0 || p > 1.0) {
					throw new NumberFormatException("Probability have to be between than 0.0 (exclusive) and 1.0!");
				}
				
				if(w <= 0) {
					throw new NumberFormatException("Weight have to be greater than 0.0!");
				}
				selected.setWeight(w);
				((ProbabilisticGraph) graph).setProbability(selected, p);
				// set style
				if(p == 1.0) {
					cell.setStyle(NORMAL_EDGE_STYLE);
				} else {
					cell.setStyle(PROB_EDGE_STYLE);
				}
				// update labels
				if(isShowingWeight) {
					cell.setValue(w);
				} else {
					if(p == 1.0) {
						cell.setValue(null);
					} else {
						cell.setValue(p);
					}
				}
//				util.Log.appendln("Edge " + selected.toString() + " edited.");
				refresh();
			} catch(NumberFormatException ex) {
				JOptionPane.showMessageDialog(null,	ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if(options[result] == "Set as Certain") {
			((ProbabilisticGraph) graph).setProbability(selected, 1.0);
			cell.setStyle(NORMAL_EDGE_STYLE);
			if(!isShowingWeight) {
				cell.setValue(null);
			}
//			util.Log.appendln("Edge " + selected.toString() + " edited.");
			refresh();
		}
	}
	
	private void prepareStyles() {
		Hashtable<String, Object> emptyNodeStyle = new Hashtable<String, Object>();
		//style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.BLUE));
		//style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
		//style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
		emptyNodeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		emptyNodeStyle.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
		
//		Hashtable<String, Object> boxStyle = new Hashtable<String, Object>();
//		boxStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		
		Hashtable<String, Object> filledNodeStyle = new Hashtable<String, Object>();
//		filledNodeStyle.put(mxConstants.STYLE_FONTCOLOR, mxUtils.getHexColorString(Color.RED));
//		filledNodeStyle.put(mxConstants.STYLE_LABEL_BORDERCOLOR, mxUtils.getHexColorString(Color.RED));
//		filledNodeStyle.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.RED));
//		filledNodeStyle.put(mxConstants.STYLE_STROKEWIDTH, 3.0);
		filledNodeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
//		filledNodeStyle.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
		
		mxStylesheet stylesheet = graphView.getStylesheet();
		stylesheet.putCellStyle(EMPTY_NODE_STYLE, emptyNodeStyle);
		stylesheet.putCellStyle(FILLED_NODE_STYLE, filledNodeStyle);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == editNodeMenu) {
			showEditNodeDialog(editSelected);
		} else if(e.getSource() == removeNodeMenu) {
			
			// last node?
			if(graph.getAllVertices().size() == 1) {
				final ImageIcon icon = new ImageIcon(GraphVisualizer.class.getResource("/images/meme.png"));
				JOptionPane.showMessageDialog(this, 
											  "", 
											  "That was your last node...", 
											  JOptionPane.INFORMATION_MESSAGE, 
											  icon);
			}
			
			// remove from model
			Vertex toDelete = nodesViewReverse.get(editSelected);

			// remove connecting edges first
			Vector<Edge> connected = graph.getEdgesFrom(toDelete);
			for(Edge edge : connected) {
				graph.removeEdge(edge);
			}
			
			// 2nd: save to remove vertex
			nodesView.remove(toDelete.getId());
			nodesViewReverse.remove(editSelected);
			graph.removeVertex(toDelete);
			 
			// remove from gui
			graphView.removeCells(new Object[]{editSelected});
			
			// is it selected in edit mode?
			editModeNodeSelector.removeSelection(toDelete.getId());
			
//			util.Log.appendln("Node " + toDelete.getId() + " deleted");
			
			
		} else if(e.getSource() == editEdgeMenu) {
			showEditEdgeDialog(editSelected);
		} else if (e.getSource() == removeEdgeMenu) {

			// remove from model
			Edge toDelete = edgesView.get(editSelected);
			edgesView.remove(editSelected);
			graph.removeEdge(toDelete);
			
			// remove from gui
			graphView.removeCells(new Object[]{editSelected});
			
//			util.Log.appendln("Edge " + toDelete.toString() + " deleted");
			
		} else if (e.getSource() == createEdgeMenu) {
			int start  = editModeNodeSelector.getSelection().get(0);
			int target = editModeNodeSelector.getSelection().get(1);
			Edge edge = new Edge(graph.getVertex(start), graph.getVertex(target));
			edge.setWeight(1.0);
			graph.addEdge(edge);

			mxCell cell = (mxCell) graphView.insertEdge(parent, null, null, nodesView.get(start), 
					                                    nodesView.get(target), null);
			((ProbabilisticGraph) graph).setProbability(edge, 1.0);
			
			if(isShowingWeight) {
				editSelected.setValue(edge.getWeight());
			}
			cell.setStyle(NORMAL_EDGE_STYLE);
			edgesView.put(cell, edge);
//			util.Log.appendln("Edge " + edge.toString() + " created.");
			refresh();
		} else if(e.getSource() == addNodeMenu) {
			Vertex v = graph.addVertex();
			
			mxCell cell = (mxCell) graphView.insertVertex(parent, null, null, editEvent.getX(), editEvent.getY(), 
                    				GuiConstants.NODE_WIDTH, GuiConstants.NODE_HEIGHT, EMPTY_NODE_STYLE);
			if(isShowingObjectId) {
				int objectId = v.getObjectId();
				if(objectId == Vertex.NO_OBJECT) {
					cell.setValue(null);
				} else {
					cell.setValue(v.getObjectId());
				}
			} else {
				cell.setValue(v.getId());
			}
			nodesView.put(v.getId(), cell);
			nodesViewReverse.put(cell, v);
//			util.Log.appendln("Node " + v.toString() + " created.");
			refresh();
		}
	}
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	    	
	    }

	    public void mouseReleased(MouseEvent e) {
	    	if(editMode) {
	    		if (e.getButton() == MouseEvent.BUTTON3) {
		        	showPopup(e);
		        }
	    	}
	    }

	    private void showPopup(MouseEvent e) {
	    	preparePopUpMenu(e);
	        popUp.show(e.getComponent(),
                    e.getX(), e.getY());
	    }
	    
	    private void preparePopUpMenu(MouseEvent e) {
	    	popUp.removeAll();
	    	mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
	    	editEvent = e;
			if(cell != null) {
				editSelected = cell;
				if(cell.isVertex()) {
					Vertex v = nodesViewReverse.get(cell);
					editNodeMenu.setText("Edit Node \"" + v + "\"");
					removeNodeMenu.setText("Remove Node \"" + v + "\"");
					popUp.add(editNodeMenu);
					popUp.add(removeNodeMenu);
				} else if(cell.isEdge()) {
					Edge edge = edgesView.get(cell);
					editEdgeMenu.setText("Edit Edge " + edge.toString());
					removeEdgeMenu.setText("Remove Edge " + edge.toString());
					
					popUp.add(editEdgeMenu);
					popUp.add(removeEdgeMenu);
				}
			} else {
				popUp.add(addNodeMenu);
			}
			
			if(editModeNodeSelector.getSelection().size() == NodeSelector.MAX_SELECTION) {
				int start = editModeNodeSelector.getSelection().get(0);
				int target = editModeNodeSelector.getSelection().get(1);
				if(graph.getEdge(start, target) == null) {
					createEdgeMenu.setText("Create Edge " + editModeNodeSelector.getSelectionToString());
					popUp.add(createEdgeMenu);
				}
			}
	    }
	}
}
