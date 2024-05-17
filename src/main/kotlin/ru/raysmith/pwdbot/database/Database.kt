package ru.raysmith.pwdbot.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.raysmith.pwdbot.Environment
import ru.raysmith.utils.properties.PropertiesFactory

const val COLLATE_UTF8MB4_UNICODE_CI = "utf8mb4_unicode_ci"
const val COLLATE_UTF8_UNICODE_CI = "utf8_unicode_ci"

object Database {
    val logger: Logger = LoggerFactory.getLogger("database")
    
    val tables: MutableList<Table> = mutableListOf(
        Users, MessagesToDelete, UsersMessagesToDelete
    )

    fun init() {
        val properties = PropertiesFactory.from("db.properties")
        val portString = Environment.databasePort?.let { ":$it" } ?: ""
        val jdbc = StringBuilder("jdbc:mysql://${Environment.databaseHost}$portString/${Environment.databaseName}?")

        properties.forEach { key, value ->
            jdbc.append("$key=$value&")
        }
        jdbc.deleteCharAt(jdbc.lastIndex)

        val config = HikariConfig().apply {
            jdbcUrl         = jdbc.toString()
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username        = Environment.databaseUser
            password        = Environment.databasePass
            maximumPoolSize = Runtime.getRuntime().availableProcessors()
            transactionIsolation = IsolationLevel.TRANSACTION_SERIALIZABLE.name
            PropertiesFactory.from("hikari.properties").forEach { key, value ->
                addDataSourceProperty(key as String, value)
            }
        }
        
        Database.connect(HikariDataSource(config)).also {
            transaction {
                validateSchema()
            }
        }
    }

    /** Create tables and execute migration if needed */
    fun validateSchema() {
        SchemaUtils.create(*tables.toTypedArray())
        SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray(), withLogs = false)
        SchemaUtils.addMissingColumnsStatements(*tables.toTypedArray(), withLogs = false)
    }
}
