package algorithms

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * Created: 31.12.13, 10:45
 * @author fliebhart
 */
class EagerTest extends FunSuite with ShouldMatchers{

  test("IndexedSeq preserves insertion order")         {
    val mySeq = IndexedSeq.empty :+ 5 :+ 3 :+ 6 :+ 0
    assert(mySeq(0) == 5)
    assert(mySeq(1) == 3)
    assert(mySeq(2) == 6)
    assert(mySeq(3) == 0)
  }

}
