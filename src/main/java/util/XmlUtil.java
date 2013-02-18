package util;

import graph.ProbabilisticGraph;
import graph.core.Edge;
import graph.core.Vertex;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtil {
	private static final String ROOT     = "ProbabilisticGraph";
	private static final String NODES    = "Nodes";
	private static final String NODE     = "Node";
	private static final String NODE_ID  = "Id";
	private static final String OBJECT_ID= "ObjectId";
	private static final String NODE_X   = "XPos";
	private static final String NODE_Y   = "YPos";
	private static final String EDGES    = "Edges";
	private static final String EDGE     = "Edge";
	private static final String EDGE_SOURCE = "Source";
	private static final String EDGE_TARGET = "Target";
	private static final String EDGE_WEIGHT = "Weight";
	private static final String EDGE_PROB   = "Probability";

	public static void importGraphToXml(ProbabilisticGraph graph, String saveLocation) {
		try {
			System.out.println("Saving graph... (Location: " + saveLocation + ")");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root
			Document doc = docBuilder.newDocument();
			Element root = doc.createElement(ROOT);
			doc.appendChild(root);
			
			Element nodes = doc.createElement(NODES);
			Vector<Vertex> vertices = graph.getAllVertices();
			for(Vertex v : vertices) {
				Element node  = doc.createElement(NODE);
				Element id    = doc.createElement(NODE_ID);
				Element oId   = doc.createElement(OBJECT_ID);
				Element xPos  = doc.createElement(NODE_X);
				Element yPos  = doc.createElement(NODE_Y);
				
				id.appendChild(doc.createTextNode(Integer.toString(v.getId())));
				oId.appendChild(doc.createTextNode(Integer.toString(v.getObjectId())));
				xPos.appendChild(doc.createTextNode(Integer.toString(v.getNodeLocation().x)));
				yPos.appendChild(doc.createTextNode(Integer.toString(v.getNodeLocation().y)));
				
				node.appendChild(id);
				node.appendChild(oId);
				node.appendChild(xPos);
				node.appendChild(yPos);
				nodes.appendChild(node);
			}
			root.appendChild(nodes);
			
			Element edges = doc.createElement(EDGES);
			Vector<Edge> gEdges = graph.getAllEdges();
			for(Edge e : gEdges) {
				Element edge = doc.createElement(EDGE);
				Element source = doc.createElement(EDGE_SOURCE);
				Element target = doc.createElement(EDGE_TARGET);
				Element weight = doc.createElement(EDGE_WEIGHT);
				Element prob   = doc.createElement(EDGE_PROB);
				
				source.appendChild(doc.createTextNode((Integer.toString(e.getSource().getId()))));
				target.appendChild(doc.createTextNode((Integer.toString(e.getTarget().getId()))));
				weight.appendChild(doc.createTextNode((Double.toString(e.getWeight()))));
				prob.appendChild(doc.createTextNode(Double.toString(graph.getProbability(e))));
				
				edge.appendChild(source);
				edge.appendChild(target);
				edge.appendChild(weight);
				edge.appendChild(prob);
				edges.appendChild(edge);
			}
			root.appendChild(edges);
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transfomer = tFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(saveLocation));
			
			// output for testing
			transfomer.transform(source, result);
			
			System.out.println("Saving graph... DONE!");
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(TransformerConfigurationException e) {
			e.printStackTrace();
		} catch(TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static void importGraphFromXml(String fileName) {
		ProbabilisticGraph.getInstance().clear();
		System.out.println("Reading graph from XML...");
		try {
			File file = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
			NodeList nodeList = doc.getElementsByTagName(NODE);
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					// read values
					Vertex v = new Vertex(Integer.parseInt(getTagValue(NODE_ID, e)));
					int oId = Integer.parseInt(getTagValue(OBJECT_ID, e));
					int x = Integer.parseInt(getTagValue(NODE_X, e));
					int y = Integer.parseInt(getTagValue(NODE_Y, e));
					v.setNodeLocation(x, y);
					v.setObjectId(oId);
					ProbabilisticGraph.getInstance().addVertex(v);
				}
			}
			
			NodeList edges = doc.getElementsByTagName(EDGE);
			for(int i = 0; i < edges.getLength(); i++) {
				Node nEdge = edges.item(i);
				if(nEdge.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) nEdge;
					Vertex source = ProbabilisticGraph.getInstance().getVertex(Integer.parseInt(getTagValue(EDGE_SOURCE, e)));
					Vertex target = ProbabilisticGraph.getInstance().getVertex(Integer.parseInt(getTagValue(EDGE_TARGET, e)));
					Edge edge = new Edge(source, target);
					double weight = Double.parseDouble(getTagValue(EDGE_WEIGHT, e));
					double prob = Double.parseDouble(getTagValue(EDGE_PROB, e));
					edge.setWeight(weight);
					ProbabilisticGraph.getInstance().addEdge(edge);
					ProbabilisticGraph.getInstance().setProbability(edge, prob);
				}
			}
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		System.out.println("Reading graph from XML... DONE!");
	}
	
	private static String getTagValue(String tag, Element element) {
		NodeList nList = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node nValue = (Node) nList.item(0);
		return nValue.getNodeValue();
	}
}
