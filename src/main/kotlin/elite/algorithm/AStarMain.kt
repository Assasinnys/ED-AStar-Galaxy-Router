package elite.algorithm

import elite.database.Database
import elite.database.Database.Companion.C_ID64
import elite.database.Database.Companion.C_SUBTYPE
import elite.database.Database.Companion.C_SYS_NAME
import elite.database.Database.Companion.C_X
import elite.database.Database.Companion.C_Y
import elite.database.Database.Companion.C_Z
import elite.pojo.Coordinates
import elite.replaces
import elite.utils.*
import java.sql.ResultSet
import java.util.concurrent.Executors

//TODO check for neutron as a second star -> [future 0%]
//TODO check neighbors StarPoint's for better way (lower cost) [ready 50%]

class AStarMain(private val startSystem: String, private val finishSystem: String) {
    private val database = Database()
    private val finishStarPoint: StarPoint = createFinishStarPoint()
    private val startStarPoint: StarPoint = createStartStarPoint()


    private val openedList = hashMapOf<Long, StarPoint>()
    private val closedList = hashMapOf<Long, StarPoint>()
    private val stopwatch = Stopwatch()

    private val threadPool = Executors.newSingleThreadExecutor()

    init {
        openedList[startStarPoint.systemId64] = startStarPoint
//        println("startStarPoint=${startStarPoint.systemId64}")
//        println("finishStarPoint=${finishStarPoint.systemId64}")
    }

    fun activateAStarAlgorithm() {

        multithreatingFindNeighbours(startStarPoint, 1)

        openedList.remove(startStarPoint.systemId64)
        closedList[startStarPoint.systemId64] = startStarPoint

        if (openedList.isEmpty()) {
            println("${consoleStringCounter()}Unable to complete task. No neighbors found near startStarPoint. =(")
            return
        }

        do {

            if (checkForFinish()) {
                println("${consoleStringCounter()} Finish found.")
                printTheFoundPath()
                return
            }

            val selectedStarPoint = findStarPointWithMinCost()
            multithreatingFindNeighbours(selectedStarPoint, 1)
            openedList.remove(selectedStarPoint.systemId64)
            closedList[selectedStarPoint.systemId64] = selectedStarPoint

        } while (openedList.isNotEmpty())

        println("${consoleStringCounter()} Unable to complete task. No neighbors found during searching process. =(")
        return
    }

    private fun checkForFinish(): Boolean {
        if (openedList.containsKey(finishStarPoint.systemId64)) {
            finishStarPoint.previousStarPoint = openedList[finishStarPoint.systemId64]
            return true
        }
        return false
    }

    private fun findStarPointWithMinCost(): StarPoint {
        stopwatch.start()
        return openedList.minBy {
            it.value.costF
        }!!.value.also {
            println(
                "Min cost star point: G = ${it.costG}, F = ${it.costF}, " +
                        "dist = ${it.distance}, start = ${it.previousStarPoint == startStarPoint}"
            )
            stopwatch.stopWithConsoleOutput("Min cost find time: ")
        }
    }

    private fun multithreatingFindNeighbours(starPoint: StarPoint, jumpModifier: Int) {
        val maxRange = isNeutronDistance(starPoint.isNeutronStar)
        val sql = "select $C_ID64, $C_X, $C_Y, $C_Z, $C_SUBTYPE = 'Neutron Star' as isNeutronStar, " +
                "$C_SYS_NAME, " +
                "sqrt((${starPoint.coords.x}-x)^2+(${starPoint.coords.y}-y)^2+(${starPoint.coords.z}-z)^2) as dist\n" +
                "from $CORRIDOR\n" +
                "where sqrt((${starPoint.coords.x}-x)^2+(${starPoint.coords.y}-y)^2+(${starPoint.coords.z}-z)^2) " +
                "between 0 and ${maxRange.plus(jumpModifier.minus(1).times(USUAL_DISTANCE))}" +
                "and not $C_ID64=${starPoint.systemId64}"

//        threadPool.submit {
            findNeighbours(starPoint, sql, jumpModifier)
//        }.get()
    }

