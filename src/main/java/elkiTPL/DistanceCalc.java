package elkiTPL;

import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;
import de.lmu.ifi.dbs.elki.index.tree.Entry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialDirectoryEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialPointLeafEntry;

// For Euclidean distances
public class DistanceCalc {
	
	public static long distanceCalculations = 0;

	 public static double mindist(double[] point1, double[] point2) {
	     double sumOfSquares =0;
	     for(int i=0;i<point1.length;i++) {
	         sumOfSquares += (point1[i]-point2[i])*(point1[i]-point2[i]);
	     }
//	      System.out.print("\n Dist: "+sumOfSquares);
	     distanceCalculations++;
	     return (Math.sqrt(sumOfSquares));
	 }

	 public static double mindist(double[] point, SpatialComparable mbr) {      double sumOfSquares =0;
	     for(int i=0;i<point.length;i++) {
	         if (point[i] < mbr.getMin(i)) sumOfSquares += (mbr.getMin(i)-point[i])*(mbr.getMin(i)-point[i]);
	         if (point[i] > mbr.getMax(i)) sumOfSquares += (point[i]-mbr.getMax(i))*(point[i]-mbr.getMax(i));
	     }
	       // if (debug)   System.out.println("BLAH");
	     distanceCalculations++;
	     return (Math.sqrt(sumOfSquares));
	 }
	 public static double mindist(SpatialComparable mbr,double[] point) {
	     return mindist(point,mbr);
	 }

	 public static double mindist(SpatialComparable mbr1, SpatialComparable mbr2) {
	     double sumOfSquares =0;
	     for(int i=0;i<mbr1.getDimensionality();i++) {
	         if (mbr1.getMax(i) < mbr2.getMin(i)) sumOfSquares += (mbr2.getMin(i)-mbr1.getMax(i))*(mbr2.getMin(i)-mbr1.getMax(i));
	         if (mbr1.getMin(i) > mbr2.getMax(i)) sumOfSquares += (mbr2.getMax(i)-mbr1.getMin(i))*(mbr2.getMax(i)-mbr1.getMin(i));
	     }
	     distanceCalculations++;
	     return (Math.sqrt(sumOfSquares));
	 }

	 public static double maxdist(double[] point1, double[] point2) {
	     return mindist(point1,point2);
	 }

	 public static double maxdist(double[] point, SpatialComparable mbr) {
	     double sumOfSquares =0;
	     for(int i=0;i<point.length;i++) {
	         if (point[i] <= mbr.getMin(i)) sumOfSquares += (mbr.getMax(i)-point[i])*(mbr.getMax(i)-point[i]);
	         if (point[i] > mbr.getMax(i)) sumOfSquares += (point[i]-mbr.getMin(i))*(point[i]-mbr.getMin(i));
	         if (point[i] <= mbr.getMax(i) && point[i] > mbr.getMin(i)) {
	             sumOfSquares += Math.max(point[i]-mbr.getMin(i),mbr.getMax(i)-point[i])*Math.max(point[i]-mbr.getMin(i),mbr.getMax(i)-point[i]);
	         }
	     }
	     distanceCalculations++;
	     return (Math.sqrt(sumOfSquares));
	 }
	 public static double maxdist(SpatialComparable mbr,double[] point) {
	     return maxdist(point, mbr);
	 }

	 public static double maxdist(SpatialComparable mbr1, SpatialComparable mbr2) {
	     double sumOfSquares =0;
	     for(int i=0;i<mbr1.getDimensionality();i++) {
	         if (mbr1.getMax(i) < mbr2.getMin(i)) sumOfSquares += (mbr2.getMax(i)-mbr1.getMin(i)) * (mbr2.getMax(i)-mbr1.getMin(i));
	         else if (mbr1.getMin(i) > mbr2.getMax(i)) sumOfSquares += (mbr1.getMax(i)-mbr2.getMin(i)) * (mbr1.getMax(i)-mbr2.getMin(i));
	         else {
	             double localmax = 0;
	             if (Math.abs(mbr1.getMax(i)-mbr2.getMin(i))>localmax) localmax = Math.abs(mbr1.getMax(i)-mbr2.getMin(i));
	             if (Math.abs(mbr2.getMax(i)-mbr1.getMax(i))>localmax) localmax = Math.abs(mbr2.getMax(i)-mbr1.getMax(i));
	             if (Math.abs(mbr2.getMin(i)-mbr1.getMin(i))>localmax) localmax = Math.abs(mbr2.getMin(i)-mbr1.getMin(i));
	             if (Math.abs(mbr2.getMax(i)-mbr1.getMin(i))>localmax) localmax = Math.abs(mbr2.getMax(i)-mbr1.getMin(i));
	             sumOfSquares += localmax * localmax;
	         }
	     }
	     distanceCalculations++;
	     return (Math.sqrt(sumOfSquares));
	 }

