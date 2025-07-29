# Todo MCP Server Development Guidelines

## Project Overview

This is a Kotlin-based Model Context Protocol (MCP) server implementing a todo management system. The project consists of two modules:
- `todo-mcp-server`: The main MCP server with todo functionality
- `todo-mcp-client`: A basic client module (currently placeholder)

## Build/Configuration Instructions

### Prerequisites
- JVM 23 (configured via Gradle toolchain)
- Gradle 8.13+ (wrapper included)

### Technology Stack
- **Kotlin**: 2.2.0
- **MCP SDK**: 0.6.0 (io.modelcontextprotocol:kotlin-sdk)
- **Database**: SQLite 3.46.1.3 with Exposed ORM 1.0.0-beta-4
- **Networking**: Ktor 3.2.2
- **Testing**: JUnit 5.10.0 (currently has compatibility issues)
- **Logging**: SLF4J 2.0.9

### Build Commands

```bash
# Compile and assemble (recommended - avoids test issues)
./gradlew :todo-mcp-server:assemble

# Run the server
./gradlew :todo-mcp-server:run

# Build the server
./gradlew :todo-mcp-server:build

# Create fat JAR with shadow plugin
./gradlew :todo-mcp-server:shadowJar
```

### Known Build Issues

**JUnit Test Task Issue**: The project currently has a compatibility issue with JUnit test task creation:
```
Could not create task of type 'Test'
```

**Workaround**: Use `assemble` instead of `build` to compile without running tests.

**JVM Warnings**: You may see warnings about restricted method access:
```
WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader
```
These are harmless but can be suppressed with `--enable-native-access=ALL-UNNAMED`.

### Database Configuration

- **Database Type**: SQLite
- **Location**: System temp directory (`java.io.tmpdir/todos.db`)
- **Auto-initialization**: Database and tables are created automatically on server startup
- **ORM**: Uses Jetbrains Exposed with auto-incrementing integer IDs

## Testing Information

### Current Testing Approach

Testing is performed using **Python scripts** that communicate with the MCP server via JSON-RPC 2.0 protocol over stdin/stdout.

### Test Files

1. **`simple_test.py`**: Basic manual test with server output monitoring
2. **`test_todo_operations.py`**: Comprehensive test covering all CRUD operations
3. **`test_simple.py`**: Simplified test script (created for debugging)

### Available MCP Tools

- `add_todo`: Add new todo item (requires: title, optional: description)
- `list_todos`: List all todos (no parameters)
- `mark_done_todo`: Mark todo as completed (requires: id)
- `delete_todo`: Delete todo item (requires: id)

### Creating New Tests

1. Start server process: `./gradlew :todo-mcp-server:run --quiet`
2. Send initialization request first
3. Use `tools/call` method for tool invocations
4. Handle server process lifecycle (terminate after tests)

**Example Tool Call**:
```json
{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
        "name": "add_todo",
        "arguments": {
            "title": "Test Todo",
            "description": "Optional description"
        }
    }
}
```

## Code Style and Architecture

### Package Structure

```
com.lizz.server/
├── ServerMain.kt           # Application entry point
├── database/
│   └── DatabaseConfig.kt   # Database initialization
├── model/
│   └── Todo.kt            # Data models and table definitions
├── repository/
│   └── TodoRepository.kt   # Data access layer
└── tools/
    ├── TodoTool.kt        # Tool interface
    ├── AddTodoTool.kt     # Add todo implementation
    ├── DeleteTodoTool.kt  # Delete todo implementation
    ├── ListTodosTool.kt   # List todos implementation
    └── MarkDoneTodoTool.kt # Mark done implementation
```

### Architectural Patterns

#### 1. **Singleton Objects**
Use `object` for stateless components:
```kotlin
object DatabaseConfig {
    fun init() { /* ... */ }
}

object TodoRepository {
    fun addTodo(title: String, description: String? = null): Todo { /* ... */ }
}
```

#### 2. **Interface-Based Tools**
All MCP tools implement `TodoTool` interface:
```kotlin
interface TodoTool {
    val name: String
    val description: String
    val inputSchema: Tool.Input?
    suspend fun execute(request: CallToolRequest): CallToolResult
    fun register(server: Server)
}
```

#### 3. **Data Classes and Table Separation**
Separate database table definitions from data classes:
```kotlin
// Table definition
object Todos : IntIdTable() {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    // ...
}

// Data class
data class Todo(
    val id: Int,
    val title: String,
    val description: String?,
    // ...
)
```

#### 4. **Transaction Wrapping**
All database operations wrapped in transactions:
```kotlin
fun addTodo(title: String, description: String? = null): Todo {
    return transaction {
        // Database operations here
    }
}
```

### Code Style Guidelines

#### Naming Conventions
- **Classes**: PascalCase (`AddTodoTool`, `TodoRepository`)
- **Functions**: camelCase (`addTodo`, `markTodoDone`)
- **Properties**: camelCase (`isDone`, `createdAt`)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: lowercase with dots (`com.lizz.server.tools`)

#### Function Naming
- Use descriptive names: `markTodoDone()` instead of `mark()`
- Boolean functions: `isDone`, `hasDescription`
- Repository methods: `addTodo`, `deleteTodo`, `getTodoById`

#### Error Handling
- Return `CallToolResult` with error messages for invalid parameters
- Use nullable returns for optional data (`getTodoById(): Todo?`)
- Validate required parameters explicitly

#### JSON Schema Definition
Use `buildJsonObject` for tool input schemas:
```kotlin
override val inputSchema = Tool.Input(
    properties = buildJsonObject {
        putJsonObject("title") {
            put("type", "string")
            put("description", "Title of the todo item")
        }
    },
    required = listOf("title")
)
```

### Development Best Practices

1. **Database Operations**: Always wrap in `transaction { }` blocks
2. **Timestamp Management**: Use `LocalDateTime.now()` for created/updated timestamps
3. **Parameter Validation**: Check required parameters before processing
4. **Tool Registration**: Use the `register()` method pattern for consistent tool setup
5. **Response Formatting**: Use `TextContent` for user-friendly messages
6. **Nullable Handling**: Use safe calls and explicit null checks

### Adding New Tools

1. Create new class implementing `TodoTool`
2. Define `name`, `description`, and `inputSchema`
3. Implement `execute()` method with parameter validation
4. Register tool in `ServerMain.kt`
5. Add corresponding repository method if needed
6. Create Python test for the new functionality

### Debugging Tips

- Server uses stdio transport - check stdin/stdout communication
- Database file location: `System.getProperty("java.io.tmpdir") + "/todos.db"`
- Add logging with SLF4J if needed
- Use `--quiet` flag with Gradle to reduce build output noise
- Test individual tools using Python scripts before integration testing