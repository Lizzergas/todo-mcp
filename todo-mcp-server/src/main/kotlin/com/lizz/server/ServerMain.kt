package com.lizz.server

import com.lizz.server.database.DatabaseConfig
import com.lizz.server.repository.TodoRepository
import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.*

fun main() {
    `run mcp server`()
}

fun `run mcp server`() {
    // Initialize database
    DatabaseConfig.init()

    val server = Server(
        serverInfo = Implementation(
            name = "Todo",
            version = "0.0.1",
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = true))
        )
    )

    val transport = StdioServerTransport(
        inputStream = System.`in`.asInput(),
        outputStream = System.out.asSink().buffered()
    )

    // Register add_todo tool
    server.addTool(
        name = "add_todo",
        description = "Add a new todo item",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("title") {
                    put("type", "string")
                    put("description", "Title of the todo item")
                }
                putJsonObject("description") {
                    put("type", "string")
                    put("description", "Optional description of the todo item")
                }
            },
            required = listOf("title")
        )
    ) { request ->
        val title = request.arguments["title"]?.jsonPrimitive?.content
        val description = request.arguments["description"]?.jsonPrimitive?.content

        if (title == null) {
            return@addTool CallToolResult(
                content = listOf(TextContent("The 'title' parameter is required."))
            )
        }

        val todo = TodoRepository.addTodo(title, description)
        CallToolResult(content = listOf(TextContent("Todo added: TODO-${todo.id} - ${todo.title}")))
    }

    // Register delete_todo tool
    server.addTool(
        name = "delete_todo",
        description = "Delete a todo item by ID",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("id") {
                    put("type", "number")
                    put("description", "ID of the todo item to delete")
                }
            },
            required = listOf("id")
        )
    ) { request ->
        val id = request.arguments["id"]?.jsonPrimitive?.intOrNull

        if (id == null) {
            return@addTool CallToolResult(
                content = listOf(TextContent("The 'id' parameter is required and must be a number."))
            )
        }

        val deleted = TodoRepository.deleteTodo(id)
        if (deleted) {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id deleted successfully.")))
        } else {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id not found.")))
        }
    }

    // Register mark_done_todo tool
    server.addTool(
        name = "mark_done_todo",
        description = "Mark a todo item as done",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("id") {
                    put("type", "number")
                    put("description", "ID of the todo item to mark as done")
                }
            },
            required = listOf("id")
        )
    ) { request ->
        val id = request.arguments["id"]?.jsonPrimitive?.intOrNull

        if (id == null) {
            return@addTool CallToolResult(
                content = listOf(TextContent("The 'id' parameter is required and must be a number."))
            )
        }

        val marked = TodoRepository.markTodoDone(id)
        if (marked) {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id marked as done.")))
        } else {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id not found.")))
        }
    }

    // Register list_todos tool
    server.addTool(
        name = "list_todos",
        description = "List all todo items with their IDs in JIRA-style format (TODO-1, TODO-2, etc.)"
    ) { request ->
        val todos = TodoRepository.listTodos()

        if (todos.isEmpty()) {
            CallToolResult(content = listOf(TextContent("No todos found.")))
        } else {
            val todoList = todos.map { todo ->
                val status = if (todo.isDone) "[DONE]" else "[PENDING]"
                val description = if (todo.description != null) " - ${todo.description}" else ""
                "TODO-${todo.id} $status ${todo.title}$description"
            }.joinToString("\n")

            CallToolResult(content = listOf(TextContent(todoList)))
        }
    }

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}