    private fun findNeighbours(starPoint: StarPoint, sql: String, jumpModifier: Int) {
        checkConnection()
        var isNoResult = true

        val sw = Stopwatch().apply { start() }
        val resultSet = database.query(sql)

        while (resultSet.next()) {
            with(resultSet) {
                val isNeutronStar = getBoolean("isNeutronStar")
                val coords = Coordinates(getDouble(C_X), getDouble(C_Y), getDouble(C_Z))

                if (isNeutronStar || coords == finishStarPoint.coords) {
                    val newStarPoint = StarPoint(
                        starPoint, getLong(C_ID64), coords,
                        isNeutronStar, getDouble("dist"),
                        getString(C_SYS_NAME), starPoint.jumpCounter.plus(jumpModifier), finishStarPoint.coords
                    )
                    if (closedList.notContains(newStarPoint.systemId64)) {
                        openedList.smartAdd2(newStarPoint)
                        isNoResult = false
                    }
                }
            }
        }
        resultSet.close()
        sw.stopWithConsoleOutput("Process time: ")

        if (isNoResult) {
            multithreatingFindNeighbours(starPoint, jumpModifier.plus(1))
        }
    }

    private fun isNeutronDistance(isNeutron: Boolean) = if (isNeutron) NEUTRON_DISTANCE else USUAL_DISTANCE

    private fun checkConnection() {
        if (!database.isConnected)
            database.openConnection()
    }

    private fun createFinishStarPoint(): StarPoint {
        checkConnection()
        val result = database.query(
            "SELECT $C_X, $C_Y, $C_Z, $C_ID64, $C_SUBTYPE = 'Neutron Star' as isNeutronStar " +
                    "FROM $CORRIDOR WHERE $C_SYS_NAME='${finishSystem}'"
        ).also { it.next() }
        val coords = Coordinates(result.getDouble(C_X), result.getDouble(C_Y), result.getDouble(C_Z))

        return StarPoint(
            null, result.getLong(C_ID64), coords, result.getBoolean("isNeutronStar")
            , 0.0, finishSystem, 0, coords
        ).also {
            result.close()
        }
    }

    private fun createStartStarPoint(): StarPoint {
        checkConnection()
        val result = database.query(
            "SELECT $C_X, $C_Y, $C_Z, $C_ID64, $C_SUBTYPE = 'Neutron Star' as isNeutronStar " +
                    "FROM $CORRIDOR WHERE $C_SYS_NAME='$startSystem'"
        )

        return if (result.fetchSize > 1)
            createBetterStarPoint(null, result)
        else {
            result.next()
            StarPoint(
                null, result.getLong(C_ID64), Coordinates(
                    result.getDouble(C_X), result.getDouble(C_Y),
                    result.getDouble(C_Z)
                ), result.getBoolean("isNeutronStar"), 0.0, startSystem, 0, finishStarPoint.coords
            )
        }
    }

    private fun isNeutronStar(subtype: String) = subtype == "Neutron Star"

    private fun createBetterStarPoint(previousStarPoint: StarPoint?, result: ResultSet): StarPoint {
        val list = mutableListOf<TempPoint>()
        while (result.next()) {
            list.add(
                TempPoint(
                    result.getBoolean("isNeutronStar"),
                    result.getLong(C_ID64),
                    Coordinates(result.getDouble(C_X), result.getDouble(C_Y), result.getDouble(C_Z))
                )
            )
        }
        list.forEach { point ->
            if (point.isNeutronStar) {
                return StarPoint(
                    previousStarPoint,
                    point.id64,
                    point.coords,
                    true,
                    0.0,
                    startSystem,
                    0,
                    finishStarPoint.coords
                )
            }
        }
        return StarPoint(
            previousStarPoint,
            list[0].id64,
            list[0].coords,
            false,
            0.0,
            startSystem,
            0,
            finishStarPoint.coords
        )
    }

    private fun printTheFoundPath(): Int {
        database.closeDB()
        threadPool.shutdownNow()
        var starPoint = finishStarPoint.previousStarPoint!!
        var counter = 0
        var fullDistance = 0.0
        while (starPoint != startStarPoint) {
            println(
                "${consoleStringCounter()} Point $counter id = ${starPoint.systemId64}, " +
                        "name = ${starPoint.systemName} " +
                        "distance = ${starPoint.distance}"
            )
            fullDistance += starPoint.distance
            starPoint = starPoint.previousStarPoint!!
            counter++
        }
        println("${consoleStringCounter()} Total jumps counter = ${finishStarPoint.previousStarPoint?.jumpCounter}, distance = $fullDistance ly, replaces = $replaces, cof = ${StarPoint.NEUTRON_COF}")
        return counter
    }

    class TempPoint(
        val isNeutronStar: Boolean,
        val id64: Long,
        val coords: Coordinates
    )

    companion object {
        const val CORRIDOR = "coridor3"
        const val NEUTRON_DISTANCE = 240
        const val USUAL_DISTANCE = 60
    }
}