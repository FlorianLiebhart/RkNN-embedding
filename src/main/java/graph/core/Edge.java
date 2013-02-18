package graph.core;

public class Edge {
	private Vertex source;
	private Vertex target;
	private double weight;
	
	public Edge(Vertex source, Vertex target) {
		this.source = source;
		this.target = target;
	}

	public Vertex getSource() {
		return source;
	}

	public void setSource(Vertex source) {
		this.source = source;
	}

	public Vertex getTarget() {
		return target;
	}

	public void setTarget(Vertex target) {
		this.target = target;
	}
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String toString() {
		return "(" + source + " - " + target + ")";
	}
	
	public boolean containsVertex(Vertex vertex) {
		if(vertex == source || vertex == target) {
			return true;
		}
		return false;
	}
}
