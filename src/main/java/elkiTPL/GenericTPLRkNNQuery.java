package elkiTPL;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.HyperBoundingBox;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;
import de.lmu.ifi.dbs.elki.data.spatial.SpatialUtil;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.distance.DistanceDBIDList;
import de.lmu.ifi.dbs.elki.database.ids.generic.GenericDistanceDBIDList;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;
import de.lmu.ifi.dbs.elki.index.tree.AbstractNode;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialDirectoryEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialIndexTree;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialNode;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialPointLeafEntry;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2013
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


public class GenericTPLRkNNQuery<N extends SpatialNode<N,E>, E extends SpatialEntry, O extends DoubleVector, D extends Distance<D>> extends TPLRkNNQuery<O, D> {

  private SpatialIndexTree<N,E> tree;
  
  private EuclideanDistanceFunction dist = EuclideanDistanceFunction.STATIC;
  
  private int min_card;
  
  private boolean withClipping;
  
  
  @SuppressWarnings("unchecked")
  public GenericTPLRkNNQuery(SpatialIndexTree<N,E> tree, DistanceQuery<O,D> distancequery, boolean withClipping){
    super(distancequery);
    this.tree = tree;
    this.withClipping = withClipping;
    min_card = (int) (0.4 * ((AbstractNode<SpatialEntry>) tree.getRoot()).getCapacity());
  }
  
  
  private PriorityQueue<SimpleEntry<Double, TPLEntry>> initializeAPL(){
    PriorityQueue<SimpleEntry<Double, TPLEntry>> apl = new PriorityQueue<SimpleEntry<Double, TPLEntry>>(50, new Comparator<SimpleEntry<Double, TPLEntry>>() {
      public int compare(SimpleEntry<Double, TPLEntry> o1, SimpleEntry<Double, TPLEntry> o2) {
        return Double.compare(o1.getKey(), o2.getKey());
      }
    });
    
    apl.add(new SimpleEntry<Double, TPLEntry>(0.0, new TPLEntry(tree.getRootEntry(), 0)));
    
    return apl;
  }
  
  
  private ArrayList<SpatialPointLeafEntry> initializeCandidateSet(){
    ArrayList<SpatialPointLeafEntry> candidateSet = new ArrayList<SpatialPointLeafEntry>();
    return candidateSet;
  }
  
  
  private ArrayList<TPLEntry> initializeRefinementSet(){
    ArrayList<TPLEntry> refinementSet = new ArrayList<TPLEntry>();
    return refinementSet;
  }
  
  
  @SuppressWarnings("unchecked")
  private ArrayList<ArrayList<?>> filter(O q, int k){
    ArrayList<ArrayList<?>> res = new ArrayList<ArrayList<?>>();
    
    // initialize APL and insert (R-tree root, 0) to APL
    PriorityQueue<SimpleEntry<Double, TPLEntry>> apl = initializeAPL();
    
    // initialize CandidateSet
    ArrayList<SpatialPointLeafEntry> candidateSet = initializeCandidateSet();
    
    // initialize RefinementSet
    ArrayList<TPLEntry> refinementSet = initializeRefinementSet();
    
    double val = 0.0;
    
    // while APL is not empty
    while(!apl.isEmpty()) {
      // entry=(se,key)=de-heap APL
      SimpleEntry<Double, TPLEntry> entry = apl.poll();
      E se = (E) entry.getValue().getEntry();
      if (se.isLeafEntry()){
        if (Arrays.equals(((SpatialPointLeafEntry) se).getValues(), q.getValues())){
          continue;
        }
      }

      if (withClipping){
        val = k_trim(q, k, candidateSet, se);
      } else {
        val = prune(q, k, candidateSet, se);
      }
      
      // if (trim(q, candidateSet, se) = infinite
      if ( val == Double.POSITIVE_INFINITY) {
        // then candidateSet = candidateSet + {se}
        refinementSet.add(entry.getValue());
      } else { // entry may be or contain a candidate
        // if se is data point
        if(se.isLeafEntry()) {
          // then candidateSet = candidateSet + {se}
          candidateSet.add((SpatialPointLeafEntry) se);
        } else { // else 
          N node = tree.getNode(se);
          // if se points to a leaf node node
          if (node.isLeaf()){
            // for each point se2 in node ( sorted on dist(se2,q) )
            
            for(SpatialEntry se2 : ((AbstractNode<SpatialEntry>) node).getEntries()) {
              double distance = 0.0;
              if (withClipping)
                distance = k_trim(q, k, candidateSet, se2);
              else
                distance = prune(q, k, candidateSet, se2);

              // if (trim(q, candidateSet, se2) != infinite)
              if(distance != Double.POSITIVE_INFINITY){
                // then insert (se2, dist(p,q)) in APL
                apl.add(new SimpleEntry<Double, TPLEntry>(distance, new TPLEntry(se2, entry.getValue().getDepth()+1)));
              } else {
                // refinementSet = refinementSet + {se2}
                refinementSet.add(new TPLEntry(se2, entry.getValue().getDepth()+1));
              }
            }
          } else { // else if se points to an intermediate node
            // for each entry N_i in node
            for (SpatialEntry N_i : ((AbstractNode<SpatialEntry>) node).getEntries()) {
              // mindist(Nres_i, q) = trim(q, candidateSet, N_i)
              double distance = 0.0;
              if (withClipping)
                distance = k_trim(q, k, candidateSet, N_i);
              else
                distance = prune(q, k, candidateSet, N_i);
              // if mindist(Nres_i, q) = infinite
              if (distance == Double.POSITIVE_INFINITY){
                // refinementSet = refinementSet + {N_i}
                entry.getValue().addToDepth(1);
                refinementSet.add(new TPLEntry(N_i, entry.getValue().getDepth()));
              } else {
                // else insert (N_i, mindist(Nres_i, q)) in APL
                entry.getValue().addToDepth(1);
                apl.add(new SimpleEntry<Double, TPLEntry>(distance, new TPLEntry(N_i, entry.getValue().getDepth())));
              }
            }
          }
        }
      }
    }
    
    res.add(candidateSet);
    res.add(refinementSet);
    
    return res;
  }
  
  
  
