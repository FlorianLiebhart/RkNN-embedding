package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.AlgorithmStrings;

public class OutlookBar extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JPanel topPanel         = new JPanel(new GridLayout(1, 1));
	private JPanel bottomPanel      = new JPanel(new GridLayout(1, 1));
	private ArrayList<BarInfo> bars = new ArrayList<BarInfo>();
	
	private int selectedBar;                        // The currently visible bar (zero-based index)
	private JComponent selectedBarComponent = null; // A place-holder for the currently visible component
	private boolean nonSelectionMode = false;

	public OutlookBar() {
		this.setLayout(new BorderLayout());
		this.setToolTipText("Select an action");		
		this.add(topPanel, BorderLayout.NORTH );
		this.add(bottomPanel, BorderLayout.SOUTH );
	}

	/**
	 * Create a new algorithm entry in the outlook bar
	 * 
	 * @param name : name of the algorithm (see: {@link AlgorithmStrings})
	 * @param component : the panel component providing the information 
	 * @param nodeNeeded : number of node(s) needed for the algorithm
	 * @param allowForMoreNode : the algorithm can also be executed if more than nodeNeeded is selected
	 */
	public void addBar(String name, JComponent component, int nodeNeeded, boolean allowForMoreNode) {
		BarInfo barInfo = new BarInfo(name, component, nodeNeeded, allowForMoreNode);
		barInfo.getButton().addActionListener(this);
		this.bars.add(barInfo);
	}

	/**
	 * Create a new algorithm entry in the outlook bar
	 * 
	 * @param name : name of the algorithm (see: {@link AlgorithmStrings})
	 * @param icon : icon data
	 * @param component : the panel component providing the information 
	 * @param nodeNeeded : number of node(s) needed for the algorithm
	 * @param allowForMoreNode : the algorithm can also be executed if more than nodeNeeded is selected
	 */
	public void addBar(String name, Icon icon, JComponent component, int nodeNeeded, boolean allowForMoreNode) {
		BarInfo barInfo = new BarInfo(name, icon, component, nodeNeeded, allowForMoreNode);
		barInfo.getButton().addActionListener( this );
		this.bars.add(barInfo);
	}

	/**
	 * @return the selected bar index
	 */
	public int getSelectedBar() {
		return this.selectedBar;
	}
	
	/**
	 * @return the selected algorithm
	 */
	public String getSelectedAlgorithm() {
		return this.bars.get(selectedBar).getName();
	}

	public void setSelectedBar(int selectedBar) {
		if(selectedBar >= 0 && selectedBar < this.bars.size()) {
			this.selectedBar = selectedBar;
			render();
		}
	}

	public void render() {
		int topBars    = this.selectedBar + 1;
		int bottomBars = this.bars.size() - topBars;

		// Render the top bars
		this.topPanel.removeAll();
		GridLayout topLayout = (GridLayout) this.topPanel.getLayout();
		topLayout.setRows(topBars);
		for(int i = 0; i < topBars; i++) {
			this.topPanel.add(bars.get(i).getButton());
			bars.get(i).setNotSelectedIcon();
		}
		this.topPanel.validate();

		// Render the center component
		if(this.selectedBarComponent != null) {
			this.remove(this.selectedBarComponent);
		}
		
		if(!nonSelectionMode) {
			this.selectedBarComponent = bars.get(selectedBar).getComponent();
			bars.get(selectedBar).setSelectedIcon();
			this.add(selectedBarComponent, BorderLayout.CENTER);
		}
		
		// Render the bottom bars
		this.bottomPanel.removeAll();
		GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();
		bottomLayout.setRows(bottomBars);
		for(int i = topBars; i < bars.size(); i++) {
			bottomPanel.add(bars.get(i).getButton());
			bars.get(i).setNotSelectedIcon();
		}
		this.bottomPanel.validate();
		
		this.validate();
	}

	public void actionPerformed( ActionEvent e ) {
		Object source = e.getSource();
		for(int i = 0; i < bars.size(); i++) {
			if(bars.get(i).getButton() == source) {
				nonSelectionMode = false;
				setSelectedBar(i);
				break;
			}
		}
	}
	
	public int getTotalBar() {
		return this.bars.size();
	}
	
	public void updateBar(int nodeSelected) {
		for(BarInfo bi : bars) {
			if(bi.nodeNeeded == nodeSelected) {
				bi.getButton().setEnabled(true);
			} else {
				if(bi.nodeNeeded < nodeSelected && bi.allowForMoreNode) {
					bi.getButton().setEnabled(true);
				} else {
					bi.getButton().setEnabled(false);
				}
			}
		}
	}
	
	public void setNonSelection() {
		this.nonSelectionMode = true;
		setSelectedBar(this.bars.size() - 1);
	}
	
	public void setAllDisabled() {
		setNonSelection();
		for(BarInfo bi : bars) {
			bi.getButton().setEnabled(false);
			bi.setNotSelectedIcon();
		}
	}
	
	public void setAllEnabled() {
		setNonSelection();
		for(BarInfo bi : bars) {
			bi.getButton().setEnabled(true);
		}
	}
	
	public void setEnabled(String name) {
		for(BarInfo bi : bars) {
			if(bi.getName().equals(name)) {
				bi.getButton().setEnabled(true);
			}
		}
	}
	
	public void setDisabled(String name) {
		for(BarInfo bi : bars) {
			if(bi.getName().equals(name)) {
				bi.getButton().setEnabled(false);
			}
		}
	}

	/**
	 * Internal class that maintains information about individual Outlook bars
	 */
	private class BarInfo {
		private String name;
		private JButton button;
		private JComponent component;
		private int nodeNeeded;
		private boolean allowForMoreNode;

		public BarInfo(String name, JComponent component, int nodeNeeded, boolean allowForMoreNode) {
			this.name = name;
		    this.component = component;
		    this.button = new JButton(name);
		    this.nodeNeeded = nodeNeeded;
		    this.allowForMoreNode = allowForMoreNode;
		    this.button.setHorizontalAlignment(JLabel.LEFT);
		    if(name.equals("DUMMY")) { // nur als platzhalter
		    	this.button.setText("");
		    	this.button.setVisible(false);
		    }
		 }

		 public BarInfo(String name, Icon icon, JComponent component, int nodeNeeded, boolean allowForMoreNode) {
			 this.name = name;
			 this.component = component;
		   	 this.button = new JButton(name, icon);
		   	this.nodeNeeded = nodeNeeded;
		    this.allowForMoreNode = allowForMoreNode;
		   	 if(name.equals("DUMMY")) {
		    	this.button.setText("");
		    	this.button.setVisible(false);
		   	 }
		 }
		 
		 private void setSelectedIcon() {
			 this.button.setIcon(new ImageIcon(getClass().getResource("/images/green.png")));
		 }
		 
		 private void setNotSelectedIcon() {
			 this.button.setIcon(new ImageIcon(getClass().getResource("/images/red.png")));
		 }

		 public String getName() {
			 return this.name;
		 }

		 public JButton getButton() {
			 return this.button;
		 }

		 public JComponent getComponent() {
			 return this.component;
		 }
	}
}
