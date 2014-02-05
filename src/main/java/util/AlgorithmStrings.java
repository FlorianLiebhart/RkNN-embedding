package util;

public class AlgorithmStrings {
	// ALGORITHM IDENTIFIER
	public final static String SHORTEST_PATH   	  = "Shortest Path";
	public final static String REACHABLE_NODES 	  = "Reachable Nodes";
	public final static String KNN             	  = "K-Nearest Neighbours";
	public final static String REVERSE_NN           = "Reverse Nearest Neighbours";
	public final static String RKNN_NAIVE           = "RkNN Naive";
	public final static String RKNN_EAGER           = "RkNN Eager";
	public final static String RKNN_EMBEDDED        = "RkNN Embedded";
	public final static String CONNECT_COMP    	  = "Connected Components";
	public final static String LARGEST_CONNECT_COMP = "Largest Connected Component";
	public final static String DURCHMESSER          = "Diameter";
	
	
	// INFO SHOWN IN INFO-PANEL
	public final static String REACHABLE_NODES_INFO = "Find the number of nodes that can be reached from an initial node.";
	
	public final static String SHORTEST_PATH_INFO   = "Calculate the shortest path between two nodes in a graph" + 
														 " using Dijkstra's Algorithm and Sampling such that the sum of the weights of its" + 
														 " constituent edges is minimized.";
	
	public final static String CONNECTED_COMP_INFO    = "A connected component of an undirected graph is a subgraph " + 
	                                                      "in which any two vertices are connected to each other by paths, " + 
	                                                      "and which is connected to no additional vertices.";
	
	public final static String LARGEST_CONNECTED_COMP_INFO = "Get the largest connected component from this graph.";
	
	public final static String DURCHMESSER_INFO =	 "The diameter of a graph is the maximum eccentricity of any vertex in the graph." + 
													 "\nThat is, it is the greatest distance between any pair of vertices." + 
													 "\nTo find the diameter of a graph, first find the shortest path between each pair of vertices." + 
													 "\nThe greatest length of any of these paths is the diameter of the graph.";
	
	public final static String KNN_INFO = "The k-nearest neighbor algorithm is amongst the simplest of all machine learning algorithms:" + 
											" an object is classified by a majority vote of its neighbors, with the object being assigned to" + 
											" the class most common amongst its k nearest neighbors (k is a positive integer, typically small)." + 
											" If k = 1, then the object is simply assigned to the class of its nearest neighbor.";
	
	public final static String REVERSE_NN_INFO = "For a query q, reverse nearest neighbors retrieve all objects which has q as their nearest neigbor";
	public final static String RKNN_INFO = "For a query point q, reverse k-nearest neighbors retrieve all objects which have q as their k nearest neigbour";

}
