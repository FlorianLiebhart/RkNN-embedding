package graph.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

public class Graph {
	private HashMap<Integer, Vertex> vertices;
	private HashSet<Edge> edges;
	private HashMap<Vertex, HashMap<Vertex, Edge>> mapping = new HashMap<Vertex, HashMap<Vertex, Edge>>();
	
	public Graph() {
		vertices = new HashMap<Integer, Vertex>();
		edges    = new HashSet<Edge>();
	}
	
	public void addVertex(Vertex v) {
		this.vertices.put(new Integer(v.getId()), v);
		this.mapping.put(v, new HashMap<Vertex,Edge>());
	}

	public Vertex addVertex() {
		int id = 0;
		while(true) {
			if(getVertex(id) == null) {
				break;
			}
			id++;
		}
		Vertex v = new Vertex(id);

		this.vertices.put(new Integer(v.getId()), v);
		this.mapping.put(v, new HashMap<Vertex,Edge>());

		return v;
	}

	public Vertex getVertex(int nodeId) {
		//for(Vertex v : vertices) {
		//	if(v.getId() == nodeId) {
		//		return v;
		//	}
		//}
		return vertices.get(new Integer(nodeId));
	}

	public void removeVertex(Vertex v) {
		this.vertices.remove(v.getId());

		HashMap<Vertex, Edge> m = this.mapping.get(v);
		for(Edge e: m.values()){
			if(e.getSource() == v || e.getTarget() == v) {
				this.edges.remove(e);
				this.mapping.get(e.getSource()).remove(e.getTarget());
				this.mapping.get(e.getTarget()).remove(e.getSource());
			}
		}
	}

	public void setVertices(Vector<Vertex> vertices) {
		this.vertices = new HashMap<Integer, Vertex>();
		this.mapping = new HashMap<Vertex, HashMap<Vertex, Edge>>();
		for(Vertex v: vertices){
			this.vertices.put(new Integer(v.getId()), v);
			this.mapping.put(v, new HashMap<Vertex,Edge>());
		}
	}

	public void addEdge(Edge e) {
		this.edges.add(e);
		try {
			this.mapping.get(e.getSource()).put(e.getTarget(), e);
			this.mapping.get(e.getTarget()).put(e.getSource(), e);
		} catch(NullPointerException ex) {
			System.out.println("Error adding edge " + e.toString() + ": " + ex.getLocalizedMessage());
		}
	}
	
	public void removeEdge(Edge e) {
		this.edges.remove(e);
		this.mapping.get(e.getSource()).remove(e.getTarget());
		this.mapping.get(e.getTarget()).remove(e.getSource());
	}
	
	public void setEdges(Vector<Edge> edges) {
		this.edges = new HashSet<Edge>();
		this.mapping = new HashMap<Vertex, HashMap<Vertex, Edge>>();
		
		for(Vertex v: this.vertices.values()){
			this.mapping.put(v, new HashMap<Vertex,Edge>());
		}
		
		for(Edge e: edges){
			this.addEdge(e);
		}	
	}
	
	public Edge getEdge(int v1, int v2) {
		Vertex start = getVertex(v1);
		Vertex end   = getVertex(v2);
		
		if(start == null || end == null) {
			return null;
		}
		
		return mapping.get(start).get(end);
	}
	
	public Edge getEdge(Vertex start, Vertex end) {
		
		if(start == null || end == null) {
			return null;
		}
		
		return mapping.get(start).get(end);
	}
	
	public boolean containsEdge(int v1, int v2) {
		Vertex start = getVertex(v1);
		Vertex end   = getVertex(v2);
		
		if(start == null || end == null) {
			return false;
		}
		
		return mapping.get(start).get(end) != null;
	}
	
	public boolean containsEdge(Edge edge) {
		Vertex start = edge.getSource();
		Vertex end   = edge.getTarget();
		
		if(start == null || end == null) {
			return false;
		}
		
		return mapping.get(start).get(end) != null;
	}
	