  @SuppressWarnings("unchecked")
  private DistanceDBIDList<D> refine(O q, int k, ArrayList<SpatialPointLeafEntry> candidateSet, ArrayList<TPLEntry> refinementSet){
    
    ArrayList<SpatialPointLeafEntry> refinementSetPoints = new ArrayList<SpatialPointLeafEntry>();
    ArrayList<TPLEntry> refinementSetNodes = new ArrayList<TPLEntry>();
    
    // split refinementSet in refinementSetPoints and refinementSetNodes
    for(TPLEntry entry : refinementSet) {
      if(entry.getEntry().isLeafEntry()) {
        refinementSetPoints.add((SpatialPointLeafEntry) entry.getEntry());
      }
      else {
        refinementSetNodes.add(entry);
      }
    }
    
    HashMap<DBID, HashMap<Integer, TPLEntry>> toVisits = new HashMap<DBID, HashMap<Integer, TPLEntry>>();
    HashMap<DBID, Integer> count = new HashMap<DBID, Integer>();
    ArrayList<SpatialPointLeafEntry> refinedCandidateSet = new ArrayList<SpatialPointLeafEntry>();
    refinedCandidateSet = (ArrayList<SpatialPointLeafEntry>) candidateSet.clone();
    int removed = 0;

    
    
    // for each point p in candidateSet
    nextCandidate: for(int i = 0; i < candidateSet.size(); i++) {
      SpatialPointLeafEntry p = candidateSet.get(i);
      int counter = k; 
      
      // for each other point p2 in candidateSet
      for(int j = 0; j < candidateSet.size(); j++) {
        SpatialPointLeafEntry p2 = candidateSet.get(j);
        if (p != p2){
          // if dist(p,p2)<dist(p,q)
          if (dist.doubleMinDist(p, p2) < dist.doubleMinDist(p, q)){
            counter--;
            if (counter == 0){ // TODO: Better use counter <= 0
              // candidateSet = candidateSet - {p}
              refinedCandidateSet.remove(i-removed);
              removed++;
              // goto nextCandidate
              continue nextCandidate;
            }
          }
        }
      }
      
      count.put(p.getDBID(), counter);
      // if p is not eliminated initialize toVisit(p)={}
      toVisits.put(p.getDBID(), new HashMap<Integer, TPLEntry>());
    }
    
    
    GenericDistanceDBIDList<D> result = new GenericDistanceDBIDList<D>();
    
    // repeat
    while(true){
      k_refinement_round(q, k, refinedCandidateSet, refinementSetPoints, refinementSetNodes, count, toVisits, result);
      
      // if candidateSet = {} return
      if(refinedCandidateSet.isEmpty()) {
        break;
      }
      
      // refinementSetPoints = refinementSetNodes = {} -- initialization of next round
      refinementSetNodes.clear();
      refinementSetPoints.clear();
      
      // Let bestNode be the lowest level node that appears in the largest number of sets toVisit(p)
      TPLEntry bestNode = getLowestLevelNodeAppearingMostOften(toVisits);
      
      // remove bestNode from all toVisit(p)
      for(HashMap<Integer, TPLEntry> toVisit : toVisits.values()) {
         toVisit.remove(((SpatialDirectoryEntry) bestNode.getEntry()).getEntryID());
      }
      
      // and access bestNode
      N node = tree.getNode((E) bestNode.getEntry());
      
      for(SpatialEntry e : ((AbstractNode<SpatialEntry>) node).getEntries()) {
        // if bestNode is leaf node
        if(e.isLeafEntry()) {
          // refinementSetPoints = {e|e in bestNode} -- refinementSetPoints contains only the points of bestNode
          refinementSetPoints.add((SpatialPointLeafEntry) e);
        }
        // if bestNode is an intermediate node
        else {
          // refinementSetNodes = {N|N in bestNode} -- refinementSetNodes contains the child nodes of bestNode
          refinementSetNodes.add(new TPLEntry(e, bestNode.getDepth()));
        }
      }
    }

    return result;
    
  }
  
  
  
