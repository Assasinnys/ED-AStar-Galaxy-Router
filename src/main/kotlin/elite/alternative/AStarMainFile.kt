package elite.alternative

import elite.algorithm.StarPoint
import elite.database.Database
import elite.pojo.Coordinates
import elite.replaces
import elite.utils.*
import java.io.File
import java.sql.ResultSet
import kotlin.system.exitProcess

//TODO check for neutron as a second star
//TODO check neighbors StarPoint's for better way (lower cost) [ready 50%]

class AStarMainFile {
    private val coords = Coordinates(-2275.3125, -1140.5312, 1284.9688)
    private val finishStarPoint = StarPoint(null, 216165812715L, coords, true, 0.0, null, 0, coords)

    private val startStarPoint =
        StarPoint(null, 5532807773L, Coordinates(157.0, -27.0, -70.0), true, 0.0, null, 0, finishStarPoint.coords)

    private val file = File(FILENAME)

    private val openedList = mutableListOf<StarPoint>()
    private val closedList = mutableListOf<StarPoint>()

    init {
        openedList.add(startStarPoint)
    }

    fun activateAStarAlgorithm() {

        findNeighbours(startStarPoint)

        openedList.remove(startStarPoint)
        closedList.add(startStarPoint)

        if (openedList.isEmpty()) {
            println("${consoleStringCounter()}Unable to complete task. No neighbors found near startStarPoint. =(")
            return
        }

        do {
            val sw = Stopwatch().start()
            if (checkForFinish()) {
                println("${consoleStringCounter()} Finish found.")
                printTheFoundPath()
                return
            }
            sw.stopWithConsoleOutput("CheckForFinish time: ")
            val selectedStarPoint = findStarPointWithMinCost()
            findNeighbours(selectedStarPoint)
            openedList.remove(selectedStarPoint)
            closedList.add(selectedStarPoint)

        } while (openedList.isNotEmpty())

        println("${consoleStringCounter()} Unable to complete task. No neighbors found during searching process. =(")
    }

    private fun checkForFinish(): Boolean {
        openedList.forEach { point ->
            if (point == finishStarPoint) {
                finishStarPoint.previousStarPoint = point
                return true
            }
        }
        return false
    }

    private fun findStarPointWithMinCost(): StarPoint {
        val sw = Stopwatch().start()
        return openedList.minBy { starPoint -> starPoint.costF }!!.also { nextStarPoint ->
            sw.stopWithConsoleOutput("FIND MIN COST TIME: ")
            println(
                "Min cost star point: G = ${nextStarPoint.costG}, F = ${nextStarPoint.costF}, H = ${nextStarPoint.costH} " +
                        "dist = ${nextStarPoint.distance}, start = ${nextStarPoint.previousStarPoint == startStarPoint}"
            )
        }
    }

    private fun findNeighbours(starPoint: StarPoint) {
        val sw1 = Stopwatch().start()
        val sw2 = Stopwatch().start()
        val reader = file.bufferedReader().lines()
        sw2.stopWithConsoleOutput("READ:LINES time: ")
        reader.forEach { line ->

            val sArray = line.split(" ")
            val coords = Coordinates(
                sArray[1].toDouble(),
                sArray[2].toDouble(),
                sArray[3].toDouble()
            )
            val distance = calcDistance(
                coords.x,
                starPoint.coords.x,
                coords.y,
                starPoint.coords.y,
                coords.z,
                starPoint.coords.z
            )

            if (starPoint.isNeutronStar && distance <= NEUTRON_DISTANCE) {
                val newStarPoint = StarPoint(
                    starPoint, sArray[0].toLong(),
                    coords,
                    sArray[4].toBoolean(),
                    distance,
                    null,
                    starPoint.jumpCounter.plus(1),
                    finishStarPoint.coords
                )
                if (closedList.notContains(newStarPoint))
                    openedList.smartAdd2(newStarPoint)

            } else if (!starPoint.isNeutronStar && distance <= USUAL_DISTANCE) {
                val newStarPoint = StarPoint(
                    starPoint, sArray[0].toLong(),
                    coords,
                    sArray[4].toBoolean(),
                    distance,
                    null,
                    starPoint.jumpCounter.plus(1),
                    finishStarPoint.coords
                )

                if (closedList.notContains(newStarPoint))
                    openedList.smartAdd2(newStarPoint)
            }
        }
        reader.close()
        sw1.stopWithConsoleOutput("Find neighbors time (openListSize = ${openedList.size}): ")
    }

    private fun printTheFoundPath(): Int {
//        val db = Database().apply { openConnection() }
        var starPoint = finishStarPoint.previousStarPoint!!
        var counter = 0
        var fullDistance = 0.0
        while (starPoint != startStarPoint) {
//            val systemName = db.query("select ${Database.C_SYS_NAME} FROM ${AStarMain.CORRIDOR} WHERE ${Database.C_ID64} = ${starPoint.systemId64}").let {
//                it.next()
//                it.getString(Database.C_SYS_NAME)
//            }
            println(
                "${consoleStringCounter()} Point $counter id = ${starPoint.systemId64}, " +
//                        "name = $systemName " +
                        "distance = ${starPoint.distance}, G = ${starPoint.costG}, F = ${starPoint.costF}"
            )
            fullDistance += starPoint.distance
            starPoint = starPoint.previousStarPoint!!
            counter++
        }
//        db.closeDB()
        println("${consoleStringCounter()} Total jumps counter = $counter, distance = $fullDistance ly, replaces = $replaces, cof = ${StarPoint.NEUTRON_COF}")
        return counter
    }

    companion object {
        const val NEUTRON_DISTANCE = 240
        const val USUAL_DISTANCE = 60
    }
}