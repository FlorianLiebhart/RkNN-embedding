package algorithms

import scala.collection.mutable.{PriorityQueue}
import scala.collection.mutable.HashMap

import graph.{SVertex, SGraph}
import util.Utils._


object Dijkstra {


  def dijkstra(sGraph: SGraph, source: SVertex): HashMap[SVertex, Double] = {

    val h = new PriorityQueue[VD]()
    var distances = new HashMap[SVertex, Double]()
    var previous = new HashMap[SVertex, VD]()

    distances.put(source, 0)

    for(v <- sGraph.getAllVertices){
      if(!(v equals source)){
        distances.put(v, Double.PositiveInfinity)
        previous.put(v, null)
      }
      h.enqueue((v,distances.get(v).get))
    }

    var visitedNodes = HashMap[Integer, SVertex]()

    while (!h.isEmpty) {
      var n = h.dequeue()
      if(!(visitedNodes contains n._1.id)){
        visitedNodes.put(n._1.id, n._1)
        for(v <- sGraph.getNeighborsFrom(n._1)){
          val alt = distances.get(n._1).get + sGraph.getEdge(n._1, v).getWeight
          if(alt < distances.get(v).get){
            distances.put(v, alt)
            previous.put(v, n)
            h.enqueue((v, alt))
          }
        }
      }
    }
    distances
  }
}