  private void k_refinement_round(O q, int k, ArrayList<SpatialPointLeafEntry> candidateSet,
      ArrayList<SpatialPointLeafEntry> refinementSetPoints, ArrayList<TPLEntry> refinementSetNodes,
      HashMap<DBID, Integer> count, HashMap<DBID, HashMap<Integer, TPLEntry>> toVisits, 
      GenericDistanceDBIDList<D> result) {
    
    // for each point p in candidateSet
    nextCandidate: for (int i = 0; i < candidateSet.size(); i++){
      SpatialPointLeafEntry p = candidateSet.get(i);
      int counter = count.get(p.getDBID());
      
      // for each point p2 in refinementSetPoints
      for (SpatialPointLeafEntry p2 : refinementSetPoints){
        // if dist(p,p2)<dist(p,q)
        if (dist.doubleMinDist(p, p2) < dist.doubleMinDist(p, q)){
          // counter(p)--
          counter--;
          // if counter(p) = 0
          if (counter == 0){  // TODO: Better use counter <= 0
            // candidateSet = candidateSet - {p} -- false hit
            candidateSet.remove(i);
            i--;
            // goto nextCandidate
            continue nextCandidate;
          }
        }
      }
      count.put(p.getDBID(), counter);
      
      // for each node MBR entry in refinementSetNodes
      for(TPLEntry entry : refinementSetNodes) {
        SpatialEntry N = entry.getEntry();
        // if maxdist(p,entry)<dist(p,q) and min_card(entry)>=counter(p)
        if (DistanceCalc.maxdist(p, N) < dist.doubleMinDist(p, q) && min_card >= counter){
          // candidateSet = candidateSet - {p}
          candidateSet.remove(i);
          i--;
          // goto nextCandidate to test next candidate
          continue nextCandidate;
        }
      }
      
      // for each node MBR entry in refinementSetNodes
      for(TPLEntry entry : refinementSetNodes) {
        SpatialDirectoryEntry N = (SpatialDirectoryEntry) entry.getEntry();
        // if mindist(p,entry)<dist(p,q)
        if (dist.doubleMinDist(p, N) < dist.doubleMinDist(p, q)){
          // add entry in set toVisit(p)
          toVisits.get(p.getDBID()).put(N.getEntryID(), entry);
        }
      }
      
      // if toVisit(p) = {}
      if (toVisits.get(p.getDBID()) != null && toVisits.get(p.getDBID()).size() == 0){
        // candidateSet = candidateSet - {p}
        candidateSet.remove(i);
        i--;
        toVisits.remove(p.getDBID());
        // and report p -- actual result
        DBID dbid = p.getDBID();
        D distance = (D) distanceQuery.distance(dbid, q);
        result.add(distance, dbid);
      }
    }
    
  }
  
  
  
  
  private double prune (NumberVector<?> q, int k, ArrayList<SpatialPointLeafEntry> candidateSet, SpatialEntry entry){
    int pruneCount = 0;

    for(SpatialComparable candidate : candidateSet) {
      if(PruningHeuristic.relationalTest(entry, q, candidate)) {
        pruneCount++;
      }
      if(pruneCount >= k) {
        return Double.POSITIVE_INFINITY;
      }
    }

    return EuclideanDistanceFunction.STATIC.doubleMinDist(q, entry);
  }

  
  
