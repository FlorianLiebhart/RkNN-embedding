package graph;

import graph.core.Edge;
import graph.core.Graph;
import graph.core.Vertex;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Vector;

import util.GuiConstants;
import view.GuiUtil;


public class GraphGenerator {
	private boolean weightOne = true;
	
	private static GraphGenerator instance = null;
	
	private GraphGenerator() {
		
	}
	
	public static GraphGenerator getInstance() {
		if(instance == null) {
			instance = new GraphGenerator();
		}
		return instance;
	}
	
	public void generateProbabilisticGraph(int numberOfVertices, int numberOfEdges, int numberOfUncertainEdges, int numberOfObjects) {
        ProbabilisticGraph.getInstance().clear();

        System.out.print("Creating nodes...");
        createNodes(ProbabilisticGraph.getInstance(), numberOfVertices);
        System.out.print(" done.");

        System.out.print(" Creating objects...");
        addRandomObjects(ProbabilisticGraph.getInstance(), numberOfObjects);
        System.out.print(" done.");

        System.out.print(" Setting nodes position...");
        GuiUtil.setNodesPosition(ProbabilisticGraph.getInstance());
        System.out.print(" done.");

        System.out.print(" Creating edges...");
        createEdgesAccordingNodesPosition(ProbabilisticGraph.getInstance(), numberOfEdges);
        System.out.print(" done.");

        addRandomProbabilities(ProbabilisticGraph.getInstance(), numberOfUncertainEdges);

        System.out.print(" Creating edge weights...");
        addWeights(ProbabilisticGraph.getInstance());
        System.out.print(" done.");
	}

    public void generateProbabilisticGraph(int numberOfVertices, int numberOfEdges, int numberOfUncertainEdges, int numberOfObjects, boolean weightOne) {
        this.weightOne = weightOne;
        generateProbabilisticGraph(numberOfVertices, numberOfEdges, numberOfUncertainEdges, numberOfObjects);
    }


        @SuppressWarnings("unchecked")
	private void addRandomObjects(ProbabilisticGraph graph, int numberOfObjects) {
		Vector<Vertex> nodes = (Vector<Vertex>) graph.getAllVertices().clone();
		
		if(numberOfObjects > nodes.size()) {
			throw new RuntimeException("Maximum number of objects: " + nodes.size());
		}
		Random randomer = new Random(System.currentTimeMillis());
		int created = 0;
		int id = 0;
		int nr = 1;
		while(created != numberOfObjects) {
			Vertex v = nodes.get(randomer.nextInt(nodes.size()));
			v.setObjectId(id);
			nodes.remove(v);
			id++;
			created++;
//			System.out.println("Creating Objects... (" + nr++ + " / " + numberOfObjects + ")");
		}
	}

	private void addRandomProbabilities(ProbabilisticGraph target, int numberOfUncertainEdges) {
			if(target.getAllEdges() == null) {
				throw new RuntimeException("Edges have to be created first!");
			}
			
			if(target.getAllEdges().size() < numberOfUncertainEdges) {
				throw new RuntimeException("Maximum number of edges: " + target.getNumberOfEdges());
			}
			
			if(numberOfUncertainEdges < 0) {
				throw new RuntimeException("Number of edges with probabilities < 0");
			}
			
			Random randomer = new Random(System.currentTimeMillis());
			Vector<Edge> modifiedEdges = new Vector<Edge>();
			@SuppressWarnings("unchecked")
			Vector<Edge> candidates = (Vector<Edge>) target.getAllEdges().clone();
			DecimalFormat df = new DecimalFormat("#.##");
			int modified = 1;
			while(modifiedEdges.size() < numberOfUncertainEdges) { 
				int index = randomer.nextInt(candidates.size());
				
				double randomProb = randomer.nextDouble();
				
				String randomPString = df.format(randomProb);
				while(randomPString.matches("0") || randomPString.matches("1")) {
					randomProb = randomer.nextDouble(); // probability can't be zero or one
					randomPString = df.format(randomProb);
				}
				target.setProbability(candidates.get(index), Double.valueOf(randomPString.replaceAll(",", ".")));
				modifiedEdges.add(candidates.get(index));
				candidates.remove(index);
//				System.out.println("Adding probabilities...(" + modified++ + " / " + numberOfUncertainEdges + ")");
			}
//			target.setUncertainEdges(modifiedEdges);
			
			for(Edge e : target.getAllEdges()) {
				if(!modifiedEdges.contains(e)) {
					target.setProbability(e, 1.0); // set rest of the edges with probability = 1.0
				}
			}
	}
	
