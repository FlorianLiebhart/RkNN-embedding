package elkiTPL;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.HyperBoundingBox;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;

public class PruningHeuristic {
	
	public static final int MAXDISTPRUNING = 0, CORNERPRUNING = 1, RENZIDIST = 2;
	public static int distanceCalculations = 0;
	
	/**
	 * Tests whether maxDistMbr is closer to mbr than minDistMbr (in linear time).
	 * If test is true => maxDistMbr is closer to mbr than minDistMbr. 
	 * @param mbr the MBR of the inner set which is tested 
	 * @param minDistMbr the MBR of the minDistMbr
	 * @param maxDistMbr the MBR of a page
	 * @return false if there exists a point of mbr which has a lower mindist to minDistMbr than maxdist to maxDistMbr
	 */
	public static boolean relationalTest(SpatialComparable mbr, SpatialComparable minDistMbr, SpatialComparable maxDistMbr, int pruning){

		if(pruning == CORNERPRUNING){
			distanceCalculations++;
			if(DistanceCalc.mindist(mbr, minDistMbr) <= 0) 
				return false;
			List<double[]> corners = Misc.getCorners(mbr);
			for(double[] corner: corners){
				distanceCalculations+=2;
				double maxdist = DistanceCalc.maxdist(corner, maxDistMbr);
				double mindist = DistanceCalc.mindist(corner, minDistMbr);
				if(maxdist>mindist)
					return false;
			}
			return true;
		}else if(pruning == MAXDISTPRUNING){
		  distanceCalculations+=2;
			if(DistanceCalc.maxdist(mbr, maxDistMbr)<DistanceCalc.mindist(mbr, minDistMbr)){
				return true;
			}
			return false;
		}else if(pruning == RENZIDIST){
			return relationalTest(mbr, minDistMbr, maxDistMbr);
		}
		
		return false;
	}
	public static boolean relationalTest(SpatialComparable mbr, DoubleVector minPoint, DoubleVector maxPoint){
		return relationalTest(mbr, new HyperBoundingBox(minPoint.getValues(), minPoint.getValues()), new HyperBoundingBox(maxPoint.getValues(), maxPoint.getValues()), RENZIDIST);
	}
	/**
   * returns true if one part of the mbr has a smaller maxdist to maxdistmbr and the other part a smaller mindist to mindistmbr
   * (true if mbr gets only partial pruned by maxdistmbr)
   * @return
   */
  public static boolean partialPruned(SpatialComparable mbr, SpatialComparable minDistMbr, SpatialComparable maxDistMbr){
    double maxMinusMin = 0.0;
    for(int d = 0; d < mbr.getDimensionality();d++){
      double qd1 = 0, qd2 = 0, pd1 = 0, pd2 = 0;
      
      //Test if minimum can lie in the middle of interval
      double t = Double.MAX_VALUE;
      double midPoint = (maxDistMbr.getMin(d)+maxDistMbr.getMax(d))/2.0;
      if(mbr.getMin(d)<midPoint && mbr.getMax(d) > midPoint){
        double max = Math.max(Math.abs(mbr.getMin(d)-midPoint), Math.abs(mbr.getMax(d)-midPoint));
        double min = 0;
        if(mbr.getMin(d)>midPoint)min = mbr.getMin(d)-midPoint;
        else if(mbr.getMin(d)<midPoint) min = midPoint - mbr.getMin(d);
        t = max*max -min*min;
      }
      //MinDist of Q
      if(mbr.getMin(d)>minDistMbr.getMax(d))qd1 = mbr.getMin(d)-minDistMbr.getMax(d);
      else if(mbr.getMin(d)<minDistMbr.getMin(d)) qd1 = minDistMbr.getMin(d) - mbr.getMin(d);
      
      if(mbr.getMax(d)>minDistMbr.getMax(d))qd2 = mbr.getMax(d)-minDistMbr.getMax(d);
      else if(mbr.getMax(d)<minDistMbr.getMin(d)) qd2 = minDistMbr.getMin(d) - mbr.getMax(d);
      
      //maxDist of pruner
      pd1 = Math.max(Math.abs(mbr.getMin(d)-maxDistMbr.getMin(d)), Math.abs(mbr.getMin(d)-maxDistMbr.getMax(d)));
      pd2 = Math.max(Math.abs(mbr.getMax(d)-maxDistMbr.getMin(d)), Math.abs(mbr.getMax(d)-maxDistMbr.getMax(d)));
      
      maxMinusMin += Math.min(Math.min(pd1*pd1 - qd1*qd1, pd2*pd2 - qd2*qd2),t);
    }
    distanceCalculations+=1;
    return maxMinusMin<0;
  }
  
  public static void resetCalcs(){
	  distanceCalculations = 0;
  }
  /**
	 * Tests whether maxDistMbr is closer to mbr than minDistMbr (in linear time).
	 * If test is true => maxDistMbr is closer to mbr than minDistMbr. 
	 * @param mbr the MBR of the inner set which is tested 
	 * @param minDistMbr the MBR of the minDistMbr
	 * @param maxDistMbr the MBR of a page
	 * @return false if there exists a point of mbr which has a lower mindist to minDistMbr than maxdist to maxDistMbr
	 */
  public static boolean relationalTest(SpatialComparable mbr, SpatialComparable minDistMbr, SpatialComparable maxDistMbr){
		double maxMinusMin = 0.0;
		for(int d = 0; d < mbr.getDimensionality();d++){
			double qd1 = 0, qd2 = 0, pd1 = 0, pd2 = 0;
			
			//MinDist of Q
			if(mbr.getMin(d)>minDistMbr.getMax(d))qd1 = mbr.getMin(d)-minDistMbr.getMax(d);
			else if(mbr.getMin(d)<minDistMbr.getMin(d)) qd1 = minDistMbr.getMin(d) - mbr.getMin(d);
			
			if(mbr.getMax(d)>minDistMbr.getMax(d))qd2 = mbr.getMax(d)-minDistMbr.getMax(d);
			else if(mbr.getMax(d)<minDistMbr.getMin(d)) qd2 = minDistMbr.getMin(d) - mbr.getMax(d);
			
			//maxDist of pruner
			pd1 = Math.max(Math.abs(mbr.getMin(d)-maxDistMbr.getMin(d)), Math.abs(mbr.getMin(d)-maxDistMbr.getMax(d)));
			pd2 = Math.max(Math.abs(mbr.getMax(d)-maxDistMbr.getMin(d)), Math.abs(mbr.getMax(d)-maxDistMbr.getMax(d)));
			
			maxMinusMin += Math.max(pd1*pd1 - qd1*qd1, pd2*pd2 - qd2*qd2);
		}
		distanceCalculations+=1;
		return maxMinusMin<0;
	}
}
