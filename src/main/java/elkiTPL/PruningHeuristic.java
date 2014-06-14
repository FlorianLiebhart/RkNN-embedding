package elkiTPL;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.HyperBoundingBox;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialPointLeafEntry;

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

  In EmbeddedTPLQuery.prune called with:  relationalTest(entry,       // which is tried to be pruned using the candidate)
                                                         q,
                                                         candidate)

  If dist(mbr,   maxD) < dist(mbr,   minD) ==> true
  If dist(entry, cnd)  < dist(entry, q)    ==> true  (-> entry pruned)
               _
          |   |_| entry (mbr)
          |
     q    |   p (cnd)
   (minD) |  (maxD)
          |
   */ // todo: the way i use it for my algorithm, it probably can be simplified a lot by calling my new maxDistVMMaximumsNorm function
  public static boolean relationalTestEmbedding(SpatialComparable mbr,
                                                SpatialComparable minDistMbr,
                                                SpatialComparable maxDistMbr){

    double qTotalMindist = 0;
    double cndTotalMaxdist = 0;

		for(int d = 0; d < mbr.getDimensionality(); d++){
			double qd1 = 0;
      double qd2 = 0;
      double pd1 = 0;
      double pd2 = 0;
			
			// MinDist of q to Entry
			if(mbr.getMin(d) > minDistMbr.getMax(d))
        qd1 = mbr.getMin(d) - minDistMbr.getMax(d);
			else if(mbr.getMin(d) < minDistMbr.getMin(d))
        qd1 = minDistMbr.getMin(d) - mbr.getMin(d);

			if(mbr.getMax(d) > minDistMbr.getMax(d))
        qd2 = mbr.getMax(d) - minDistMbr.getMax(d);
			else if(mbr.getMax(d) < minDistMbr.getMin(d))
        qd2 = minDistMbr.getMin(d) - mbr.getMax(d);

      double qMindist = Math.min(qd1, qd2);
      if(qMindist > qTotalMindist)
        qTotalMindist = qMindist;
			
			// MaxDist of pruner (cnd) to Entry
			pd1 = Math.max(Math.abs(mbr.getMin(d) - maxDistMbr.getMin(d)),
                     Math.abs(mbr.getMin(d) - maxDistMbr.getMax(d)));

			pd2 = Math.max(Math.abs(mbr.getMax(d) - maxDistMbr.getMin(d)),
                     Math.abs(mbr.getMax(d) - maxDistMbr.getMax(d)));

      double cndMaxdist = Math.max(pd1, pd2);
      if(cndMaxdist > cndTotalMaxdist)
        cndTotalMaxdist = cndMaxdist;
		}
		distanceCalculations+=1;
    return cndTotalMaxdist - qTotalMindist < 0;

//    for(boolean b : maxMinusMin){
//      if(!b)
//        return false;
//    }
//		return true;
	}

  /**
   * Gets the minimum maxDist between a vector v and a node mbr, using the minimum-norm (l-infinity-norm)
   * @param v Vector
   * @param mbr Node
   * @return
   */
  public static double vmMaxDistMinimumNorm(NumberVector<?> v, SpatialComparable mbr) {
    assert(v.getDimensionality() == mbr.getDimensionality()); // todo: (should never happen for my alg!) so check, and remove later or replace by minDim:
    //    final int dim1 = v.getDimensionality(), dim2 = mbr.getDimensionality();
    //    final int mindim = (dim1 < dim2) ? dim1 : dim2;
    double maxDistanceMinNorm = 0;

    for (int d = 0; d < v.getDimensionality(); d++) {
      final double vValue = v.doubleValue(d);
      final double mbrMax = mbr.getMax(d);
      double delta        = mbrMax - vValue;

      if (delta < 0.) {
        delta = vValue - mbr.getMin(d);
      }
      if (delta > maxDistanceMinNorm) {
        maxDistanceMinNorm = delta;
      }
    }
    return maxDistanceMinNorm;
  }

  /**
   * Gets the maximum minDist between a vector v and a node mbr, using the maximum-norm (l-infinity-norm)
   * @param v Vector
   * @param mbr Node
   * @return
   */
  public static double vmMinDistanceMaximumNorm(NumberVector<?> v, SpatialComparable mbr) {
    assert(v.getDimensionality() == mbr.getDimensionality()); // todo: (should never happen for my alg!) so check, and remove later or replace by minDim

    double minDistanceMaxNorm = 0;

    for (int d = 0; d < v.getDimensionality(); d++) {
      final double vValue = v.doubleValue(d);
      final double mbrMin = mbr.getMin(d);
      double delta        = mbrMin - vValue;

      if (delta < 0.) {
        delta = vValue - mbr.getMax(d);
      }
      if (delta > minDistanceMaxNorm) {
        minDistanceMaxNorm = delta;
      }
    }
    return minDistanceMaxNorm;
  }

  /**
   * Calculates the minimum maxDist between two vectors using the minimum-norm
   * (sum the values of v1 and v2 in each dimensionality, and take the minimum of these sums)
   * @param v1
   * @param v2
   * @return
   */
  public static double vvMaxDistanceMinimumNorm(NumberVector<?> v1, NumberVector<?> v2){
    assert(v1.getDimensionality() == v2.getDimensionality()); // todo: (should never happen for my alg!) so check, and remove later or replace by minDim

    double maxDistanceMinNorm = Double.POSITIVE_INFINITY;

    for (int d = 0; d < v1.getDimensionality(); d++) {
      final double v1Value = v1.doubleValue(d);
      final double v2Value = v2.doubleValue(d);
      double vSum          = v2Value + v1Value;

      if (vSum < maxDistanceMinNorm)
        maxDistanceMinNorm = vSum;
    }

    return maxDistanceMinNorm;
  }

  /**
   * Calculates the maximum minDist between two vectors using the maximum-norm
   * (subtracts the values of v1 and v2 in each dimensionality, and take the maximum of these differences)
   * @param v1
   * @param v2
   * @return
   */
  public static double vvMinDistanceMaximumNorm(NumberVector<?> v1, NumberVector<?> v2){
    assert(v1.getDimensionality() == v2.getDimensionality()); // todo: (should never happen for my alg!) so check, and remove later or replace by minDim

    double minDistanceMaxNorm = 0;

    for (int d = 0; d < v1.getDimensionality(); d++) {
      final double v1Value = v1.doubleValue(d);
      final double v2Value = v2.doubleValue(d);
      double vDiff         = Math.abs(v2Value - v1Value);

      if (vDiff > minDistanceMaxNorm)
        minDistanceMaxNorm = vDiff;
    }

    return minDistanceMaxNorm;
  }

  /**
     * Tests whether maxDistMbr is closer to mbr than minDistMbr (in linear time).
     * If test is true => maxDistMbr is closer to mbr than minDistMbr.
     * @param mbr the MBR of the inner set which is tested
     * @param minDistMbr the MBR of the minDistMbr
     * @param maxDistMbr the MBR of a page
     * @return false if there exists a point of mbr which has a lower mindist to minDistMbr than maxdist to maxDistMbr

    In TPLQuery.prune called with:  relationalTest(entry,       // which is tried to be pruned using the candidate)
                                                   q,
                                                   candidate)

    If dist(mbr,   maxD) < dist(mbr,   minD) ==> true
    If dist(entry, cnd)  < dist(entry, q)    ==> true  (-> entry pruned)
                 _
            |   |_| entry (mbr)
            |
       q    |   p (cnd)
     (minD) |  (maxD)
            |
   */
  public static boolean relationalTest(SpatialComparable mbr,
                                       SpatialComparable minDistMbr,
                                       SpatialComparable maxDistMbr){
		double maxMinusMin = 0.0;
		for(int d = 0; d < mbr.getDimensionality(); d++){
			double qd1 = 0;
      double qd2 = 0;
      double pd1 = 0;
      double pd2 = 0;

			// MinDist of q to Entry
			if(mbr.getMin(d) > minDistMbr.getMax(d))
        qd1 = mbr.getMin(d) - minDistMbr.getMax(d);
			else if(mbr.getMin(d) < minDistMbr.getMin(d))
        qd1 = minDistMbr.getMin(d) - mbr.getMin(d);


			if(mbr.getMax(d) > minDistMbr.getMax(d))
        qd2 = mbr.getMax(d) - minDistMbr.getMax(d);
			else if(mbr.getMax(d) < minDistMbr.getMin(d))
        qd2 = minDistMbr.getMin(d) - mbr.getMax(d);

			// MaxDist of pruner (cnd) to Entry
			pd1 = Math.max(Math.abs(mbr.getMin(d) - maxDistMbr.getMin(d)),
                     Math.abs(mbr.getMin(d) - maxDistMbr.getMax(d)));

			pd2 = Math.max(Math.abs(mbr.getMax(d) - maxDistMbr.getMin(d)),
                     Math.abs(mbr.getMax(d) - maxDistMbr.getMax(d)));

			maxMinusMin += Math.max(pd1*pd1 - qd1*qd1,
                              pd2*pd2 - qd2*qd2);
		}
		distanceCalculations+=1;
		return maxMinusMin<0;
	}

}