	 public static double minmaxdist(double[] point1, double[] point2) {
		 double dist = mindist(point1,point2);
	     if (dist==0) return Double.POSITIVE_INFINITY;
	     return dist;
	 }

	 public static double minmaxdist(double[] point, SpatialComparable mbr) {
	     int dim = mbr.getDimensionality();
	     double minDist = Double.MAX_VALUE;
	           double[] Al= new double[dim];
	       double[] Am= new double[dim];
	       double[] Au= new double[dim];
	       double[] Bl= new double[dim];
	       double[] Bm= new double[dim];
	       double[] Bu= new double[dim];
	       double[] pA= new double[dim];
	       double[] pB= new double[dim];
	           for(int i=0;i<dim;i++) {
	       Al[i] = point[i];
	       Am[i] = point[i];
	       Au[i] = point[i];
	             Bl[i]=mbr.getMin(i);
	       Bu[i]=mbr.getMax(i);
	       Bm[i]=(mbr.getMin(i) + mbr.getMax(i))/2;
	       if (Am[i]<Bm[i]) pB[i]=Bl[i];
	       else pB[i]=Bu[i];
	             if (Am[i]<pB[i]) pA[i]=Al[i];
	       else pA[i]=Au[i];
	     }
	     for(int i=0;i<dim;i++) {
	                       double current = 0.0;
	             current = (pA[i]-pB[i])*(pA[i]-pB[i]);
	       double temp=0.0;
	       for(int j=0;j<dim;j++) {
	           if (i==j) continue;
	           if ((Au[j]-Bl[j])*(Au[j]-Bl[j])>(Al[j]-Bu[j])*(Al[j]-Bu[j])) temp += (Au[j]-Bl[j])*(Au[j]-Bl[j]);
	           else temp += (Al[j]-Bu[j])*(Al[j]-Bu[j]);
	       }
	       current += temp;
	       if (current < minDist) minDist = current;
	     }
	     distanceCalculations++;
	     return Math.sqrt(minDist);
	 }





	 public static double minmaxdist(SpatialComparable mbr1, SpatialComparable mbr2) {   //TODO: Non Exponential (in d) algorithm!
	     int dim = mbr1.getDimensionality();
	     double minDist = Double.MAX_VALUE;
	           double[] Al= new double[dim];
	       double[] Am= new double[dim];
	       double[] Au= new double[dim];
	       double[] Bl= new double[dim];
	       double[] Bm= new double[dim];
	       double[] Bu= new double[dim];
	       double[] pA= new double[dim];
	       double[] pB= new double[dim];
	       for(int i=0;i<dim;i++) {
	               Al[i] = mbr1.getMin(i);
	               Am[i] = mbr1.getMax(i);
	               Au[i] = (mbr1.getMin(i) + mbr1.getMax(i))/2;
	                             Bl[i]=mbr2.getMin(i);
	               Bu[i]=mbr2.getMax(i);
	               Bm[i]=(mbr2.getMin(i) + mbr2.getMax(i))/2;
	                             if (Am[i]<Bm[i]) pB[i]=Bl[i];
	               else pB[i]=Bu[i];
	                             if (Am[i]<pB[i]) pA[i]=Al[i];
	               else pA[i]=Au[i];
	             }
	         for(int i=0;i<dim;i++) {
	             double current = 0.0;
	             current = (pA[i]-pB[i])*(pA[i]-pB[i]);
	       double temp=0.0;
	       for(int j=0;j<dim;j++) {
	           if (i==j) continue;
	           if ((Au[j]-Bl[j])*(Au[j]-Bl[j])>(Al[j]-Bu[j])*(Al[j]-Bu[j])) temp += (Au[j]-Bl[j])*(Au[j]-Bl[j]);
	           else temp += (Al[j]-Bu[j])*(Al[j]-Bu[j]);
	       }
	       current += temp;
	       if (current < minDist) minDist = current;
	     }
	     distanceCalculations++;
	     return Math.sqrt(minDist);

	 }

	 public static double minmaxdist(SpatialComparable mbr,double[] point) {
	     return minmaxdist(point,mbr);
	 }

