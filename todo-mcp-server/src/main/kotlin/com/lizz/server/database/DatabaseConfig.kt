package com.lizz.server.database

import com.lizz.server.model.Todos
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils.create
import org.jetbrains.exposed.v1.jdbc.transactions.transaction


object DatabaseConfig {
    fun init() {
        // Use system temp directory to avoid read-only file system issues
        val dbPath = System.getProperty("java.io.tmpdir") + "/todos.db"
        Database.connect("jdbc:sqlite:$dbPath", "org.sqlite.JDBC")

        transaction {
            create(Todos)
        }
    }
}