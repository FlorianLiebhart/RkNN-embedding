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
    var visitedNodes = Seq.empty[Vertex]
    var verifiedPoints = IndexedSeq.empty[Vertex]
    var RkNN_q = IndexedSeq.empty[VD]
    while (!h.isEmpty) {
      val el = h.dequeue
      val n = el._1
      val d_nq = el._2
      if (!visitedNodes.contains(n)) {
        visitedNodes = visitedNodes :+ n
        val kNN_n = rangeNN(graph, n, k, d_nq, false)

        //        val unVerifiedPoints = kNN_n diff verifiedPoints
        //        verifiedPoints ++= unVerifiedPoints
        //        val newlyVerified = unVerifiedPoints.filter(p => verify(graph, p, k, q))
        //        RkNN_q ++= newlyVerified

        for (p <- kNN_n)
          if (!verifiedPoints.contains(p)) {
            verifiedPoints :+= p._1
            if (verify(graph, p, k, q))
              RkNN_q :+= p
          }
        if (kNN_n.size < k)
          for (n_i <- graph.getNeighborsFrom(n).asScala)
            h.enqueue((n_i, d_nq + graph.getEdge(n, n_i).getWeight))
      }
    }
    RkNN_q
  }

  /**
   * From a given point q, finds the k next neighbours with a maximum distance of e, if such k points exist.
   * Otherwise, it returns a smaller number (possibly 0) of NNs.
   * Returns an empty list if e = 0
   * @param q
   * @param k
   * @param e
   * @return
   *
   */
  def rangeNN(graph: Graph, q: Vertex, k: Int, e: Double, verifyMode: Boolean): IndexedSeq[VD] = {
    val h = PriorityQueue.empty[VD]
    var knns = IndexedSeq.empty[VD]
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

          // ensure n.dist <= maxdist
          if (d_nq > e)
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
    rangeNN(graph, p._1, k, p._2, true).map(_._1).contains(q) // kein shortest path! (siehe S4, letzter Absatz!)
  }
}