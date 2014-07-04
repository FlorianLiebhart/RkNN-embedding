package elkiTPL;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.ids.*;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.MaximumDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.index.tree.AbstractNode;
import de.lmu.ifi.dbs.elki.index.tree.spatial.*;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeNode;

import static elkiTPL.PruningHeuristic.vmMaxDistMinimumNorm;
import static elkiTPL.PruningHeuristic.vmMinDistanceMaximumNorm;

import util.Stats;
import util.CPUTimeDiff;
import util.Log;


public class EmbeddedTPLQuery {

  private final Relation<DoubleVector>                        relation;
  private final SpatialIndexTree<RStarTreeNode, SpatialEntry> tree;

  private final MaximumDistanceFunction                       maxDistFunction = MaximumDistanceFunction.STATIC;
  private final DistanceQuery<DoubleVector, DoubleDistance>   maxDistQuery;

  private final int                                           min_card;


  public EmbeddedTPLQuery(SpatialIndexTree<RStarTreeNode, SpatialEntry> tree, Relation<DoubleVector> relation){
    this.relation     = relation;
    this.maxDistQuery = maxDistFunction.instantiate(relation);
    this.tree         = tree;
    this.min_card     = (int) (0.4 * ((AbstractNode<SpatialEntry>) tree.getRoot()).getCapacity());
  }

  /**
   * @param q
   * @param k
   * @return
   */
  public ArrayList<DBID> filterRefinement(DoubleVector q, int k) {
    Log.append("    - Performing filter step..");
    Log.printFlush();
    CPUTimeDiff timeFilterStep = new CPUTimeDiff();

    ArrayList<ArrayList<?>> filtered = filter(q, k);

    ArrayList<SpatialPointLeafEntry> candidateSet        = (ArrayList<SpatialPointLeafEntry>) filtered.get(0);
    ArrayList<SpatialPointLeafEntry> refinementSetPoints = (ArrayList<SpatialPointLeafEntry>) filtered.get(1);
    ArrayList<TPLEntry>              refinementSetNodes  = (ArrayList<TPLEntry>) filtered.get(2);

    timeFilterStep.end();
    Log.appendln(" done in " + timeFilterStep);
    Stats.setEmbeddingFilteredCandidates(candidateSet.size());
    Log.appendln("    - After filtering: " + candidateSet.size() + " cnds, " + refinementSetPoints.size() + " refPoints, " + refinementSetNodes.size() + " refNodes.");



    // A candidate p is an early result, if for k other candidates p': MaxDist(q, p) <= MinDist(p', q)
    // TODO: This is currently not used, as it does not work correctly! Review! (Returns way too many results, sometimes). Once it works, instead of candidateSet, pass cndSetEarlyResultsRemoved to refine()
    CPUTimeDiff timeEarlyResult = new CPUTimeDiff();

    SpatialPointLeafEntry kthCandidate = candidateSet.get( (k > candidateSet.size()) ? candidateSet.size() - 1 : k-1 );
    double minDistKthCndQ              = PruningHeuristic.vvMinDistanceMaximumNorm(q, kthCandidate);

    ArrayList<SpatialPointLeafEntry> cndSetEarlyResultsRemoved = (ArrayList<SpatialPointLeafEntry>) candidateSet.clone();
    ArrayList<SpatialPointLeafEntry> earlyResults                = new ArrayList<SpatialPointLeafEntry>();

    double counter = 0;
    for (SpatialPointLeafEntry cnd : candidateSet){
      double maxDistCndQ = PruningHeuristic.vvMaxDistanceMinimumNorm(q, cnd);
      if (maxDistCndQ <= minDistKthCndQ)
        counter ++;
      if (counter >= k) {
        earlyResults.add(cnd);
        cndSetEarlyResultsRemoved.remove(cnd);
      }
    }

    timeEarlyResult.end();
    Log.appendln("    - Early results: " + earlyResults.size() + " of " + candidateSet.size() + " candidates. Remaining candidates: " + cndSetEarlyResultsRemoved.size() + ".. done in " + timeEarlyResult);


    Log.appendln("    - Performing refinement step..");
    Log.printFlush();
    CPUTimeDiff timeRefinementStep = new CPUTimeDiff();

    ArrayList<DBID> refined = refine(q, k, candidateSet, refinementSetPoints, refinementSetNodes);

    timeRefinementStep.end();
    Log.appendln("    Refinement step done in " + timeRefinementStep);
    Log.appendln("    - After refining: " + refined.size() + " cnds");

    return refined; // TODO: Fix: Add earlyResults to refined!
  }


