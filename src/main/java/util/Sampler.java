package util;

import graph.ProbabilisticGraph;
import graph.core.Edge;
import graph.core.Graph;
import graph.core.Vertex;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class Sampler {

	private static Sampler instance = null;
	
	private final Random randomer;
	private HashMap<Edge, Boolean> sampledEdges; 
	
	private Sampler() {
		randomer = new Random();
		sampledEdges = new HashMap<Edge, Boolean>();
	}
	
	public static Sampler getInstance() {
		if(instance == null) {
			instance = new Sampler();
		}
		return instance;
	}
	
	public void reset() {
		sampledEdges = new HashMap<Edge, Boolean>();
	}
	
	public Vector<Edge> getAllEdgesFromGraph() {
		return ProbabilisticGraph.getInstance().getAllEdges();
	}
	
	public Vector<Vertex> getAllNodesFromGraph() {
		return ProbabilisticGraph.getInstance().getAllVertices();
	}
	
	public Double getEdgeWeight(int startNodeId, int endNodeId) {
		Edge e = ProbabilisticGraph.getInstance().getEdge(startNodeId, endNodeId);
		if(sampledEdges.get(e)) {
			return e.getWeight();
		} else {
			return null;
		}
	}
	
	public Vector<Vertex> sampleEdges(Vertex home) {
		Vector<Vertex> edges = new Vector<Vertex>();
		
		for(Edge e : ProbabilisticGraph.getInstance().getEdgesFrom(home)) {
			//if(e.getSource() == home || e.getTarget() == home) { // find edge
				Vertex v = (home == e.getSource()) ? e.getTarget() : e.getSource();
				try {
					if(sampledEdges.get(e)) {
						edges.add(v);
					}
				} catch(NullPointerException ex) { // edge has to be sampled first
					if(randomer.nextDouble() <= getEdgeProbability(e)) {
						sampledEdges.put(e, true);
						edges.add(v);
					} else {
						sampledEdges.put(e, false);
					}
				}
			//}
		}
		
		/*for(Edge e : ProbabilisticGraph.getInstance().getAllEdges()) {
			if(e.getSource() == home || e.getTarget() == home) { // find edge
				Vertex v = (home == e.getSource()) ? e.getTarget() : e.getSource();
				try {
					if(sampledEdges.get(e)) {
						edges.add(v);
					}
				} catch(NullPointerException ex) { // edge has to be sampled first
					if(randomer.nextDouble() <= getEdgeProbability(e)) {
						sampledEdges.put(e, true);
						edges.add(v);
					} else {
						sampledEdges.put(e, false);
					}
				}
			}
		}*/
		
		return edges;
	}
	
	public void sampleAllEdges() {
		for(Edge e : ProbabilisticGraph.getInstance().getAllEdges()) {
			if(randomer.nextDouble() <= getEdgeProbability(e)) {
				sampledEdges.put(e, true);
			} else {
				sampledEdges.put(e, false);
			}
		}
	}
	
	public double getEdgeProbability(Edge e) {
		return ProbabilisticGraph.getInstance().getProbability(e);
	}
	
	public Vertex getVertex(int id) {
		return ProbabilisticGraph.getInstance().getVertex(id);
	}

	public Graph generateSampledGraph() {
		Graph sGraph = new Graph();
		
		sGraph.setVertices(ProbabilisticGraph.getInstance().getAllVertices());
		
		Vector<Edge> newEdges = new Vector<Edge>();
		for(Edge e : ProbabilisticGraph.getInstance().getAllEdges()) {
			double p = ProbabilisticGraph.getInstance().getProbability(e);
			if(randomer.nextDouble() <= p) {
				newEdges.add(e);
			}
		}
		sGraph.setEdges(newEdges);
		
		return sGraph;
	}

	public static double calculateSampleGraphPosibility(Graph sampledGraph) {
		double prob = 1.0;
		Vector<Edge> choosenEdges = sampledGraph.getAllEdges();
		Vector<Edge> allEdges = ProbabilisticGraph.getInstance().getAllEdges();
		for(Edge e : allEdges) {
			if(choosenEdges.contains(e)) {
				prob *= ProbabilisticGraph.getInstance().getProbability(e);
			} else {
				prob *= (1 - ProbabilisticGraph.getInstance().getProbability(e));
			}
		}
		
		return prob;
	}
}
