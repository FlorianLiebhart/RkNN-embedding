package util;

import graph.core.Graph;
import graph.core.Vertex;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;



public abstract class GraphAlgorithm {
	
	public static Vector<Integer> getReverseNearestNeighbors(Vertex home) {
		Vector<Integer> rNeighbors = new Vector<Integer>();
		
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			if(!v.containsObject() || v == home) {
				continue;
			}
			
			Vertex nearest = getNearestNeighboursOnTheFly(v);
			if(nearest == home) {
				rNeighbors.add(v.getId());
			}
		}
		Sampler.getInstance().reset();
		return rNeighbors;
	}
	
	
	public static Vector<Integer> getKNearestNeighboursOnTheFly(Vertex start, int k) {
		Vector<Integer> neighbors = new Vector<Integer>();
		
		// initial values
		Vector<Vertex> unvisitedNodes = new Vector<Vertex>(); // TODO braucht man das?
		HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			if(v == start) {
				nodeDistance.put(v, 0.0);
			} else {
				nodeDistance.put(v, Double.POSITIVE_INFINITY); 
				unvisitedNodes.add(v);
			}
		}
		
		Vertex currentNode = start;
		while(true) {
			for(Vertex v : Sampler.getInstance().sampleEdges(currentNode)) { 
				double tDistance = nodeDistance.get(currentNode) + Sampler.getInstance().getEdgeWeight(currentNode.getId(), v.getId());
				if(tDistance < nodeDistance.get(v)) {
					nodeDistance.put(v, tDistance);
				}
			}
			
			if(currentNode != start) {
				unvisitedNodes.remove(currentNode);
			}
			
//			boolean finish = true;
//			for(Vertex v : unvisitedNodes) {
//				if(nodeDistance.get(v) < Double.POSITIVE_INFINITY) { 
//					finish = false;
//					break;
//				} 
//			}
//			
//			if(finish) {
//				break;
//			}
			
			if(unvisitedNodes.size() == 0) {
				break;
			}
			
			Vertex smallest = null; // get node with smallest tentative distance
			for(Vertex v : unvisitedNodes) {
				if(smallest == null) {
					smallest = v;
				} else {
					if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
						smallest = v;
					}
				}
			}
			
			if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) { 
				break;
			}
			
			currentNode = smallest; // ...set it as 'current node'
			
			if(!neighbors.contains(smallest.getId())) {
				if(smallest.containsObject()) { // ...and add it to knn if it has object
					neighbors.add(smallest.getId());
				}
			}
			if(neighbors.size() == k) {
				break;
			}
		} // end while
		Sampler.getInstance().reset();
		return neighbors;
	}

	
	public static Vertex getNearestNeighboursOnTheFly(Vertex start) {
		Vertex nearest = null;
		// initial values
		Vector<Vertex> unvisitedNodes = new Vector<Vertex>(); 
		HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
		
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			if(v == start) {
				nodeDistance.put(v, 0.0);
			} else {
				nodeDistance.put(v, Double.POSITIVE_INFINITY); 
				unvisitedNodes.add(v);
			}
		}
		
		Vertex currentNode = start;
		while(true) {
			for(Vertex v : Sampler.getInstance().sampleEdges(currentNode)) {
				double tDistance = nodeDistance.get(currentNode) + Sampler.getInstance().getEdgeWeight(currentNode.getId(), v.getId());
				if(tDistance < nodeDistance.get(v)) {
					nodeDistance.put(v, tDistance);
				}
			}
			
			if(currentNode != start) {
				unvisitedNodes.remove(currentNode);
			}
			
			if(unvisitedNodes.size() == 0) {
				break;
			}
			
			Vertex smallest = null;// get node with smallest tentative distance
			for(Vertex v : unvisitedNodes) {
				if(smallest == null) {
					smallest = v;
				} else {
					if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
						smallest = v;
					}
				}
			}
			
			if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) { 
				break;
			}
			
			currentNode = smallest; // ...set it as 'current node'
			
			if(smallest.containsObject()) {
				nearest = smallest;
				break;
			}

		} // end while
