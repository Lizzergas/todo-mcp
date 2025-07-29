package com.lizz.server.repository

import com.lizz.server.model.Todo
import com.lizz.server.model.Todos
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

object TodoRepository {

    fun addTodo(title: String, description: String? = null): Todo {
        return transaction {
            val now = LocalDateTime.now()
            val id = Todos.insertAndGetId {
                it[Todos.title] = title
                it[Todos.description] = description
                it[isDone] = false
                it[createdAt] = now
                it[updatedAt] = now
            }

            Todo(
                id = id.value,
                title = title,
                description = description,
                isDone = false,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun deleteTodo(id: Int): Boolean {
        return transaction {
            Todos.deleteWhere { Todos.id eq id } > 0
        }
    }

    fun markTodoDone(id: Int): Boolean {
        return transaction {
            val updated = Todos.update({ Todos.id eq id }) {
                it[isDone] = true
                it[updatedAt] = LocalDateTime.now()
            }
            updated > 0
        }
    }

    fun listTodos(): List<Todo> {
        return transaction {
            Todos.selectAll().orderBy(Todos.id).map { row ->
                Todo(
                    id = row[Todos.id].value,
                    title = row[Todos.title],
                    description = row[Todos.description],
                    isDone = row[Todos.isDone],
                    createdAt = row[Todos.createdAt],
                    updatedAt = row[Todos.updatedAt]
                )
            }
        }
    }

    fun getTodoById(id: Int): Todo? {
        return transaction {
            Todos.selectAll().where { Todos.id eq id }.singleOrNull()?.let { row ->
                Todo(
                    id = row[Todos.id].value,
                    title = row[Todos.title],
                    description = row[Todos.description],
                    isDone = row[Todos.isDone],
                    createdAt = row[Todos.createdAt],
                    updatedAt = row[Todos.updatedAt]
                )
            }
        }
    }
}