  private double k_trim (DoubleVector q, int k, ArrayList<SpatialPointLeafEntry> candidateSet, SpatialEntry entry){
    SpatialComparable Nres = entry;
    if (candidateSet.size() >= k){
      ArrayList<ArrayList<SpatialPointLeafEntry>> subsets = getSubsets(candidateSet, k);
      
      for (int i = 0; i < subsets.size(); i++){
        ArrayList<SpatialPointLeafEntry> current = subsets.get(i);
        Nres = clipping(q, current, Nres);

        if (Nres == null){
          return Double.POSITIVE_INFINITY;
        }
      }
    }
    return EuclideanDistanceFunction.STATIC.doubleMinDist(q, Nres);
  }
  
  
  private static SpatialComparable clipping(DoubleVector q, ArrayList<SpatialPointLeafEntry> subset, SpatialComparable rect){
    SpatialComparable result = null;
    SpatialPointLeafEntry[] points = new SpatialPointLeafEntry[subset.size()];
    for (int i = 0; i<subset.size(); i++){
      points[i] = subset.get(i);
    }
    double min[] = new double[rect.getDimensionality()], max[] = new double[rect.getDimensionality()]; 
    for(int i = 0; i < subset.size(); i++){
      SpatialComparable sc;
      //1)a) compute midpoint between q and points[i] => X
      //  b) compute normal A of l, pointing to q => A
      double[] x = new double[rect.getDimensionality()];
      double[] a = new double[rect.getDimensionality()];
      double[] z = new double[rect.getDimensionality()];
      double c = 0, d = 0;
      double alength = 0;
      for(int j = 0; j < x.length; j++){
        x[j] = (q.getValue(j)+points[i].getValues()[j])/2.0; 
        a[j] = q.getValue(j)-x[j];
        alength += a[j]*a[j];
      }
      //2)normalize a, and since since A*X=c, compute c =>c
      //use the same loop to compute z and the dot product in d, for performance reasons
      alength = Math.sqrt(alength);
      for(int j = 0; j < a.length; j++){
        a[j] /= alength;
        c += a[j]*x[j];
        z[j] = (a[j] > 0) ? rect.getMax(j) : rect.getMin(j);
        d += a[j]*z[j];
      }
      d = c - d;
      //6) if d > 0 return "No"
      if(d > 0){
        //mbr lies totally outside q's voronoi cell, hence it can be pruned totally
        continue;
      }
      
      //7) for each j:
      for(int j = 0; j < a.length; j++){
        if(a[j] == 0){
          min[j] = rect.getMin(j);
          max[j] = rect.getMax(j);
        } else 
        if(a[j] > 0){
          max[j] = rect.getMax(j);
          min[j] = Math.max(rect.getMin(j), rect.getMax(j)+d/a[j]);
        } else 
          if(a[j] < 0){
          min[j] = rect.getMin(j);
          max[j] = Math.min(rect.getMax(j), rect.getMin(j)+d/a[j]);
          }
      }
      //sc is the part that can not be pruned by a point p, i.e. probably contains a candidate from point of view of point p
      //since we have only k points, each part that can not be pruned by one point, can not be pruned during this iteration at all
      //hence, we can compute the mbr of all these non-pruned parts and not prune this.
      sc = new HyperBoundingBox(min,max); 
      result = (result == null) ? sc : SpatialUtil.union(result, sc);
    }
    
    return result;
  }
  
  
  
  private void getSubsets (ArrayList<SpatialPointLeafEntry> candidateSet, int k, int id, ArrayList<SpatialPointLeafEntry> current, ArrayList<ArrayList<SpatialPointLeafEntry>> res){
    
    if (current.size() == k){
      res.add(new ArrayList<>(current));
      return;
    } else {
      if (id == candidateSet.size()) return;
      SpatialPointLeafEntry x = candidateSet.get(id);
      current.add(x);
      //"guess" x is in the subset
      getSubsets(candidateSet, k, id+1, current, res);
      current.remove(x);
      //"guess" x is not in the subset
      getSubsets(candidateSet, k, id+1, current, res);
    }
    
    return;
  }
  