//		Sampler.getInstance().reset(); -> will be reseted in getReverseNearestNeighbors()
		return nearest;
	}
	
	public static Vector<Integer> getKNearestNeighbours(Graph g, Vertex start, int k) {
		Vector<Integer> neighbors = new Vector<Integer>();
		
		// initial values
		Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
		HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			if(v == start) {
				nodeDistance.put(v, 0.0);
			} else {
				nodeDistance.put(v, Double.POSITIVE_INFINITY); 
				unvisitedNodes.add(v);
			}
		}
		
		Vertex currentNode = start;
		while(true) {
			for(Vertex v : g.getNeighborsFrom(currentNode)) { //  graph.getNeighborsFrom(currentNode)) {
				double tDistance = nodeDistance.get(currentNode) + g.getEdge(v, currentNode).getWeight();
				if(tDistance < nodeDistance.get(v)) {
					nodeDistance.put(v, tDistance);
				}
			}
			
			if(currentNode != start) {
				unvisitedNodes.remove(currentNode);
			}
			
//			boolean finish = true;
//			for(Vertex v : unvisitedNodes) {
//				if(nodeDistance.get(v) < Double.POSITIVE_INFINITY) { 
//					finish = false;
//					break;
//				} 
//			}
//			
//			if(finish) {
//				break;
//			}
			
			if(unvisitedNodes.size() == 0) {
				break;
			}
			
			Vertex smallest = null; // get node with smallest tentative distance
			for(Vertex v : unvisitedNodes) {
				if(smallest == null) {
					smallest = v;
				} else {
					if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
						smallest = v;
					}
				}
			}
			
			if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) {
				break;
			}
			
			currentNode = smallest; // ...set it as 'current node'
			if(!neighbors.contains(smallest.getId())) {
				if(smallest.containsObject()) { // ...and add it to knn if it has object
					neighbors.add(smallest.getId());
				}
			}
			if(neighbors.size() == k) {
				break;
			}
		} // end while
		
		return neighbors;
	}
	
	
	public static int getReachableNodesOnTheFly(Vertex start) {
		// initial values
		Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
		Vector<Vertex> reachableNodes = new Vector<Vertex>();
		HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			if(v == start) {
				nodeDistance.put(v, 0.0);
			} else {
				nodeDistance.put(v, Double.POSITIVE_INFINITY); 
				unvisitedNodes.add(v);
			}
		}
		
		Vertex currentNode = start;
		while(true) {
			for(Vertex v : Sampler.getInstance().sampleEdges(currentNode)) {
				double tDistance = nodeDistance.get(currentNode) + Sampler.getInstance().getEdgeWeight(currentNode.getId(), v.getId());
				if(tDistance < nodeDistance.get(v)) {
					nodeDistance.put(v, tDistance);
				}
			}
			
			if(currentNode != start) {
				unvisitedNodes.remove(currentNode);
				if(!reachableNodes.contains(currentNode)) {
					reachableNodes.add(currentNode);
				}
			}
			
			if(unvisitedNodes.size() == 0) {
				break;
			}
			
			Vertex smallest = null; // get node with smallest tentative distance
			for(Vertex v : unvisitedNodes) {
				if(smallest == null) {
					smallest = v;
				} else {
					if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
						smallest = v;
					}
				}
			}
			
			if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) { 
				break;
			}
			
			currentNode = smallest; // ...set it as 'current node'
		} // end while
		Sampler.getInstance().reset();
		return reachableNodes.size();
	}

	public static int getReachableNodes(Graph graph, Vertex start) {
		// initial values
		Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
		Vector<Vertex> reachableNodes = new Vector<Vertex>();
		HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
		for(Vertex v : graph.getAllVertices()) {
			if(v == start) {
				nodeDistance.put(v, 0.0);
			} else {
				nodeDistance.put(v, Double.POSITIVE_INFINITY); // -1 : infinity
				unvisitedNodes.add(v);
			}
		}
		
		Vertex currentNode = start;
		while(true) {
			for(Vertex v : graph.getNeighborsFrom(currentNode)) {
				double tDistance = nodeDistance.get(currentNode) + graph.getEdge(currentNode.getId(), v.getId()).getWeight();
				if(tDistance < nodeDistance.get(v)) {
					nodeDistance.put(v, tDistance);
				}
			}
			
			if(currentNode != start) {
				unvisitedNodes.remove(currentNode);
				if(!reachableNodes.contains(currentNode)) {
					reachableNodes.add(currentNode);
				}
			}
			
			if(unvisitedNodes.size() == 0) {
				break;
			}
			
			Vertex smallest = null; // get node with smallest tentative distance
			for(Vertex v : unvisitedNodes) {
				if(smallest == null) {
					smallest = v;
				} else {
					if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
						smallest = v;
					}
				}
			}
			
			if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) { // TODO check
				break;
			}
			
			currentNode = smallest; // ...set it as 'current node'
		} // end while
		
		return reachableNodes.size();
	}

	

	public static int getConnectedComponents() {
		// initial values
		int connectedComponents = 0;
			
		Vector<Vertex> toAnalyzedNodes = new Vector<Vertex>();
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			toAnalyzedNodes.add(v);
		}
		Random indexRandomer = new Random(System.currentTimeMillis());
		
		while(toAnalyzedNodes.size() > 0) {
			// start dijkstra, until all nodes visited
			HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
			Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
			
			Vertex start = toAnalyzedNodes.get(indexRandomer.nextInt(toAnalyzedNodes.size()));
			for(Vertex v : toAnalyzedNodes) { // init values
				if(v == start) {
					nodeDistance.put(v, 0.0);
				} else {
					nodeDistance.put(v, Double.POSITIVE_INFINITY); 
					unvisitedNodes.add(v);
				}
			}
			toAnalyzedNodes.remove(start);
			
			Vertex currentNode = start;
			while(true) {
				for(Vertex v : Sampler.getInstance().sampleEdges(currentNode)) {
					double tDistance = nodeDistance.get(currentNode) + Sampler.getInstance().getEdgeWeight(currentNode.getId(), v.getId());
					if(tDistance < nodeDistance.get(v)) {
						nodeDistance.put(v, tDistance);
					}
				}
				
				if(currentNode != start) {
					unvisitedNodes.remove(currentNode);
					toAnalyzedNodes.remove(currentNode);
				}
				
				boolean finish = true;
				for(Vertex v : unvisitedNodes) {
					if(nodeDistance.get(v) < Double.POSITIVE_INFINITY) { 
						finish = false;
						break;
					} 
				}
				
				if(finish) {
					connectedComponents++;
					break;
				}
				
				Vertex smallest = null; // get node with smallest tentative distance
				for(Vertex v : unvisitedNodes) {
					if(smallest == null) {
						smallest = v;
					} else {
						if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
							smallest = v; 
						}
					}
				}
				currentNode = smallest; // ...set it as 'current node'
			} // end while
		}
		Sampler.getInstance().reset();
		return connectedComponents;
	}

	public static Graph getLargestConnectedComponent() {
			Graph subGraph = null;
			Vector<Vector<Vertex>> allConnectedComponents = new Vector<Vector<Vertex>>();
			
			// initial values
			Vector<Vertex> toAnalyzedNodes = new Vector<Vertex>();
			for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
				toAnalyzedNodes.add(v);
			}
			Random indexRandomer = new Random(System.currentTimeMillis());
			
			while(toAnalyzedNodes.size() > 0) {
				HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
				Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
				Vector<Vertex> connectedComponents = new Vector<Vertex>();
				
				Vertex start = toAnalyzedNodes.get(indexRandomer.nextInt(toAnalyzedNodes.size()));
				for(Vertex v : toAnalyzedNodes) {
					if(v == start) {
						nodeDistance.put(v, 0.0);
					} else {
						nodeDistance.put(v, Double.POSITIVE_INFINITY); 
						unvisitedNodes.add(v);
					}
				}
				toAnalyzedNodes.remove(start);
				connectedComponents.add(start);
				
				Vertex currentNode = start;
				while(true) {
					for(Vertex v : Sampler.getInstance().sampleEdges(currentNode)) {
						double tDistance = nodeDistance.get(currentNode) + Sampler.getInstance().getEdgeWeight(currentNode.getId(), v.getId());
						if(tDistance < nodeDistance.get(v)) {
							nodeDistance.put(v, tDistance);
						}
					}
					
					if(currentNode != start) {
						unvisitedNodes.remove(currentNode);
						toAnalyzedNodes.remove(currentNode);
						connectedComponents.add(currentNode);
					}
					
					if(unvisitedNodes.size() == 0) {
						allConnectedComponents.add(connectedComponents);
						break;
					}
					
					Vertex smallest = null; // get node with smallest tentative distance
					for(Vertex v : unvisitedNodes) {
						if(smallest == null) {
							smallest = v;
						} else {
							if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
								smallest = v; 
							}
						}
					}
					
					if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) { 
						allConnectedComponents.add(connectedComponents);
						break;
					}
					
					currentNode = smallest; // ...set it as 'current node'
				} // end while
				
				// if the size of one of the discovered connected components >= rest of the nodes -> we're done
				for(Vector<Vertex> conComp : allConnectedComponents) {
					if(conComp.size() >= toAnalyzedNodes.size()) {
						break; // finish
					}
				}
			}
			
			Vector<Vertex> biggest = null;
			for(Vector<Vertex> connectedComponents : allConnectedComponents) {
				if(biggest == null) {
					biggest = connectedComponents;
				} else {
					if(connectedComponents.size() > biggest.size()) {
						biggest = connectedComponents;
					}
				}
			}
			
			subGraph = GraphAlgorithm.getSubGraph(biggest);
	//		setNodesPosition(subGraph); NOoooo
			Sampler.getInstance().reset();
			
			return subGraph;
		}

	public static Graph getSubGraph(Vector<Vertex> nodes) {
		Graph subGraph = new Graph();
		
		Vector<Vertex> subGraphNodes = subGraph.getAllVertices();
//		Vector<Edge> subGraphEdges   = subGraph.getAllEdges();
		for(Vertex v : nodes) {
			subGraphNodes.add(v);
			// TODO!!! currently only vertices are shown
		}
		
		return subGraph;
		
	}

	public static double getShortestPath(Graph graph, Vertex start, Vertex target) {
			// initial values
			Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
	//		HashMap<Vertex, Vertex> previous = new HashMap<Vertex, Vertex>();     // this shows the path
			HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
			for(Vertex v : graph.getAllVertices()) {
				if(v == start) {
					nodeDistance.put(v, 0.0);
				} else {
					nodeDistance.put(v, Double.POSITIVE_INFINITY); // -1 : infinity
	//				previous.put(v, null);
					unvisitedNodes.add(v);
				}
			}
			
			Vertex currentNode = start;
			while(true) {
				for(Vertex v : graph.getNeighborsFrom(currentNode)) {
					double tDistance = nodeDistance.get(currentNode) + graph.getEdge(currentNode.getId(), v.getId()).getWeight();
					if(tDistance < nodeDistance.get(v)) {
						nodeDistance.put(v, tDistance);
	//					previous.put(v, currentNode);
					}
				}
				
				if(currentNode != start) {
					unvisitedNodes.remove(currentNode);
				}
				
				if(currentNode == target) {
					break;
				}
				
				boolean finish = true;
				for(Vertex v : unvisitedNodes) {
					if(nodeDistance.get(v) < Double.POSITIVE_INFINITY) { 
						finish = false;
						break;
					} 
				}
				
				if(finish) {
					break;
				}
				
				Vertex smallest = null; // get node with smallest tentative distance
				for(Vertex v : unvisitedNodes) {
					if(smallest == null) {
						smallest = v;
					} else {
						if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
							smallest = v; 
						}
					}
				}
				
				currentNode = smallest; // ...set it as 'current node'
			} // end while
			
			return nodeDistance.get(target);
	}

	public static double getShortestPathOnTheFly(Vertex start, Vertex target) {
		// initial values
		Vector<Vertex> unvisitedNodes = new Vector<Vertex>();
		HashMap<Vertex, Double> nodeDistance = new HashMap<Vertex, Double>(); // tentative distance value for every node
		for(Vertex v : Sampler.getInstance().getAllNodesFromGraph()) {
			if(v == start) {
				nodeDistance.put(v, 0.0);
			} else {
				nodeDistance.put(v, Double.POSITIVE_INFINITY);
				unvisitedNodes.add(v);
			}
		}
		
		Vertex currentNode = start;
		while(true) {
			for(Vertex v : Sampler.getInstance().sampleEdges(currentNode)) {
				double tDistance = nodeDistance.get(currentNode) + Sampler.getInstance().getEdgeWeight(currentNode.getId(), v.getId());
				if(tDistance < nodeDistance.get(v)) {
					nodeDistance.put(v, tDistance);
				}
			}
			
			if(currentNode != start) {
				unvisitedNodes.remove(currentNode);
			}
			
			if(currentNode == target) {
				break;
			}
			
			if(unvisitedNodes.size() == 0) {
				break;
			}
			
			Vertex smallest = null; // get node with smallest tentative distance
			for(Vertex v : unvisitedNodes) {
				if(smallest == null) {
					smallest = v;
				} else {
					if(nodeDistance.get(v) < nodeDistance.get(smallest)) {
						smallest = v; 
					}
				}
			}
			
			if(nodeDistance.get(smallest) == Double.POSITIVE_INFINITY) { // TODO check
				break;
			}
			
			currentNode = smallest; // ...set it as 'current node'
		} // end while
		Sampler.getInstance().reset();
		return nodeDistance.get(target);
	}

	public static double getGraphDiameter() {
		Sampler.getInstance().sampleAllEdges();
		
		double[][] a = GraphAlgorithm.getAllPairShortestPath();
		
		double max = -1.0;
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a.length; j++) {
				if(a[i][j] == Double.POSITIVE_INFINITY) {
					continue; // TODO not sure...
				}
				if(a[i][j] > max) {
					max = a[i][j];
				}
			}
		}
		Sampler.getInstance().reset();
		return max;
	}

	/** Floyd-Warshall-Algorithm.
	 *  Used for calculating graph diameter.
	 * @param
	 * @return
	 */
	private static double[][] getAllPairShortestPath() {
		int n = Sampler.getInstance().getAllNodesFromGraph().size();
		double[][] a = new double[n][n];
		
		// init
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(i == j) {
					a[i][j] = 0;
				} else {
					try {
						a[i][j] = Sampler.getInstance().getEdgeWeight(i, j);
					} catch(NullPointerException ex) {
						a[i][j] = Double.POSITIVE_INFINITY;
					}
				}
			}
		}
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				for(int k = 0; k < n; k++) {
					if(a[j][i] + a[i][k] < a[j][k]) {
						a[j][k] = a[j][i] + a[i][k];
					}
				}
			}
		}
		
		return a;
	}

}
