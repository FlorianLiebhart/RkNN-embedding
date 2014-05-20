package algorithms

import graph.{SEdge, SGraph, SVertex}

import scala.collection.mutable.PriorityQueue
import util.Utils.VD
import util.Utils.t2ToOrdered

object Eager {

  //  val graphGenerator = GraphGenerator.getInstance()
  val graph = new SGraph()

  def eager(graph: SGraph, q: SVertex, k: Int): IndexedSeq[VD] = {
    val h = new PriorityQueue[VD]
    h.enqueue((q, 0))
    var visitedNodes   = Seq.empty[SVertex]
    var verifiedPoints = IndexedSeq.empty[SVertex]
    var RkNN_q         = IndexedSeq.empty[VD]
    if(k <= 0)
      return RkNN_q
    while (!h.isEmpty) {
//      val h2   = h; val visitedNodes2 = visitedNodes; val verifiedPoints2 = verifiedPoints; val RkNN_q2 = RkNN_q
      /* When a node n is deheaped, eager applies Lemma 1 in order to
       * determine whether the expansion should proceed.*/
      val n   = h.dequeue
      if (!visitedNodes.contains(n._1)) {
        visitedNodes = visitedNodes :+ n._1
        /* In particular, it first retrieves the NN of n
         * by performing a range-NN query (n, k, d(n,q) */
        var kNN_n = rangeNN(graph, n._1, k, n._2, q) // .filterNot(_._1 equals q) // Needs to be there, ansonsten kann die nächste if- Anfrage durch q getäuscht werden. --> this is correct, verify checks if q is in it!

        // Evtl. zusätzlich Performance: Prüfe, ob kNN_n > k  ==> Kein verifizieren Notwendig, da kein Punkt rknn von q sein kann. (Da sich alle Punkte innerhalb der Range gegenseitig prunen)
        if(n._1.containsObject && !(n._1 equals q))
          kNN_n :+= n //verify(graph, n, k, q)
        /* If there are k points p, such that d(n, q) > d(n, p),
         * the expansion does not proceed further because
         * (according to Lemma 1) n cannot lead to a RNN of q. */
        for (p <- kNN_n){ // evtl. breakpoint für n= 3
          if (!verifiedPoints.contains(p._1)) {
            /* In this case, however, we need to verify if p € RkNN(q)
             * because Lemma 1 is only true for points p' != p.
             * Thus, eager issues a verify(p, k, q) query.

             * If q = kNN(p), p is added to the result. */
//          val p_updatedDist = new VD(p._1, p._2 + n._2)
            val p_updatedDist = new VD(p._1, List(p._2 + n._2, h.find(x => x._1 equals p._1).map(_._2).getOrElse(Double.PositiveInfinity), if(p._1 equals n._1) n._2 else Double.PositiveInfinity).min)

            if (verify(graph, p_updatedDist, k, q))
                RkNN_q :+= p_updatedDist
            /* Furthermore, p is marked as verified in order not to be expanded,
             * if it is found again in the future through another node.
             */
            verifiedPoints :+= p._1
          }
        }
        /* If no k data points are discovered within distance d(n,q)
         * from n, the algorithm en-heaps the adjacent nodes of n. */
        if (kNN_n.size < k)
          for (n_i <- graph.getNeighborsFrom(n._1))
            h.enqueue((n_i, n._2 + graph.getEdge(n._1, n_i).getWeight))
      }
    }
    RkNN_q.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }

  //        val unVerifiedPoints = kNN_n diff verifiedPoints
  //        verifiedPoints ++= unVerifiedPoints
  //        val newlyVerified = unVerifiedPoints.filter(p => verify(graph, p, k, q))
  //        RkNN_q ++= newlyVerified

