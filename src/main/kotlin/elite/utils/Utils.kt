package elite.utils

import kotlin.math.*

fun calcDistance(x1: Double, x2: Double, y1: Double, y2: Double, z1: Double, z2: Double) =
    sqrt((x2.minus(x1)).pow(2).plus((y2.minus(y1)).pow(2)).plus((z2.minus(z1)).pow(2)))

var consoleCounter = 0L

fun consoleStringCounter(): Long {
    consoleCounter = consoleCounter.inc()
    return consoleCounter
}

const val FILENAME = "Demo.txt"