package elite

import elite.algorithm.AStarMain
import elite.algorithm.StarPoint
import elite.algorithm.StarPoint.Companion.DISTANCE_MODIFIER
import elite.alternative.AStarMainFile
import elite.database.Database
import elite.database.Database.Companion.C_DTA
import elite.database.Database.Companion.C_ID64
import elite.database.Database.Companion.C_SUBTYPE
import elite.database.Database.Companion.C_SYS_NAME
import elite.database.Database.Companion.C_X
import elite.database.Database.Companion.C_Y
import elite.database.Database.Companion.C_Z
import elite.database.Database.Companion.TABLE_MAIN
import elite.pojo.Coordinates
import elite.utils.FILENAME
import javazoom.jl.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Executors

@Volatile
var id: Int = 0
var replaces = 0
//val resultList = mutableListOf<Pair<Int, Int>>()

/*fun mains(args: Array<String>) {
    println("Start!")
    val timeStartProgram = System.currentTimeMillis()

    *//*val stream: Stream<String> = Files.lines(Paths.get("realGrepedAndParsedV2.json"))
    stream.forEach {
        println("LINE: $it")
    }*//*


    val database = Database()
    database.openConnection()

    database.query(
        "with coords as (select x,y,z\n" +
                "    from main\n" +
                "    where bodyname='Colonia')\n" +
                "\n" +
                "select *, sqrt(((select x from coords)-x)^2+((select y from coords)-y)^2+((select z from coords)-z)^2) as dist\n" +
                "from main \n" +
                "where\n" +
                "sqrt(((select x from coords)-x)^2+((select y from coords)-y)^2+((select z from coords)-z)^2) between 0 and 200 \n" +
                "and dta <= 100 "
    )

    println("\nTask complete! Time spent: ${calcTime(timeStartProgram)} sec.")
    val s = Scanner(System.`in`)
    s.nextLine()
    s.close()
    database.closeDB()
    println("Thread stopped")
}*/

fun calcTime(timeStart: Long): Double = System.currentTimeMillis().minus(timeStart).div(1000.0)

/*fun startCoroutineQuery(systemName: String) {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    coroutineScope.launch {
        val startCoroutineTime = System.currentTimeMillis()
        val database = Database()
        database.openConnection()
        database.query(
            "with coords as (select x,y,z\n" +
                    "    from main\n" +
                    "    where bodyname='${systemName}')\n" +
                    "\n" +
                    "select *, sqrt(((select x from coords)-x)^2+((select y from coords)-y)^2+((select z from coords)-z)^2) as dist\n" +
                    "from main \n" +
                    "where\n" +
                    "sqrt(((select x from coords)-x)^2+((select y from coords)-y)^2+((select z from coords)-z)^2) between 0 and 200 \n" +
                    "and dta <= 100 "
        )
        println("Coroutine complete query ${systemName}. Time: ${calcTime(startCoroutineTime)}")
        database.closeDB()
    }
}*/


/*fun addToResultList(result: Int) {
    resultList.add(StarPoint.NEUTRON_COF to result)
}*/

/*fun addToResultList(result: Pair<Int, Int>) {
    resultList.add(result)
}*/

/*    val cofList = arrayListOf(300, 320, 340, 380, 400, 420, 460)
    val executor = Executors.newFixedThreadPool(6)
    cofList.forEach {
        executor.execute {
            val algorithm = AStarMain(START, FINISH, it)
            println("Starting algorithm... ${Thread.currentThread().name}")
            addToResultList(algorithm.activateAStarAlgorithm())
        }
    }
    executor.shutdown()
    Scanner(System.`in`).nextLine()
    resultList.forEach {
        println("COF = ${it.first}, jumps = ${it.second}.")
    }

    val algorithm = AStarMain("Jackson''s Lighthouse", "Prooe Drye AM-D d12-6")
    println("Starting algorithm...")
    algorithm.activateAStarAlgorithm()*/


/*fun main(args: Array<String>) {
    println("Start program!")
    val timeStartProgram = System.currentTimeMillis()

    val database = Database().also { it.openConnection() }
    val systems = mutableListOf<DBNote>()
    val resultSet = database.query("SELECT id64, x, y, z, subtype = 'Neutron Star' as isNeutron FROM coridor2 where dta <= 100")
    while (resultSet.next()) {
        val system = DBNote(
            resultSet.getLong(C_ID64),
            resultSet.getBoolean("isNeutron"),
            Coordinates(
                resultSet.getDouble(C_X),
                resultSet.getDouble(C_Y),
                resultSet.getDouble(C_Z)
            )
        )
        systems.add(system)
    }
    resultSet.close()
    database.closeDB()

    println("Num of systems is ${systems.size}.")

    println("Work complete. Time spent ${calcTime(timeStartProgram)}")
}*/

//class DBNote(val id64: Long, val isNeutronStar: Boolean, val coordinates: Coordinates)


//TODO Write table from db to file
/*fun main(args: Array<String>) {
    println("Starting task")
    val database = Database().also { it.openConnection() }
    val file = File("FILENAME")
    file.createNewFile()
    val writer = file.printWriter()
    val resultSet = database.query("select $C_ID64, $C_X, $C_Y, $C_Z, $C_SUBTYPE = 'Neutron Star' as isNeutronStar, " +
                    "$C_SYS_NAME from coridor4 where dta = 0")
    while (resultSet.next()) {
        writer.println("${resultSet.getLong(C_ID64)} ${resultSet.getLong(C_X)} ${resultSet.getLong(C_Y)} " +
                "${resultSet.getLong(C_Z)} ${resultSet.getBoolean("isNeutronStar")} " +
                "\"${resultSet.getString(C_SYS_NAME)}\"")
    }
    println("Task complete")
}*/

//TODO test reading speed
/*fun readFile() {
    var timeStart = System.currentTimeMillis()
    val file = File(FILENAME)
//    val byteArray = Files.readAllBytes(file.toPath()) // better performance
    val stream = Files.lines(file.toPath())
    println("Reading time: ${calcTime(timeStart)}")
    timeStart = System.currentTimeMillis()
//    val str = String(byteArray, Charsets.UTF_8)
//    println(str)
    println("lines = ${stream.count()}")
    println("Create string: ${calcTime(timeStart)}")
}*/

//TODO alternative MAIN with work in file
fun main(args: Array<String>) {
    println("Start A Star with file table")

    val time = System.currentTimeMillis()
    println("Distance modifier $DISTANCE_MODIFIER")
    val aStar = AStarMainFile()
    println("Starting algorithm")
    aStar.activateAStarAlgorithm()

    println("Complete with time: ${calcTime(time)}")
}

//TODO MAIN METHOD
/*fun main(args: Array<String>) {
    println("Start!")
    val timeStartProgram = System.currentTimeMillis()

    println("Distance modifier = $DISTANCE_MODIFIER")

    val algorithm = AStarMain(START, FINISH)
    println("Starting algorithm...")
    algorithm.activateAStarAlgorithm()

    println("Work complete. Time spent ${calcTime(timeStartProgram)}")
}*/

// systems
const val SOL = "Sol"
const val COLONIA = "Colonia"
const val SYSTEM3K = "Prooe Drye AM-D d12-6"
const val JACKSON = "Jackson''s Lighthouse"
const val START = JACKSON
const val FINISH = SYSTEM3K


























