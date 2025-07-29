package com.lizz.server.tools

import com.lizz.server.repository.TodoRepository
import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.serialization.json.*

class DeleteTodoTool : TodoTool {
    override val name = "delete_todo"
    override val description = "Delete a todo item by ID"
    override val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("id") {
                put("type", "number")
                put("description", "ID of the todo item to delete")
            }
        },
        required = listOf("id")
    )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val id = request.arguments["id"]?.jsonPrimitive?.intOrNull

        if (id == null) {
            return CallToolResult(
                content = listOf(TextContent("The 'id' parameter is required and must be a number."))
            )
        }

        val deleted = TodoRepository.deleteTodo(id)
        return if (deleted) {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id deleted successfully.")))
        } else {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id not found.")))
        }
    }
}