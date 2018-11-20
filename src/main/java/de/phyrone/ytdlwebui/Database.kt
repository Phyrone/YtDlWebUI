package de.phyrone.ytdlwebui

import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.nio.charset.StandardCharsets

object DB {
    init {


    }

    fun openDatabase() {
        val type = config[DatabaseSel.type]
        val username = config[DatabaseSel.username]
        val password = config[DatabaseSel.password]
        val path =
                if (config[DatabaseSel.dbPath].isNotBlank()) {
                    config[DatabaseSel.dbPath]
                } else {
                    "./" + config[DatabaseSel.databaseName]
                }
        val url =
                if (config[DatabaseSel.url].isNotBlank()) {
                    config[DatabaseSel.url]
                } else {
                    type.urlTemplate
                            .replace("%path", path, true)
                            .replace("%host", config[DatabaseSel.host], true)
                            .replace("%port", config[DatabaseSel.port].toString(), true)
                            .replace("%database", config[DatabaseSel.databaseName], true)
                            .replace("%user", config[DatabaseSel.username], true)
                            .replace("%password", config[DatabaseSel.host], true)

                }
        when {
            type.hikari -> {
                val cfg = HikariConfig()
                cfg.jdbcUrl = url
                cfg.driverClassName = type.driver
                if (config[DatabaseSel.authEnabled]) {
                    cfg.username = username
                    cfg.password = password
                }
                val dataSource = HikariDataSource(cfg)
                Database.connect(dataSource)
                Runtime.getRuntime().addShutdownHook(Thread {
                    dataSource.close()
                })
            }
            config[DatabaseSel.authEnabled] -> Database.connect(url, type.driver, username, password)
            else -> Database.connect(url, type.driver)
        }
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(
                    UsersTable
            )
        }

    }
}

enum class DatabaseType(val urlTemplate: String, val driver: String, val hikari: Boolean = false) {
    H2("jdbc:h2:%path", "org.h2.Driver"),
    MYSQL("jdbc:mysql:", "", true)
}

object UsersTable : Table("Users") {
    val id = integer("ID").primaryKey().autoIncrement()
    val username = varchar("Username", 512).uniqueIndex()
    val password = varchar("Password", 128)
    val salt = varchar("Salt", 128)
}

fun main(args: Array<String>) {
    val hashed = Hashing.sha512().hashString("DwdadadsaawAWdwa", StandardCharsets.UTF_8)
    println("String: " + hashed.toString() + " - " + hashed.toString().length)
    val enc = BaseEncoding.base64().encode(hashed.asBytes()).toLowerCase()
    println("Base: $enc - ${enc.length}")
}