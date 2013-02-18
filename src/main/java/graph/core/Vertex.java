package graph.core;

import java.awt.Point;

public class Vertex {
	public static final int NO_OBJECT = -1;
	
	private int id;
	private int objectId;
	private Point pos;
	
	public Vertex(int id) {
		this.id       = id;
		this.objectId = NO_OBJECT;
	}
	
	public int getId() {
		return id;
	}
	
	public void setObjectId(int id) {
		objectId = id;
	}
	
	public int getObjectId() {
		return objectId;
	}

	public String toString() {
		return this.id + "";
	}

	public void setNodeLocation(int x, int y) {
		pos = new Point(x, y);
	}
	
	public Point getNodeLocation() {
		return pos;
	}
	
	public boolean containObject() {
		return objectId != Vertex.NO_OBJECT;
	}
}
