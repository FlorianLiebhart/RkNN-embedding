package algorithms

import graph.core._
import graph.core.Graph
import scala.collection.mutable.PriorityQueue
import scala.collection.JavaConverters._
import util.Utils.VD
import util.Utils.t2ToOrdered

object Eager {

  //  val graphGenerator = GraphGenerator.getInstance()
  val graph = new Graph()

  def eager(graph: Graph, q: Vertex, k: Int): IndexedSeq[VD] = {
    val h = new PriorityQueue[VD]
    h.enqueue((q, 0))
    var visitedNodes   = Seq.empty[Vertex]
    var verifiedPoints = IndexedSeq.empty[Vertex]
    var RkNN_q         = IndexedSeq.empty[VD]
    while (!h.isEmpty) {
      val h2   = h; val visitedNodes2 = visitedNodes; val verifiedPoints2 = verifiedPoints; val RkNN_q2 = RkNN_q

      /* When a node n is deheaped, eager applies Lemma 1 in order to
       * determine whether the expansion should proceed.*/
      val n   = h.dequeue
      if (!visitedNodes.contains(n._1)) {
        visitedNodes = visitedNodes :+ n._1
        /* In particular, it first retrieves the NN of n
         * by performing a range-NN query (n, k, d(n,q) */
        val kNN_n = rangeNN(graph, n._1, k, n._2, false).filterNot(_._1 equals q) // Needs to be there, ansonsten kann die nächste if- Anfrage durch q getäuscht werden. --> this is correct, verify checks if q is in it!

        /* If there are k points p, such that d(n, q) > d(n, p),
         * the expansion does not proceed further because
         * (according to Lemma 1) n cannot lead to a RNN of q. */
        for (p <- kNN_n){ // evtl. breakpoint für n= 3
          if (!verifiedPoints.contains(p._1)) {
            /* In this case, however, we need to verify if p € RkNN(q)
             * because Lemma 1 is only true for points p' != p.
             * Thus, eager issues a verify(p, k, q) query.

             * If q = kNN(p), p is added to the result. */
              val p_updatedDist = new VD(p._1, scala.math.min(p._2 + n._2, h.find(x => x._1 equals p._1).map(_._2).getOrElse(Double.PositiveInfinity)))
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
          for (n_i <- graph.getNeighborsFrom(n._1).asScala)
            h.enqueue((n_i, n._2 + graph.getEdge(n._1, n_i).getWeight))
      }
    }
    RkNN_q
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
   */
  def rangeNN(graph: Graph, q: Vertex, k: Int, e: Double, verifyMode: Boolean): IndexedSeq[VD] = {
    val h = PriorityQueue.empty[VD]
    var knns = IndexedSeq.empty[VD] // evtl. als PriorityQueue
    var visitedNodes: IndexedSeq[Vertex] = IndexedSeq(q)

//    h.enqueue((n, 0))
    val neighbours: Seq[Vertex] = graph.getNeighborsFrom(q).asScala   // evtl priorityQueue
    val vertexDistanceTuples = neighbours.map(n_i => (n_i, graph.getEdge(q, n_i).getWeight))
    vertexDistanceTuples.map(x => h.enqueue(x))

    processQueue

    def processQueue: Any = {
      while (!h.isEmpty) {
        val el = h.dequeue()
        val n = el._1
        val d_nq = el._2
        if (!visitedNodes.contains(n)) {
          visitedNodes :+= n

          // ensure n.dist < maxdist für rangenn (da sonst lemma 1 nicht gilt)
          // und n.dist <= maxdist für verify (damit q enthalten ein kann, da e = d(n,q)!)
          if (!verifyMode && (d_nq >= e) || verifyMode && (d_nq > e)) //TODO: muss richtig sein! Denn wenn d(q, n) = d(n, p), dann gilt Lemma 1 nicht und p2 kann nicht als rknn ausgeschlossen werden, weil dann q € knn(p2) (zB. falls d(n3,n6) = 4)
            return

          // Grenzfall: Falls "letztes" k mehrere nächste Nachbarn mit gleichen Abstand
          if (knns.size >= k && d_nq > knns.last._2)
            return

          if (n.containsObject)
            knns :+= el

          val unVisitedNeighbours: Seq[Vertex] = graph.getNeighborsFrom(n).asScala diff visitedNodes
          val vertexDistanceTuples = unVisitedNeighbours.map(n_i => (n_i, d_nq + graph.getEdge(n, n_i).getWeight))
          vertexDistanceTuples.map(x => h.enqueue(x))
        }
      }
    }
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
  def verify(graph: Graph, p: VD, k: Int, q: Vertex): Boolean = {
    // ist richtig, aber muss ersetzt werden durch eigene Implementierung der Methode, denn es kann vorzeitig
    // abgebrochen werden, sobald q gefunden wird   --> in Ausarbeitung aufnehmen!
    // TODO: VERIFY-Flag in Range-NN, sodass die Methode verify entfallen kann (bzw. an diese Methode delegieren kann.)
    // TODO: (Verify-Flag auch wichtig für:) Für Verify aber ganz klar: dist > e => break, weil sonst q nicht gefunden wird, aber (da bei k-nächste Nachbarn
    // bei gleichberechtigen Ergebnissen alle zurück gegeben werden müssen, q trotzdem als knn gilt. (in Ausarbeitung aufnehmen!)
    rangeNN(graph, p._1, k, p._2, true).map(_._1).contains(q) // kein shortest path! (siehe S4, letzter Absatz!)
  }
}