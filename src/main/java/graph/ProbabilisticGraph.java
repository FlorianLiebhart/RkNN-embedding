package graph;

import graph.core.Edge;
import graph.core.Graph;

import java.util.HashMap;
import java.util.Random;


public class ProbabilisticGraph extends Graph {
	
	private static ProbabilisticGraph instance = null;
	
	private HashMap<Edge, Double> prob;
	public Random randomer = new Random(System.currentTimeMillis());
	
	private ProbabilisticGraph() {
		super();
		prob  = new HashMap<Edge, Double>();
	}
	
	public static ProbabilisticGraph getInstance() {
		if(instance == null) {
			instance = new ProbabilisticGraph();
		}
		return instance;
	}
	
	public void clear() {
		instance = null;
	}

	public void setProbability(Edge edge, double prob) {
		if(!getAllEdges().contains(edge)) {
			throw new RuntimeException("Graph doesn't have this edge: " + edge.toString());
		} else {
			this.prob.put(edge, prob);
		}
	}

	public double getProbability(Edge edge) {
		if(!getAllEdges().contains(edge)) {
			throw new RuntimeException("Graph doesn't have this edge: " + edge.toString());
		} 
		
		return prob.get(edge);
	}
	
	public int getNumberOfUncertainEdges() {
		int count = 0;
		for(Edge e : this.getAllEdges()) {
			if(this.getProbability(e) < 1.0) {
				count++;
			} 
		}
		return count;
	}
	
}