  /**
   * From a given point q, finds the k next neighbours with a maximum distance of e, if such k points exist.
   * Otherwise, it returns a smaller number (possibly 0) of NNs.
   * Returns an empty list if e = 0
   * @param q
   * @param k
   * @param e
   * @return
   * TODO: statt verifyMode: inclusiveE: Boolean und stopAtQ:Boolean (nötig zB für NaiveRkNN)
   */
  // TODO: NaiveAlgorithm should not use rangeNN cuz double.infinity wont work any more
  def rangeNN(graph: SGraph, q: SVertex, k: Int, e: Double, originalQ: SVertex = null): IndexedSeq[VD] = {
    val h = PriorityQueue.empty[VD]
    var knns = IndexedSeq.empty[VD] // evtl. als PriorityQueue
    var visitedNodes: IndexedSeq[SVertex] = IndexedSeq(q)
    if(k <= 0)
      return knns

//    h.enqueue((n, 0))
    val neighbours: Seq[SVertex] = graph.getNeighborsFrom(q)   // evtl priorityQueue
    val vertexDistanceTuples = neighbours.map(n_i => (n_i, graph.getEdge(q, n_i).getWeight))
    vertexDistanceTuples.map(x => h.enqueue(x))

    processQueue

    def processQueue: Any = {
      while (!h.isEmpty) {
        val n = h.dequeue()
        if (!visitedNodes.contains(n._1)) {
          visitedNodes :+= n._1

          // ensure n.dist < maxdist für rangenn (da sonst lemma 1 nicht gilt)
          // und n.dist <= maxdist für verify (damit q enthalten ein kann, da e = d(n,q)!)
          if (n._2 >= e)  //TODO: muss richtig sein! Denn wenn d(q, n) = d(n, p), dann gilt Lemma 1 nicht und p2 kann nicht als rknn ausgeschlossen werden, weil dann q € knn(p2) (zB. falls d(n3,n6) = 4)
            return

          // performance verbessern: breche ab, sobald q gefunden
          // (evtl. nicht durchführen, aber in Ausarbeitung aufnehmen!), oder durchführen, und am ende der Ausarbeitung zu "Verbesserungen" aufnehmen


          // Grenzfall: Falls "letztes" k mehrere nächste Nachbarn mit gleichen Abstand
          if (knns.size >= k && n._2 > knns.last._2)
            return

          if (n._1.containsObject)
            knns :+= n

          val unVisitedNeighbours: Seq[SVertex] = graph.getNeighborsFrom(n._1) diff visitedNodes
          val vertexDistanceTuples = unVisitedNeighbours.map(n_i => (n_i, n._2 + graph.getEdge(n._1, n_i).getWeight))
          vertexDistanceTuples.map(x => h.enqueue(x))
        }
      }
    }

    val qWithDist = knns.find(_._1 equals originalQ)
    if(qWithDist.isDefined)
      knns.filter(_._2 < qWithDist.get._2)
    else
      knns
  }
  /**
   * Given two points p and q, a verification query verify(p,k,q)
   * checks whether q is among the kNNs of a data point p by applying
   * a range-NN query around the node that contains p. verify(p,k,q)
   * is in fact equivalent to range-NN(p,k,d(p,q)), i.e., search
   * terminates as soon as q is encountered (the maximum range e in this case
   * is implied by the distance d(p,q)).
   * @param p A Vertex with its distance from q
   * @param k
   * @param q
   * @return
   */
  def verify(graph: SGraph, p: VD, k: Int, q: SVertex): Boolean = {
    // ist richtig, aber muss ersetzt werden durch eigene Implementierung der Methode, denn es kann vorzeitig
    // abgebrochen werden, sobald q gefunden wird   --> in Ausarbeitung aufnehmen!
    // TODO: VERIFY-Flag in Range-NN, sodass die Methode verify entfallen kann (bzw. an diese Methode delegieren kann.)
    // TODO: (Verify-Flag auch wichtig für:) Für Verify aber ganz klar: dist > e => break, weil sonst q nicht gefunden wird, aber (da bei k-nächste Nachbarn
    // bei gleichberechtigen Ergebnissen alle zurück gegeben werden müssen, q trotzdem als knn gilt. (in Ausarbeitung aufnehmen!)
    val result = rangeNN(graph, p._1, k, p._2, q)
    // TODO: maxResultSize schmarrn, weil es für die Verifizierung nix ändert, ob auf dem zu verifizierenden Knoten ein Punkt drauf ist oder nicht

    // falsch! nicht < k, sondern die Größen der einzelnen kontrollieren, oder sichergehen dass die range < shortestdist zu q!
    // rangeNN ändern: Am Ende q und alle >= q rausschmeissen!

    result.size < k
  }
}