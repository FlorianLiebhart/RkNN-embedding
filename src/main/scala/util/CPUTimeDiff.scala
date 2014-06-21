package util

import java.lang.management.ManagementFactory

import util.Utils.formatThousands

case class CPUTimeDiff(){
    val tStart: java.lang.Long = ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime / 1000000

    private var tEnd: java.lang.Long = null

    def end   = { tEnd = ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime / 1000000 }

    def diffMillis: Int = (tEnd - tStart).toInt

    override def toString: String = {
      if(tEnd == null)
        throw new RuntimeException("End time not specified!")

      else {
         s"{{{ ${formatThousands(diffMillis)} ms.(cpu time)}}}"
       }
    }
  }