	 // Redundant when using the SpatialComparable API instead of HyperBoundingBox 
	 public static double mindistE(Entry a,Entry b) {
	     if (a.isLeafEntry()) {
	         if (b.isLeafEntry()) {  // a and b are Leaf Entries
	             return mindist(((SpatialPointLeafEntry)a).getValues(),((SpatialPointLeafEntry)b).getValues());
	         }
	         else  {              // a is Leaf, b is a Directory Entry
	             return mindist(((SpatialPointLeafEntry)a).getValues(),((SpatialDirectoryEntry)b));
	         }
	     }
	     else {                 // a is a Directory Entry
	         if (b.isLeafEntry()) {  // b is Leaf
	             return mindist(((SpatialDirectoryEntry)a),((SpatialPointLeafEntry)b).getValues());
	         }
	         else {              // a and b are Directory Entries
	             return mindist(((SpatialDirectoryEntry)a),((SpatialDirectoryEntry)b));
	         }
	     }          }
	 
	 public static double maxdistE(Entry a,Entry b) {
	     if (a.isLeafEntry()) {
	         if (b.isLeafEntry()) {  // a and b are Leaf Entries
	             return maxdist(((SpatialPointLeafEntry)a).getValues(),((SpatialPointLeafEntry)b).getValues());
	         }
	         else  {              // a is Leaf, b is a Directory Entry
	             return maxdist(((SpatialPointLeafEntry)a).getValues(),((SpatialDirectoryEntry)b));
	         }
	     }
	     else {                 // a is a Directory Entry
	         if (b.isLeafEntry()) {  // b is Leaf
	             return maxdist(((SpatialDirectoryEntry)a),((SpatialPointLeafEntry)b).getValues());
	         }
	         else {              // a and b are Directory Entries
	             return maxdist(((SpatialDirectoryEntry)a),((SpatialDirectoryEntry)b));
	         }
	     }          }
	 public static double minmaxdistE(Entry a,Entry b) {
	     if (a.isLeafEntry()) {
	         if (b.isLeafEntry()) {  // a and b are Leaf Entries
	             return minmaxdist(((SpatialPointLeafEntry)a).getValues(),((SpatialPointLeafEntry)b).getValues());
	         }
	         else  {              // a is Leaf, b is a Directory Entry
	             return minmaxdist(((SpatialPointLeafEntry)a).getValues(),((SpatialDirectoryEntry)b));
	         }
	     }
	     else {                 // a is a Directory Entry
	         if (b.isLeafEntry()) {  // b is Leaf
	             return minmaxdist(((SpatialDirectoryEntry)a),((SpatialPointLeafEntry)b).getValues());
	         }
	         else {              // a and b are Directory Entries
	             return minmaxdist(((SpatialDirectoryEntry)a),((SpatialDirectoryEntry)b));
	         }
	     }          
	  }
	 public static double maxmindistE(Entry a,Entry b){
		 double maxMinDist = 0.0;
		 for(int d = 0; d< ((SpatialEntry)a).getDimensionality();d++){
			 double temp =0.0;
			 double midPointB = (((SpatialEntry)b).getMin(d) + ((SpatialEntry)b).getMax(d))/2.0; 
			 if(((SpatialEntry)a).getMax(d)<midPointB){
				 if(!(((SpatialEntry)a).getMin(d)>((SpatialEntry)b).getMax(d)))
					 temp = ((SpatialEntry)b).getMin(d) - ((SpatialEntry)a).getMin(d);
			 }else if(((SpatialEntry)a).getMin(d)>midPointB){
				 if(!(((SpatialEntry)a).getMax(d)<((SpatialEntry)b).getMax(d)))
					 temp = ((SpatialEntry)a).getMax(d) - ((SpatialEntry)b).getMax(d);
			 }else{
				 temp = Math.max(((SpatialEntry)b).getMin(d) - ((SpatialEntry)a).getMin(d), ((SpatialEntry)a).getMax(d) - ((SpatialEntry)b).getMax(d));
			 }
			maxMinDist += temp*temp;
		 }
		 distanceCalculations++;
		 return maxMinDist;
	 }
	 public double selfminmaxdist(Entry a) {        
		 if (a.isLeafEntry()) return Double.POSITIVE_INFINITY;
		 SpatialDirectoryEntry current = (SpatialDirectoryEntry)a;
	     double dist=0;
	     for(int i=0;i<current.getDimensionality();i++) {
	         double temp =( current.getMax(i)-current.getMin(i));
	//          dist += (current.getMBR().getMax(i)-current.getMBR().getMin(i))*(current.getMBR().getMax(i)-current.getMBR().getMin(i));
	         dist += temp*temp;
	     }
	     distanceCalculations++;
	     return Math.sqrt(dist);
 }
	 @Override
  public String toString(){
		 return ""+distanceCalculations;
	 }
	 
	 public static void resetCalcs(){
		 distanceCalculations = 0;
	 }

}
