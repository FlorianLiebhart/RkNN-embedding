package elkiTPL;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.HyperBoundingBox;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialUtil;

public class Misc {
	public static List<double[]> getCorners(SpatialComparable box){
		ArrayList<double[]> corners = new ArrayList<double[]>();
		if(SpatialUtil.perimeter(box) == 0){
			corners.add(SpatialUtil.getMin(box));
			return corners;
		}
		boolean[] current = new boolean[box.getDimensionality()];
		int dim = box.getDimensionality();
		
		for(int i=0;i<Math.pow(2, dim);i++) {
			double[] newCorner = new double[dim];
			for(int j=0;j<dim;j++) {
				if (current[j]) newCorner[j] = box.getMin(j);
				else newCorner[j] = box.getMax(j);
			}
			addOne(current);
			corners.add(newCorner);
		}
		return corners;
	}
	private static void addOne(boolean[] arg) {
		arg[arg.length-1] = !arg[arg.length-1];
		for (int i=arg.length-2;i>=0 && !arg[i+1];i--) {
			if (!arg[i+1]) arg[i] = !arg[i];
		}
	}
	public static void main(String args[]){
		double[] min = {0.0, 1.0, 2.0, 3.0};
		double[] max = {4.0, 5.0, 6.0, 7.5};
		HyperBoundingBox box = new HyperBoundingBox(min, max);
		List<double[]> c = getCorners(box);
	}
}