  /***********************************/
  /*********** F I L T E R ***********/
  /***********************************/
  /**
   * @param q
   * @param k
   * @return
   */
  private ArrayList<ArrayList<?>> filter(DoubleVector q, int k){
    ArrayList<ArrayList<?>> cndsRefs = new ArrayList<ArrayList<?>>(); // will contain cndSet and refSet
    
    PriorityQueue<SimpleEntry<Double, TPLEntry>>  minHeap      = initializeMinHeap();                    // initialize mindist heap and insert (R-tree root, 0)
    ArrayList<SpatialPointLeafEntry>              cndSet       = new ArrayList<SpatialPointLeafEntry>(); // initialize candidate set
    ArrayList<SpatialPointLeafEntry>              refSetPoints = new ArrayList<SpatialPointLeafEntry>(); // initialize refinement points set
    ArrayList<TPLEntry>                           refSetNodes  = new ArrayList<TPLEntry>();              // initialize refinement nodes set

    double k_trim_mindist = 0.0;
    
    // while minHeap is not empty
    while(!minHeap.isEmpty()) {
      // entry=(spatEntry,key)=de-heap minHeap
      TPLEntry tplEntry = minHeap.poll().getValue();
      SpatialEntry spatEntry = tplEntry.getEntry();
      if (spatEntry.isLeafEntry()){  // throw away enheaped query node
        if (Arrays.equals(((SpatialPointLeafEntry) spatEntry).getValues(), q.getValues())){
          continue;
        }
      }

      k_trim_mindist = prune(q, k, cndSet, spatEntry);

      // if (trim(q, cndSet, spatEntry) = infinite
      if ( k_trim_mindist == Double.POSITIVE_INFINITY) {
        // then cndSet = cndSet + {spatEntry}
        if (spatEntry.isLeafEntry())
          refSetPoints.add((SpatialPointLeafEntry) spatEntry);
        else
          refSetNodes.add(tplEntry);
      } else { // entry may be or contain a candidate
        // if spatEntry is data point
        if(spatEntry.isLeafEntry()) {
          // then cndSet = cndSet + {spatEntry}
          cndSet.add((SpatialPointLeafEntry) spatEntry);
        } else { // else 
          RStarTreeNode node = tree.getNode(spatEntry);
          // if spatEntry points to a leaf node node
          if (node.isLeaf()){
            // for each point se2 in node ( sorted on dist(se2,q) )
            
            for(SpatialEntry se2 : ((AbstractNode<SpatialEntry>) node).getEntries()) {
              double distance = prune(q, k, cndSet, se2);
              // if (trim(q, cndSet, se2) != infinite)
              if(distance != Double.POSITIVE_INFINITY){
                // then insert (se2, dist(p,q)) in minHeap
                minHeap.add(new SimpleEntry<Double, TPLEntry>(distance, new TPLEntry(se2, tplEntry.getDepth() + 1)));
              } else {
                // refSet = refSet + {se2}
                refSetPoints.add((SpatialPointLeafEntry) se2);
              }
            }
          } else { // else if spatEntry points to an intermediate node
            // for each entry N_i in node
            for (SpatialEntry N_i : ((AbstractNode<SpatialEntry>) node).getEntries()) {
              // mindist(Nres_i, q) = trim(q, cndSet, N_i)
              double distance = prune(q, k, cndSet, N_i);
              // if mindist(Nres_i, q) = infinite
              if (distance == Double.POSITIVE_INFINITY){
                // refSet = refSet + {N_i}
                tplEntry.addToDepth(1);
                refSetNodes.add(new TPLEntry(N_i, tplEntry.getDepth()));
              } else {
                // else insert (N_i, mindist(Nres_i, q)) in minHeap
                tplEntry.addToDepth(1);
                minHeap.add(new SimpleEntry<Double, TPLEntry>(distance, new TPLEntry(N_i, tplEntry.getDepth())));
              }
            }
          }
        }
      }
    }
    
    cndsRefs.add(cndSet);
    cndsRefs.add(refSetPoints);
    cndsRefs.add(refSetNodes);
    
    return cndsRefs;
  }
  

