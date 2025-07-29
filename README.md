# Todo MCP Server

A Kotlin-based Model Context Protocol (MCP) server that provides todo management functionality for AI assistants like Claude.

## Prerequisites

- JVM 23
- Gradle 8.13+ (wrapper included)

## Quick Start

### Build the Server

```bash
# Create the executable JAR
./gradlew :todo-mcp-server:shadowJar
or
./gradlew :todo-mcp-server:build
```

### Run the Server

```bash
# Run directly with Gradle
./gradlew :todo-mcp-server:run

# Or run the JAR file
java -jar todo-mcp-server/build/libs/todo-mcp-server-1.0-SNAPSHOT-all.jar
```

## Claude Code Integration

Add this configuration to your Claude Code settings:

```json
{
  "your-todo-mcp": {
    "command": "java",
    "args": [
      "-jar",
      "/path/to/your/project/todo-mcp-server/build/libs/todo-mcp-server-1.0-SNAPSHOT-all.jar"
    ]
  }
}
```

Replace `/path/to/your/project/` with the actual path to your project directory.

## Available Tools

- `add_todo` - Add a new todo item
- `list_todos` - List all todos
- `mark_done_todo` - Mark a todo as completed
- `delete_todo` - Delete a todo item

## Database

The server uses SQLite and automatically creates a database file in your system's temp directory (`todos.db`).