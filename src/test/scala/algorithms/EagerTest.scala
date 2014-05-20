package algorithms

import org.scalatest.{FunSuite}
import org.scalatest.matchers.ShouldMatchers
import util.Utils.VD
import util.Utils.createExampleGraph
import util.Utils.t2ToOrdered
import graph.core.Vertex

/**
 * Created: 31.12.13, 10:45
 * @author fliebhart
 */
class EagerTest extends FunSuite with ShouldMatchers {

  // TODO: Test durchführen durch vergleich der Ergebnisse mit primitiver Implementierung
  /*
   * Other Tests
   */
  test("IndexedSeq preserves insertion order (important for knns in rangeNN)") {
    val mySeq = IndexedSeq.empty :+ 5 :+ 3 :+ 6 :+ 0
    assert(mySeq(0) == 5)
    assert(mySeq(1) == 3)
    assert(mySeq(2) == 6)
    assert(mySeq(3) == 0)
  }

  /*
   * RangeNN - Tests
   *
   *  TODO: Schreibe neben den spezifischen Tests für meinen Graphen auch allg. Tests für generierte Graphen
   * (zB. Checke, - dass alle Ergebnisse Objekte beinhalten,
   *              - dass alle nicht zurückgegebenen Knoten KEINE Objekte beinhalten für Range = Infinite und k = infinite
   *              - dass für k = 0 nichts zurück gegeben wird
   *              - checke die restl. Tests auf allgemeine Testbarkeit
   *              - Führe diese Tests über mehrere generierte Graphen durch (for ...graphen .. do Test )
   *              - Checke, ob rangeNN die richtigen distanzen zurück gibt!
   */
  test("rangeNN all and only nearest neighbour nodes containing objects returned for infinite k and dist"){
    val graph =  createExampleGraph
    val knns = Eager.rangeNN(graph, graph.getVertex(3), Integer.MAX_VALUE, Double.PositiveInfinity).map(_._1)
    knns should have size 4
    knns.filterNot(_.containsObject) should be ('empty)
  }
  test("rangeNN returns all object-nodes in the correct order") {
    val graph =  createExampleGraph
    val knns = Eager.rangeNN(graph, graph.getVertex(3), Integer.MAX_VALUE, Double.PositiveInfinity).map(_._1)
    knns(0) should equal (graph.getVertex(6))
    knns(1) should equal (graph.getVertex(4))
    knns(2) should equal (graph.getVertex(5))
    knns(3) should equal (graph.getVertex(7))
  }
  test("rangeNN returns only values smaller than e"){
    val graph =  createExampleGraph
    val knns = Eager.rangeNN(graph, graph.getVertex(2), Integer.MAX_VALUE, 5.0).map(_._1)
    knns should have size 2
    knns(0) should equal (graph.getVertex(5))
    knns(1) should equal (graph.getVertex(6))
  }
  test("rangeNN Grenzfall: Auch mehr als k Ergebnisse möglich, falls es beim 'letzten k' Nachbarn mit selbem Abstand gibt"){
    val graph =  createExampleGraph
    val knns = Eager.rangeNN(graph, graph.getVertex(7), 2, 11.0).map(_._1)
    knns should have size 3
    knns(0) should equal (graph.getVertex(5))
    assert((knns(1) equals graph.getVertex(4)) && (knns(2) equals graph.getVertex(6))
        || (knns(1) equals graph.getVertex(6)) && (knns(2) equals graph.getVertex(4)))
  }
  test("rangeNN does not return results if distance is 0.0"){
    val graph =  createExampleGraph
    val knns = Eager.rangeNN(graph, graph.getVertex(4), 1, 0.0).map(_._1)
    knns should be ('empty)
  }
  // todo: test: compareTo with solution from other guy? (Johannes fragen wg. Fehler)

  /*
   * Verify- Tests
   * TODO: Tests für generierte Graphen schreiben, nicht nur von den statischen
   */
  test("verify correctly verifies all valid rknns"){
    val graph =  createExampleGraph
    val isRkNN1 = Eager.verify(graph, new VD(graph.getVertex(6), 7.0), Integer.MAX_VALUE, graph.getVertex(4))
    val isRkNN2 = Eager.verify(graph, new VD(graph.getVertex(5), 8.0), Integer.MAX_VALUE, graph.getVertex(4))
    isRkNN1 should be (true)
    isRkNN2 should be (true)
  }
  test("verify correctly unverifies all unvalid rknns"){
    val graph =  createExampleGraph
    val isRkNN1 = Eager.verify(graph, new VD(graph.getVertex(4), Double.PositiveInfinity), Integer.MAX_VALUE, graph.getVertex(1))
    val isRkNN2 = Eager.verify(graph, new VD(graph.getVertex(4), Double.PositiveInfinity), Integer.MAX_VALUE, graph.getVertex(2))
    val isRkNN3 = Eager.verify(graph, new VD(graph.getVertex(4), Double.PositiveInfinity), Integer.MAX_VALUE, graph.getVertex(3))
    val isRkNN4 = Eager.verify(graph, new VD(graph.getVertex(4), Double.PositiveInfinity), Integer.MAX_VALUE, graph.getVertex(4))
    val isRkNN5 = Eager.verify(graph, new VD(graph.getVertex(7), Double.PositiveInfinity), Integer.MAX_VALUE, graph.getVertex(7))
    val isRkNN6 = Eager.verify(graph, new VD(graph.getVertex(6), 0.0), Integer.MAX_VALUE, graph.getVertex(4))
    val isRkNN7 = Eager.verify(graph, new VD(graph.getVertex(5), 7.0), Integer.MAX_VALUE, graph.getVertex(4))

    isRkNN1 should be (false)
    isRkNN2 should be (false)
    isRkNN3 should be (false)
    isRkNN4 should be (false)
    isRkNN5 should be (false)
    isRkNN6 should be (false)
    isRkNN7 should be (false)
  }

  /*
   * Eager - Tests
   * TODO: Für generierte Graphen durchführen
   * 
   */
  // specific
  test("eager all returned nodes contain objects"){
    val graph = createExampleGraph
    val rknns = Eager.eager(graph, graph.getVertex(4), Integer.MAX_VALUE)
    rknns.filterNot(_._1.containsObject) should be ('empty)
  }
  // specific
  test("eager all returned nodes are rknns"){
    val graph = createExampleGraph
    val rknns = Eager.eager(graph, graph.getVertex(4), Integer.MAX_VALUE)
    rknns.filterNot(x => Eager.verify(graph, x, Integer.MAX_VALUE, graph.getVertex(4))) should be ('empty)
  }
  // specific
  test("eager all possible rknns are being returned for maximum k"){
    val graph = createExampleGraph
    val rknns = Eager.eager(graph, graph.getVertex(4), Integer.MAX_VALUE).map(_._1)
    rknns should have size 3
    rknns.contains(graph.getVertex(6)) should be (true)
    rknns.contains(graph.getVertex(5)) should be (true)
    rknns.contains(graph.getVertex(7)) should be (true)
  }
  test("eager returns all object-nodes in the correct order") {
    val graph =  createExampleGraph
    val rknns = Eager.eager(graph, graph.getVertex(4), Integer.MAX_VALUE)
    rknns(0)._1 should equal (graph.getVertex(6))
    rknns(1)._1 should equal (graph.getVertex(5))
    rknns(2)._1 should equal (graph.getVertex(7))
  }
  test("eager returns the correct result on exampleGraph for k=1") {
    val graph =  createExampleGraph
    val rknns = Eager.eager(graph, graph.getVertex(4), 1)
    rknns should have size 2
    rknns(0)._1 should equal (graph.getVertex(6))
    rknns(1)._1 should equal (graph.getVertex(5))
  }
  test("eager Grenzfall: Auch mehr als k Ergebnisse möglich, falls es beim 'letzten k' nächste Nachbarn mit selbem Abstand gibt"){
    val graph =  createExampleGraph
    val rknns = Eager.eager(graph, graph.getVertex(5), 2)
    rknns should have size 3
    rknns(0)._1 should equal (graph.getVertex(4))
    assert((rknns(1)._1 equals graph.getVertex(7)) && (rknns(2)._1 equals graph.getVertex(6))
        || (rknns(1)._1 equals graph.getVertex(6)) && (rknns(2)._1 equals graph.getVertex(7)))
  }
}