  /***********************************/
  /*********** R E F I N E ***********/
  /***********************************/
  /**
   * @param q
   * @param k
   * @param candidateSet
   * @param refinementSetPoints
   * @param refinementSetNodes
   * @return
   */
  private ArrayList<DBID> refine(DoubleVector                     q,
                                 int                              k,
                                 ArrayList<SpatialPointLeafEntry> candidateSet,
                                 ArrayList<SpatialPointLeafEntry> refinementSetPoints,
                                 ArrayList<TPLEntry>              refinementSetNodes){

    HashMap<DBID, HashMap<Integer, TPLEntry>> toVisits               = new HashMap<DBID, HashMap<Integer, TPLEntry>>();
    HashMap<DBID, Integer>                    count                  = new HashMap<DBID, Integer>();
    ArrayList<SpatialPointLeafEntry>          selfPrunedCandidateSet = (ArrayList<SpatialPointLeafEntry>) candidateSet.clone();
    int removed = 0;
    
    /*
     * SELF PRUNING of candidates
     */
    CPUTimeDiff timeSelfPruning = new CPUTimeDiff();

    // todo (maybe small optimization): Remove init of toVisit(p) from here, do it on demand in refinement,
    // for each point p in candidateSet
    nextCandidate: for(int i = 0; i < candidateSet.size(); i++) {
        SpatialPointLeafEntry p = candidateSet.get(i);
        int counter = k;

        // for each other point p2 in candidateSet
        for(int j = 0; j < candidateSet.size(); j++) {
          SpatialPointLeafEntry p2 = candidateSet.get(j);
          if (p != p2){
            // if dist(p,p2)<dist(p,q)
            if (PruningHeuristic.vvMaxDistanceMinimumNorm(p, p2) < PruningHeuristic.vvMinDistanceMaximumNorm(p, q)){
              counter--;
              if (counter == 0){ // TO DO: Better use counter <= 0
                // candidateSet = candidateSet - {p}
                selfPrunedCandidateSet.remove(i - removed); // TO DO: This is very ugly!
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

    timeSelfPruning.end();
    Log.appendln("      - Self pruned: " + (candidateSet.size() - selfPrunedCandidateSet.size()) + " of " + candidateSet.size() + " candidates.. done in " + timeSelfPruning);
    
    ArrayList<DBID> results = new ArrayList<DBID>();

    while(true){
      k_refinement_round(q, k, selfPrunedCandidateSet, refinementSetPoints, refinementSetNodes, count, toVisits, results);
      
      // if candidateSet = {} return
      if(selfPrunedCandidateSet.isEmpty()) {
        break;
      }


      /*
       * Initialization of next round
       */
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
      RStarTreeNode node = tree.getNode((SpatialEntry) bestNode.getEntry());

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

    return results;
    
  }
  

  /***********************************************/
  /*********** R E F I N E - R O U N D ***********/
  /***********************************************/
  /**
   * @param q
   * @param k
   * @param candidateSet
   * @param refinementSetPoints
   * @param refinementSetNodes
   * @param count
   * @param toVisits
   * @param results
   */
  private void k_refinement_round(DoubleVector                              q,
                                  int                                       k,
                                  ArrayList<SpatialPointLeafEntry>          candidateSet,
                                  ArrayList<SpatialPointLeafEntry>          refinementSetPoints,
                                  ArrayList<TPLEntry>                       refinementSetNodes,
                                  HashMap<DBID, Integer>                    count,
                                  HashMap<DBID, HashMap<Integer, TPLEntry>> toVisits,
                                  ArrayList<DBID>                           results) {
    /*
     * do refinement for each point p in candidateSet
     */
    nextCandidate: for (int i = 0; i < candidateSet.size(); i++){
      SpatialPointLeafEntry p = candidateSet.get(i);
      int counter = count.get(p.getDBID());
      
      /*
       * try prune p with each point p2 € refinementSetPoints
       */
      for (SpatialPointLeafEntry p2 : refinementSetPoints){
        // if dist(p,p2)<dist(p,q)
        if (PruningHeuristic.vvMaxDistanceMinimumNorm(p, p2) < PruningHeuristic.vvMinDistanceMaximumNorm(p, q)){
          // counter(p)--
          counter--;
          // if counter(p) = 0
          if (counter == 0){  // TO DO: Better use counter <= 0
            // candidateSet = candidateSet - {p} -- false hit
            candidateSet.remove(i);
            i--;
            // goto nextCandidate
            continue nextCandidate;
          }
        }
      }
      count.put(p.getDBID(), counter);
      
      /*
       * try prune p with each MBN N entry € refinementSetNodes
       */
      for(TPLEntry entry : refinementSetNodes) {
        SpatialEntry N = entry.getEntry();
        // if maxdist(p,entry)<dist(p,q) and min_card(entry)>=counter(p)
        // TODO: min_card seems to be wrong? -> No false misses, but less pruning of true misses possible (min_card is smaller than necessary), if k > min_card
        if (vmMaxDistMinimumNorm(p, N) < PruningHeuristic.vvMinDistanceMaximumNorm(p, q) && min_card >= counter){
          // candidateSet = candidateSet - {p}
          candidateSet.remove(i);
          i--;
          // goto nextCandidate to test next candidate
          continue nextCandidate;
        }
      }
      
      /*
       * for each node MBR N entry in refinementSetNodes: Add N to toVisit(p)
       */
      for(TPLEntry entry : refinementSetNodes) {
        SpatialDirectoryEntry N = (SpatialDirectoryEntry) entry.getEntry();
        // if mindist(p,entry)<dist(p,q)
        if (PruningHeuristic.vmMinDistanceMaximumNorm(p, N) < PruningHeuristic.vvMaxDistanceMinimumNorm(p, q)){
          // add entry in set toVisit(p)
          toVisits.get(p.getDBID()).put(N.getEntryID(), entry);
        }
      }

      /*
       * if toVisit(p) = {}, add p to result
       */
      if (toVisits.get(p.getDBID()) != null && toVisits.get(p.getDBID()).size() == 0){
        // candidateSet = candidateSet - {p}
        candidateSet.remove(i);
        i--;
        toVisits.remove(p.getDBID());
        // and report p -- actual result
        DBID dbid = p.getDBID();

        DoubleDistance distance = (DoubleDistance) maxDistQuery.distance(dbid, q);
        results.add(dbid);
      }
    }
  }

  /**
   * Checks if maxdist between entry and k candidates is smaller than its mindist to q.
   * If so, returns infinity (--> entry is pruned), else, returns the mindist from q to entry.
   * @param q
   * @param k
   * @param candidateSet
   * @param entry
   * @return
   */
  private double prune (NumberVector<?> q, int k, ArrayList<SpatialPointLeafEntry> candidateSet, SpatialEntry entry){
    int pruneCount = 0;
    double qEntryMinDist = vmMinDistanceMaximumNorm(q, entry);

    for(SpatialPointLeafEntry cnd : candidateSet) {

      double cndEntryMaxDist = vmMaxDistMinimumNorm(cnd, entry);

      if (cndEntryMaxDist < qEntryMinDist)
        pruneCount++;
      }
      if(pruneCount >= k) {
        return Double.POSITIVE_INFINITY;
      }

    return qEntryMinDist;
  }

  /**
   * @param toVisits
   * @return
   */
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

  private PriorityQueue<SimpleEntry<Double, TPLEntry>> initializeMinHeap(){
    PriorityQueue<SimpleEntry<Double, TPLEntry>> minHeap = new PriorityQueue<SimpleEntry<Double, TPLEntry>>(50, new Comparator<SimpleEntry<Double, TPLEntry>>() {
      public int compare(SimpleEntry<Double, TPLEntry> o1, SimpleEntry<Double, TPLEntry> o2) {
        return Double.compare(o1.getKey(), o2.getKey());
      }
    });

    minHeap.add(new SimpleEntry<Double, TPLEntry>(0.0, new TPLEntry(tree.getRootEntry(), 0)));

    return minHeap;
  }
}
