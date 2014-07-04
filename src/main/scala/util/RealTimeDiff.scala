package util


case class RealTimeDiff(){
    val tStart: java.lang.Long = System.currentTimeMillis

    private var tEnd: java.lang.Long = null

    def end   = tEnd = System.currentTimeMillis

    def diffMillis: Int = (tEnd - tStart).toInt

    override def toString: String = {
      if(tEnd == null)
        throw new RuntimeException("End time not specified")

      else {
         s"${diffMillis} ms.(real time)"
       }
    }
  }