  private ArrayList<ArrayList<SpatialPointLeafEntry>> getSubsets (ArrayList<SpatialPointLeafEntry> candidateSet, int k){
    ArrayList<ArrayList<SpatialPointLeafEntry>> res = new ArrayList<ArrayList<SpatialPointLeafEntry>>();
    getSubsets(candidateSet, k, 0, new ArrayList<SpatialPointLeafEntry>(), res);
    return res;
  }
  
  
  private TPLEntry getLowestLevelNodeAppearingMostOften(HashMap<DBID, HashMap<Integer, TPLEntry>> toVisits) {
    int lowestLevel = 0;
    ArrayList<TPLEntry> lowestLevelNodes = new ArrayList<TPLEntry>();
    
    for(HashMap<Integer, TPLEntry> entriesToVisit : toVisits.values()) {
      if (entriesToVisit != null){
        for(TPLEntry entry : entriesToVisit.values()) {
        
          if(entry.getDepth() == lowestLevel) {
            lowestLevelNodes.add(entry);
          } 
          else if (entry.getDepth() > lowestLevel){
            lowestLevelNodes.clear();
            lowestLevelNodes.add(entry);
            lowestLevel = entry.getDepth();
          }
        }
      }
    }
    
    TPLEntry bestNode = null;
    int NumberOfBest = 0;
    
    if(lowestLevelNodes.size() == 1) {
      bestNode = lowestLevelNodes.get(0);
    }
    else {
      for(TPLEntry entry : lowestLevelNodes) {
        int count = 0;
        for(HashMap<Integer, TPLEntry> toVisit : toVisits.values()) {
          if(toVisit.containsKey(((SpatialDirectoryEntry) entry.getEntry()).getEntryID())) {
            count++;
          }
        }

        if(count > NumberOfBest) {
          NumberOfBest = count;
          bestNode = entry;
        }
      }
    }
    
    
    return bestNode;
  }
  
  
  

  @SuppressWarnings("unchecked")
  @Override
  public DistanceDBIDList<D> getRKNNForObject(O q, int k) {
    // TODO Auto-generated method stub

    System.out.println("  Performing filter step...");
    long t0 = System.currentTimeMillis();

    ArrayList<ArrayList<?>> filtered = filter(q, k);

    long t1 = System.currentTimeMillis();
    System.out.println("  Filter step done in " + (t1-t0) + " ms.");
    
    ArrayList<SpatialPointLeafEntry> candidateSet = new ArrayList<SpatialPointLeafEntry>();
    ArrayList<TPLEntry> refinementSet = new ArrayList<TPLEntry>();
    
    candidateSet = (ArrayList<SpatialPointLeafEntry>) filtered.get(0);
    refinementSet = (ArrayList<TPLEntry>) filtered.get(1);
    
    
    return refine(q, k, candidateSet, refinementSet);
  }


  @Override
  public List<? extends DistanceDBIDList<D>> getRKNNForBulkDBIDs(ArrayDBIDs ids, int k) {
    // TODO Auto-generated method stub
    List<DistanceDBIDList<D>> result = new ArrayList<DistanceDBIDList<D>>();
    
    for (DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
      result.add(getRKNNForDBID(DBIDUtil.deref(iter), k));
    }
    
    return result;
  }


  @SuppressWarnings("unchecked")
  @Override
  public DistanceDBIDList<D> getRKNNForDBID(DBIDRef id, int k) {
    // TODO Auto-generated method stub
    ArrayList<ArrayList<?>> filtered = filter(relation.get(id), k);
    
    ArrayList<SpatialPointLeafEntry> candidateSet = new ArrayList<SpatialPointLeafEntry>();
    ArrayList<TPLEntry> refinementSet = new ArrayList<TPLEntry>();
    
    candidateSet = (ArrayList<SpatialPointLeafEntry>) filtered.get(0);
    refinementSet = (ArrayList<TPLEntry>) filtered.get(1);
    
    return refine(relation.get(id), k, candidateSet, refinementSet);
  }
  
}
