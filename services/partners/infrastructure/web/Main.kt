package partners.infrastructure.web

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import shared.utils.EnvConfig

fun main() {
    val port = EnvConfig.getInt("PARTNERS_SERVICE_PORT", 8082)

    println("")
    println("========================================")
    println("  Partners Service")
    println("========================================")
    println("  Server is running on port $port")
    println("  http://localhost:$port")
    println("========================================")
    println("")

    embeddedServer(Netty, port = port, host = "0.0.0.0") { configureRouting() }.start(wait = true)
}
