package beam.sim.metrics

class PerformanceStats {
    var totalTime: Long = 0
    var numberOfStats:Int = 0

    def avg = totalTime / numberOfStats

    def addTime(time: Long): Unit = {
      totalTime = totalTime + time
      numberOfStats = numberOfStats + 1
    }

    def combine(stats: PerformanceStats): PerformanceStats = {
      val combined = new PerformanceStats
      combined.totalTime = this.totalTime + stats.totalTime
      combined.numberOfStats = this.numberOfStats + stats.numberOfStats
      combined
    }

    def reset: Unit = {
      totalTime = 0
      numberOfStats = 0
    }

    override def toString: String = s"$numberOfStats (average time: $avg [ms]; total time: $totalTime [ms])"
  }