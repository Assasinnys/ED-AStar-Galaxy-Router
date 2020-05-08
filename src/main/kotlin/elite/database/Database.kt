package elite.database

import elite.utils.consoleStringCounter
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class Database {
    private lateinit var connection: Connection
    private lateinit var statement: Statement
    var isConnected = false
        private set

    init {
        Class.forName("org.postgresql.Driver")
    }

    fun openConnection() {
        if (isConnected) return

        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/elite2", LOGIN, PASS)
        connection.autoCommit = false
        println("${consoleStringCounter()} Database opened successfully.")

        statement = connection.createStatement()
        isConnected = true
        println("${consoleStringCounter()} Statement created.")
    }

    fun update(sql: String) {
        if (isConnected) {
            statement.executeUpdate(sql)
            println("${consoleStringCounter()} Update successful.")
        } else
            println("${consoleStringCounter()} Not connected to DB")
    }

    fun commitChanges() {
        connection.commit()
        println("${consoleStringCounter()} Changes committed.")
    }

    fun query(sql: String): ResultSet {
        if (!isConnected) throw Exception("Not connected to DB")

        return statement.executeQuery(sql).also {
            println("${consoleStringCounter()} Query successful.")
        }
    }

    fun closeDB() {
        if (isConnected) {
            statement.close()
            connection.close()
            isConnected = false
            println("${consoleStringCounter()} Database closed.")
        }
    }

    companion object {
        const val LOGIN = "postgres"
        const val PASS = ""
        const val TABLE_MAIN = "main"
        const val C_SYS_NAME = "systemname"
        const val C_BODY_NAME = "bodyname"
        const val C_SUBTYPE = "subtype"
        const val C_DTA = "dta"
        const val C_IS_SCOOPABLE = "isscoopable"
        const val C_X = "x"
        const val C_Y = "y"
        const val C_Z = "z"
        const val C_ID64 = "id64"
    }
}