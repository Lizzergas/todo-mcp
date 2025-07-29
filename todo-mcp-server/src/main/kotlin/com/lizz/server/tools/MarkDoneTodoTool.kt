package com.lizz.server.tools

import com.lizz.server.repository.TodoRepository
import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.serialization.json.*

class MarkDoneTodoTool : TodoTool {
    override val name = "mark_done_todo"
    override val description = "Mark a todo item as done"
    override val inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("id") {
                put("type", "number")
                put("description", "ID of the todo item to mark as done")
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

        val marked = TodoRepository.markTodoDone(id)
        return if (marked) {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id marked as done.")))
        } else {
            CallToolResult(content = listOf(TextContent("Todo TODO-$id not found.")))
        }
    }
}