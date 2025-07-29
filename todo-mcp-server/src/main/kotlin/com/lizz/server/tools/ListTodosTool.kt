package com.lizz.server.tools

import com.lizz.server.repository.TodoRepository
import io.modelcontextprotocol.kotlin.sdk.*

class ListTodosTool : TodoTool {
    override val name = "list_todos"
    override val description = "List all todo items with their IDs in JIRA-style format (TODO-1, TODO-2, etc.)"
    override val inputSchema: Tool.Input? = null

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val todos = TodoRepository.listTodos()

        return if (todos.isEmpty()) {
            CallToolResult(content = listOf(TextContent("No todos found.")))
        } else {
            val todoList = todos.joinToString("\n") { todo ->
                val status = if (todo.isDone) "[DONE]" else "[PENDING]"
                val description = if (todo.description != null) " - ${todo.description}" else ""
                "TODO-${todo.id} $status ${todo.title}$description"
            }

            CallToolResult(content = listOf(TextContent(todoList)))
        }
    }
}