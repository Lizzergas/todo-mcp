plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "todo-mcp"

include("todo-mcp-client")
include("todo-mcp-server")