	public Vector<Vertex> getNeighborsFrom(Vertex home) {
		Vector<Vertex> neighbors = new Vector<Vertex>();
		
		/*for(Edge e : edges) {
			if(e.getSource() == home) {
				neighbors.add(e.getTarget());
			} else if(e.getTarget() == home) {
				neighbors.add(e.getSource());
			}
		}*/
		HashMap<Vertex, Edge> m = this.mapping.get(home);
		for(Edge e: m.values()){
			if(e.getSource() == home){neighbors.add(e.getTarget());}
			if(e.getTarget() == home){neighbors.add(e.getSource());}
		}
		
		return neighbors;
	}
	
	public Vector<Edge> getEdgesFrom(Vertex home){
		Vector<Edge> v = new Vector<Edge>(this.mapping.get(home).values());
		return v;
	}
	
	public Vector<Vertex> getPredecessorFrom(Vertex home) {
		Vector<Vertex> predecessor = new Vector<Vertex>();
		HashMap<Vertex, Edge> m = this.mapping.get(home);
		
		for(Edge e : m.values()) {
			if(e.getTarget() == home) {
				predecessor.add(e.getSource());
			}
		}
		
		return predecessor;
	}
	
	public Vector<Vertex> getSuccessorFrom(Vertex home) {
		Vector<Vertex> successor = new Vector<Vertex>();
		HashMap<Vertex, Edge> m = this.mapping.get(home);
		
		for(Edge e : m.values()) {
			if(e.getSource() == home) {
				successor.add(e.getTarget());
			}
		}
		
		return successor;
	}
	
	public boolean equals(Graph graph) {
		if(this.vertices.size() != graph.getNumberOfVertices()) {
			return false;
		}
		
		if(this.edges.size() != graph.getNumberOfEdges()) {
			return false;
		}
		
		// compareTo all edges
		for(Edge e : edges) {
			if(!graph.containsEdge(e.getSource().getId(), e.getTarget().getId())) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Compare two graph 
	 * 
	 * @param graph
	 * @return true, if the vertices-set are the same
	 */
	public boolean equalsIgnoreEdges(Graph graph) {
		if(this.vertices.size() != graph.getNumberOfVertices()) {
			return false;
		}
		
		for(Vertex v : graph.getAllVertices()) {
			if(!this.vertices.containsValue(v)) {
				return false;
			}
		}
		
		return true;
	}
	
	public int getNumberOfObjectNodes() {
		int count = 0;
		for(Vertex v : this.vertices.values()) {
			if(v.getObjectId() >= 0) {
				count++;
			}
		}
		return count;
	}
	
	public Vector<Edge> getAllEdges() {
		return new Vector<Edge>(this.edges);
	}

	public Vector<Vertex> getAllVertices() {
		return new Vector<Vertex>(this.vertices.values());
	}

	public int getNumberOfVertices() {
		return vertices.size();
	}
	
	public int getNumberOfEdges() {
		return edges.size();
	}

	/**
	 * Set random weight for all edges between 0 and upperLimit
	 * 
	 * @param upperLimit
	 */
	public void setAllWeightWithLimit(int upperLimit) {
		Random randomer = new Random(System.currentTimeMillis());
		for(Edge e : edges) {
			int w = randomer.nextInt(upperLimit + 1); // inclusive this number
			while(w == 0) {
				w = randomer.nextInt(upperLimit + 1);  // no to zero
			}
			e.setWeight(w);
		}
	}

	/**
	 * Set for all edge the weight specified in parameter
	 * 
	 * @param weight : all edges in the graph will have this weight
	 */
	public void setAllWeight(double weight) {
		for(Edge e : edges) {
			e.setWeight(weight);
		}
	}
	
	/**
	 * Generate a new object id
	 * 
	 * @param fromValue : search for available id starting from this value
	 * @return a new object id
	 */
	public int generateObjectId(int fromValue) {
		int id = ++fromValue;
		while(this.containObject(id)) {
			id++;
		}
		return id;
	}
	
	public boolean containObject(int objectId) {
		for(Vertex v : this.getAllVertices()) {
			if(v.getObjectId() == objectId) {
				return true;
			}
		}
		return false;
	}
}

