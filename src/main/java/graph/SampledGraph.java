package graph;

import graph.core.Edge;
import graph.core.Graph;

import java.util.Vector;

public class SampledGraph extends Graph {
	private double probability;
	private Vector<Edge> uncertainEdges;
	
	public SampledGraph() {
		super();
		uncertainEdges = new Vector<Edge>();
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	public void addUncertainEdge(Edge e) {
		uncertainEdges.add(e);
	}
	
	public Vector<Edge> getUncertainEdges() {
		return uncertainEdges;
	}
}
