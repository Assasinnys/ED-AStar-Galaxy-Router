package elite.utils

import elite.algorithm.StarPoint
import elite.replaces
import org.json.JSONArray
import java.io.*

fun Reader.readTextWithProgress(): String {
    val buffer = StringWriter()
    val timeStart = System.currentTimeMillis()
    val downloadedChars = copyTo(buffer)
    println("Totally downloaded: $downloadedChars symbols or ${conventCharsToMB(downloadedChars)} MB")
    println("for ${System.currentTimeMillis().minus(timeStart).toDouble().div(1000)} sec.")
    return buffer.toString()
}

fun Reader.copyTo(out: Writer, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
//    val speedMeter = DownloadSpeedMeter()
    var charsCopied: Long = 0
    val buffer = CharArray(bufferSize)
//    speedMeter.start()
    var chars = read(buffer)
    while (chars >= 0) {
        out.write(buffer, 0, chars)
        charsCopied += chars
//        speedMeter.checkDownloadSpeed(charsCopied)
        println("Downloaded ${conventCharsToMB(charsCopied)} MB")
        println("Heap size: ${Runtime.getRuntime().maxMemory()}")
        chars = read(buffer)
    }
    return charsCopied
}

fun JSONArray.range() = 0..length().minus(1)

fun conventCharsToMB(chars: Long): Double = chars.toDouble().div(1048576L)

fun readJsonFromFile(file: File) = FileReader(file).readTextWithProgress()

fun <T> MutableList<T>.addIfAbsent(element: T) {
    if (!this.contains(element))
        this.add(element)
}

fun HashMap<Long, StarPoint>.smartAdd(newStarPoint: StarPoint) {
    if (this.notContains(newStarPoint.systemId64)) {
        this[newStarPoint.systemId64] = newStarPoint
    } else {
        val oldStarPoint = this[newStarPoint.systemId64]!!
//        println("${consoleStringCounter()} Old star point G -> ${oldStarPoint.costG} vs ${newStarPoint.costG} <- new star point G")
        if (oldStarPoint.costG > newStarPoint.costG) {
            this.remove(oldStarPoint.systemId64)
            this[newStarPoint.systemId64] = newStarPoint
            replaces = replaces.plus(1)
        }
    }
}

@Synchronized
fun HashMap<Long, StarPoint>.smartAdd2(newStarPoint: StarPoint) {
    if (this.notContains(newStarPoint.systemId64)) {
        this[newStarPoint.systemId64] = newStarPoint
    } else {
        val oldStarPoint = this[newStarPoint.systemId64]!!
//        println("${consoleStringCounter()} Old star point jumps -> ${oldStarPoint.jumpCounter} vs ${newStarPoint.jumpCounter} <- new star point jumps")
        if (oldStarPoint.jumpCounter > newStarPoint.jumpCounter) {
            this.remove(oldStarPoint.systemId64)
            this[newStarPoint.systemId64] = newStarPoint
            replaces = replaces.plus(1)
        }
    }
}

fun <K, V> HashMap<K, V>.notContains(key: K) = !this.contains(key)