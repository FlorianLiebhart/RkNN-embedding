package algorithms

object EagerMTransl {
  
  def eager(){
	  var PopSize, VisitedSize: Int = -1
	  var isVisited, pointseen: Seq[Boolean] = null
	  
//	  // initialization
//	  pointseen.assign(NodeNum,false);
//	  PopSize=VisitedSize=0;
//	  isVisited.assign(NodeNum,false);
//	  
//	  int nns[k];
//	  float NNdists[k];
//	  int prune_up=0, old_gnc=gNetNodeAcc;
//	  
//	  while (!H.empty()) {
//	    StepEvent event=H.top();
//	    H.pop();  PopSize++;
//	    
//	    int NodeID=event.node;
//	    if (isVisited[NodeID]) continue;
//	    isVisited[NodeID]=true;   VisitedSize++;
//	    
//	    fill(nns,nns+k,-1);
//	    fill(NNdists,NNdists+k,MAX_DIST);
//	    if (isMatNN) {  // use materialized NN
//	      for (int j=0;j<k;j++) {
//	        nns[j]=node_labels[j][NodeID];
//	        NNdists[j]=node_dists[j][NodeID];
//	      }
//	      gNetNodeAcc++;
//	    } else  // no materialized NN
//	      RangeNN(NodeID,event.dist,nns,NNdists);
//	    
//	    for (int j=0;j<k;j++) {
//	      int curnode_NNID=nns[j];
//	      if (curnode_NNID>=0) 
//	      if (pointseen[curnode_NNID]==false) { // duplicate check
//	        pointseen[curnode_NNID]=true;
//	        if (HasPoint[curnode_NNID])   // 2004/5/16 08:08AM
//	          RA_NN_Verify(curnode_NNID); // expands as small range as possible
//	      }
//	    }
//	    if (event.dist>NNdists[k-1]) {
//	      prune_up++;
//	      continue; // no need to expand via this node
//	    }
//	    EnqueueAdjacentNodes(H,event,isVisited);
//	  }
//	  
//	  if (gNetNodeAcc>0)
//	  printf("nn-call %d, save: %d\n",prune_up,gNetNodeAcc-old_gnc-prune_up);
//	  //printf("Pop: %d  ;  Visited: %d\n",PopSize,VisitedSize);
  }
}