package elite.utils

class Stopwatch {
    private var startTime: Long = 0L

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun stopWithConsoleOutput(prefix: String = "Time: ") {
        if (startTime == 0L) throw Exception("Not invoke fun start()")

        val time: Double = System.currentTimeMillis().minus(startTime).toDouble().div(1000)
        println("$prefix $time")
        startTime = 0L
    }
}