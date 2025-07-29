package com.lizz.server.tools

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server

interface TodoTool {
    val name: String
    val description: String
    val inputSchema: Tool.Input?
    
    suspend fun execute(request: CallToolRequest): CallToolResult
    
    fun register(server: Server) {
        val schema = inputSchema
        if (schema != null) {
            server.addTool(
                name = name,
                description = description,
                inputSchema = schema
            ) { request ->
                execute(request)
            }
        } else {
            server.addTool(
                name = name,
                description = description
            ) { request ->
                execute(request)
            }
        }
    }
}