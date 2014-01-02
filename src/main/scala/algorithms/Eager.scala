package algorithms
import graph.GraphGenerator
import graph.core._
import graph.core.Graph
import scala.collection.mutable.PriorityQueue
import scala.collection.JavaConverters._
import com.sun.jdi.event.StepEvent
import scala.collection.{mutable, BitSet}
import java.util

object Eager {

  type T2 = Tuple2[Vertex, Double]
  implicit def t2ToOrdered(thisT2: T2): Ordered[T2] = new Ordered[T2] {
    def compare(otherT2: T2): Int = thisT2._2.compare(otherT2._2)
  }

  val graphGenerator = GraphGenerator.getInstance()
  val graph = new Graph()

  def eager(graph: Graph, q: Vertex, k: Int): Seq[Vertex] = {
    val pq = new PriorityQueue[T2]
    pq.enqueue((q, 0))
    var visitedNodes = Seq.empty[Vertex]
    val verifiedPoints = Seq.empty[Vertex]
    val RkNN_q = Seq.empty[Vertex]
    while (!pq.isEmpty) {
      val el = pq.dequeue()
      val n = el._1
      val d_nq = el._2
      if (!visitedNodes.contains(n)) { // evtl. Seq ersetzen durch HashMap
        visitedNodes = visitedNodes :+ n
        val kNN_n = rangeNN(n,k,d_nq)

        for (p <- kNN_n)
          if (!verifiedPoints.contains(p)) {
            verifiedPoints :+ p
            if (verify(p._1, k, q))
              RkNN_q :+ p
          }
        if (kNN_n.size < k)
          for (n_i <- graph.getNeighborsFrom(n).asScala.toSeq)
            pq.enqueue((n_i, d_nq + graph.getEdge(n, n_i).getWeight()))
      }
    }
    RkNN_q
  }

  /**
   * rangeNN(n,k,d(n,q)) retrieves the k nearest data points
   * with (network) distance smaller than e from n, if such k points exist.
   * Otherwise, it returns a smaller number (possibly 0) of NNs
   * @param n
   * @param k
   * @param e
   * @return
   */
//  def rangeNN(graph: Graph, n: Vertex, k: Int, e: Double): Seq[Vertex] = {
//    val resList: Seq[Vertex] = Seq.empty[Vertex];
//    graph.getNeighborsFrom()
//    resList
//  }

  /**
   * From a given point n, finds the k next neighbours with a maximum distance of e.
   * @param n
   * @param k
   * @param e
   * @return
   */
  def rangeNN(n: Vertex, k: Int, e: Double): Seq[T2] = {
    val h:PriorityQueue[T2] = PriorityQueue.empty[T2]
    val isVisited: Seq[Vertex] = Seq.empty[Vertex]
    val nns: IndexedSeq[T2] = IndexedSeq.empty[T2]
                                     
    h.enqueue((n,0))
    processQueue

    def processQueue: Any = {
      while(!h.isEmpty) {
        val currentNode = h.dequeue()
        if (!isVisited.contains(currentNode)){
          isVisited :+ currentNode

          // ensure event.dist<=dist here
          if (currentNode._2 > e)
            return

          // solve the boundary case
          if (nns.size >= k && currentNode._2 > nns.last._2)
            return

          if (nns.size < k && currentNode._1.containsObject())
            nns :+ currentNode

          val visitedNeighbours: Seq[Vertex] = graph.getNeighborsFrom(currentNode._1).asScala.toList.filter(x => isVisited.contains(x))
          val enqueueList = visitedNeighbours.map(x => (x, graph.getEdge(currentNode._1, x).getWeight))
          enqueueList.map(x => h.enqueue(x))
        }
      }
    }
    nns
  }
  /**
   * Given two points p and q, a verification query verify(p,k,q)
   * checks whether q is among the kNNs of a data point p by applying
   * a range-NN query around the node that contains p. verify(p,k,q)
   * is in fact equivalent to range-NN(p,k,d(p,q)), i.e., search
   * terminates as soon as q is encountered (the maximum range e in this case
   * is implied by the distance d(p,q)).
   * @param p
   * @param k
   * @param q
   * @return
   */
  def verify(p: Vertex, k: Int, q: Vertex): Boolean = {
    false
  }
}