	private void addWeights(Graph graph) { 
		if(weightOne) {
			graph.setAllWeight(GuiConstants.DEFAULT_NODE_WEIGHT);
		} else {
			graph.setAllWeightWithLimit(GuiConstants.MAX_EDGE_WEIGHT);
		}
	}

	private void createNodes(Graph graph, int numberOfNodes) {
		if(numberOfNodes <= 0) {
			throw new RuntimeException("Number of nodes <= 0!");
		}
		int nr = 1;
		for(int i = 0; i < numberOfNodes; i++) {
			graph.addVertex(new Vertex(i));
//			System.out.println("Creating Nodes... (" + nr++ + " / " + numberOfNodes + ")");
		}
	}

	private void createEdgesAccordingNodesPosition(Graph g, int numberOfEdges) {
			if(g.getAllVertices() == null || g.getAllVertices().size() == 0) {
				throw new RuntimeException("Nodes have to be created first!");
			}
			
			if(numberOfEdges < 0) {
				throw new RuntimeException("Number of edges < 0");
			}

			int vertices = g.getAllVertices().size();
			int max = new Double((new Integer(vertices).doubleValue() * new Integer(vertices - 1).doubleValue()) / 2).intValue();
			if(numberOfEdges > max) {
				throw new RuntimeException("Maximum number of edges: " + max);
			}
			
			Random randomer = new Random(System.currentTimeMillis());
			double radius = GuiConstants.RADIUS;
			Vector<Vertex> nodesInRadius = null;
			int created = 1;
			while(g.getAllEdges().size() < numberOfEdges) {
				
				// new -> vector usedNode
				// vielleicht auch mit "candidates="
				// while usednode.size != all node
				// get random node from graph
				// if node already used : continue
				// else search for neighbours
				
				@SuppressWarnings("unchecked")
				Vector<Vertex> candidates = (Vector<Vertex>) g.getAllVertices().clone();
				while(candidates.size() != 0) {
					int index = randomer.nextInt(candidates.size());
					
					Vertex v = candidates.get(index);
					candidates.remove(index);
					
					nodesInRadius = getAllNodesInRadius(g, v, radius);
					int edgesToCreateHere = randomer.nextInt(nodesInRadius.size() + 1);
					int createdEdges = 0;
					boolean allDone = false;
					while(createdEdges != edgesToCreateHere) { 
						if(nodesInRadius.size() == 0) {
							break; // all local nodes used
						}
						
						if(g.getAllEdges().size() == numberOfEdges) {
							allDone = true;
							break; // number of edges reached
						}
						
						Vertex target = nodesInRadius.get(randomer.nextInt(nodesInRadius.size())); // choose random node
						Edge e = new Edge(v, g.getVertex(target.getId()));
						if(g.containsEdge(e)) {
							nodesInRadius.remove(target); // don't check the same node
							continue;
						} else {
							g.addEdge(e);
							nodesInRadius.remove(target); // don't check the same node
//							System.out.println("Creating edges... (" + created++ + " / " + numberOfEdges + ")");
							createdEdges++;
						}
					}
					
					if(allDone) {
						break;
					}
				}
				
				radius += radius;
			}			
		}

	private Vector<Vertex> getAllNodesInRadius(Graph g, Vertex start, double radius) { // TODO move
		Vector<Vertex> vertices = new Vector<Vertex>();
		
		for(Vertex v : g.getAllVertices()) {
			if(v == start) {
				continue;
			}
			if(start.getNodeLocation().distance(v.getNodeLocation()) <= radius) {
				vertices.add(v);
			}
		}
		
		return vertices;
	}
	
	public void setWeightOne(boolean val) {
		weightOne = val;
	}
}
