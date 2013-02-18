package view;

import java.util.Observable;
import java.util.Vector;

public class NodeSelector extends Observable {
	public static final int MAX_SELECTION = 2;
	
	private Vector<Integer> selection = new Vector<Integer>();
	
	public NodeSelector() {
		
	}

	public void addValue(int val) {
		if(selection.size() == MAX_SELECTION) {
			selection.set(selection.size() - 1, val);
			update();
		} else {
			selection.add(val);
			update();
		}
	}
	
	public boolean isSelected(int nodeId) {
		for(Integer i : selection) {
			if(nodeId == i) {
				return true;
			}
		}
		return false;
	}
	
	public boolean alreadyExists(int val) {
		for(Integer i : selection) {
			if(i == val) {
				selection.remove(i); // doppelt selektiert -> löschen
				update();
				return true;
			}
		}
		return false;
	}
	
	public Vector<Integer> getSelection() {
		return this.selection;
	}
	
	public void removeSelection(int nodeId) {
		selection.remove(new Integer(nodeId));
	}
	
	public void update() {
		setChanged();
		notifyObservers();
	}
	
	public void clear() {
		selection.clear();
	}
	
	public String getSelectionToString() {
		if(selection.size() == MAX_SELECTION) {
			return "(" + selection.get(0) + " - " + selection.get(1) + ")";
		}
		return "";
	}
}
