package com.lizz.server.tools

import com.lizz.server.repository.TodoRepository
import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.serialization.json.*

class AddTodoTool : TodoTool {
    override val name = "add_todo"
    override val description = "Add a new todo item"
    override val inputSchema = Tool.Input(
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

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val title = request.arguments["title"]?.jsonPrimitive?.content
        val description = request.arguments["description"]?.jsonPrimitive?.content

        if (title == null) {
            return CallToolResult(
                content = listOf(TextContent("The 'title' parameter is required."))
            )
        }

        val todo = TodoRepository.addTodo(title, description)
        return CallToolResult(content = listOf(TextContent("Todo added: TODO-${todo.id} - ${todo.title}")))
    }
}