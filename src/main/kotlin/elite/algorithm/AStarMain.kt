package elite.algorithm

import elite.algorithm.StarPoint
import elite.algorithm.StarPoint.Companion.NEUTRON_COF
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
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

//TODO check for neutron as a second star
//TODO check neighbors StarPoint's for better way (lower cost) [ready 50%]

class AStarMain(private val startSystem: String, private val finishSystem: String) {
    private val database = Database()
    private val finishStarPoint: StarPoint = createFinishStarPoint()
    private val startStarPoint: StarPoint = createStartStarPoint()


    private val openedList = mutableListOf<StarPoint>()
    private val closedList = mutableListOf<StarPoint>()
    private val stopwatch = Stopwatch()

    private val threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS)
    private val dbList = mutableListOf<Database>()

    init {
        openedList.add(startStarPoint)
        repeat(NUM_OF_THREADS) {
            dbList.add(Database().apply { openConnection() })
        }
//        println("startStarPoint=${startStarPoint.systemId64}")
//        println("finishStarPoint=${finishStarPoint.systemId64}")
    }

    fun activateAStarAlgorithm()/*: Pair<Int, Int>*/ {

//        findNeighbours(startStarPoint)
        multithreatingFindNeighbours(startStarPoint)

        if (openedList.isEmpty()) {
            println("${consoleStringCounter()}Unable to complete task. No neighbors found near startStarPoint. =(")
            return
        }

        openedList.remove(startStarPoint)
        closedList.add(startStarPoint)

        do {

            if (checkForFinish()) {
                println("${consoleStringCounter()} Finish found.")
                printTheFoundPath()
                return
            }

            val selectedStarPoint = findStarPointWithMinCost()
//            findNeighbours(selectedStarPoint)
            multithreatingFindNeighbours(selectedStarPoint)
            openedList.remove(selectedStarPoint)
            closedList.add(selectedStarPoint)

        } while (openedList.isNotEmpty())

        println("${consoleStringCounter()} Unable to complete task. No neighbors found during searching process. =(")
        return
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
        stopwatch.start()
        return openedList.minBy { starPoint -> starPoint.costF }!!.also { nextStarPoint ->
            println(
                "Min cost star point: G = ${nextStarPoint.costG}, F = ${nextStarPoint.costF}, " +
                        "dist = ${nextStarPoint.distance}, start = ${nextStarPoint.previousStarPoint == startStarPoint}"
            )
            stopwatch.stopWithConsoleOutput("Min cost find time: ")
        }
    }

    private fun multithreatingFindNeighbours(starPoint: StarPoint) {
        val range = if (starPoint.isNeutronStar)
            NEUTRON_DISTANCE.div(NUM_OF_THREADS)
        else
            USUAL_DISTANCE.div(NUM_OF_THREADS)
        val sqlList = mutableListOf<String>()
        val taskList = mutableListOf<Future<*>>()
        val maxJump = if (starPoint.isNeutronStar)
            NEUTRON_DISTANCE
        else
            USUAL_DISTANCE

        for (i in 0 until maxJump step range) {
            val sql = "select $C_ID64, $C_X, $C_Y, $C_Z, $C_SUBTYPE = 'Neutron Star' as isNeutronStar, " +
                    "$C_SYS_NAME, " +
                    "sqrt((${starPoint.coords.x}-x)^2+(${starPoint.coords.y}-y)^2+(${starPoint.coords.z}-z)^2) as dist\n" +
                    "from $CORRIDOR\n" +
                    "where sqrt((${starPoint.coords.x}-x)^2+(${starPoint.coords.y}-y)^2+(${starPoint.coords.z}-z)^2) between $i and ${i.plus(
                        range
                    )}" +
                    "and not $C_ID64=${starPoint.systemId64}"
            sqlList.add(sql)
        }

        for (i in 0 until NUM_OF_THREADS) {
            taskList.add(
                threadPool.submit {
                    findNeighbours(starPoint, sqlList[i], dbList[i])
                }
            )
        }

        taskList.forEach {
            it.get()
        }

    }

    private fun findNeighbours(starPoint: StarPoint, sql: String, db: Database) {
//        checkConnection()
        val sw = Stopwatch().apply { start() }
//        stopwatch.start()
        /*val resultSet = database.query(
            "select $C_ID64, $C_X, $C_Y, $C_Z, $C_SUBTYPE = 'Neutron Star' as isNeutronStar, " +
                    "$C_SYS_NAME, " +
                    "sqrt((${starPoint.coords.x}-x)^2+(${starPoint.coords.y}-y)^2+(${starPoint.coords.z}-z)^2) as dist\n" +
                    "from $CORRIDOR\n" +
                    "where sqrt((${starPoint.coords.x}-x)^2+(${starPoint.coords.y}-y)^2+(${starPoint.coords.z}-z)^2) <= ${isNeutronDistance(
                        starPoint.isNeutronStar
                    )}" +
                    "and not $C_ID64=${starPoint.systemId64}"
        )*/
        val resultSet = db.query(sql)
//        stopwatch.stopWithConsoleOutput("Query time: ")
//        stopwatch.start()

        while (resultSet.next()) {
            with(resultSet) {
                val newStarPoint = StarPoint(
                    starPoint, getLong(C_ID64), Coordinates(
                        getDouble(C_X), getDouble(C_Y),
                        getDouble(C_Z)
                    ), getBoolean("isNeutronStar"), getDouble("dist"),
                    getString(C_SYS_NAME), starPoint.jumpCounter.plus(1), finishStarPoint.coords
                )
                if (closedList.notContains(newStarPoint)) {
//                openedList.addIfAbsent(newStarPoint)
//                openedList.smartAdd(newStarPoint)
                    openedList.smartAdd2(newStarPoint)
                }

            }
        }
        resultSet.close()
        sw.stopWithConsoleOutput("Process time: ")
//        stopwatch.stopWithConsoleOutput("Process time: ")
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
        println("${consoleStringCounter()} Total jumps counter = $counter, distance = $fullDistance ly, replaces = $replaces, cof = ${StarPoint.NEUTRON_COF}")
        dbList.forEach { db ->
            db.closeDB()
        }
        return counter
    }

    class TempPoint(
        val isNeutronStar: Boolean,
        val id64: Long,
        val coords: Coordinates
    )

    companion object {
        const val CORRIDOR = "main"
        const val NEUTRON_DISTANCE = 240
        const val USUAL_DISTANCE = 60
        const val NUM_OF_THREADS = 3
    }
}