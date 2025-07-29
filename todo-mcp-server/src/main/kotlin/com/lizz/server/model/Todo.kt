package com.lizz.server.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object Todos : IntIdTable() {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val isDone = bool("is_done").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

data class Todo(
    val id: Int,
    val title: String,
    val description: String?,
    val isDone: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)