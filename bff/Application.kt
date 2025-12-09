package bff

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import io.ktor.server.response.*
import bff.routes.configureRoutes
import bff.graphql.configureGraphQL
import bff.clients.ServiceClients
import shared.exceptions.ServiceException
import shared.utils.EnvConfig
import kotlinx.serialization.json.Json

fun main() {
    val port = EnvConfig.getInt("BFF_PORT", 8080)
    
    println("")
    println("========================================")
    println("  BFF - Backend for Frontend")
    println("========================================")
    println("  Server is running on port $port")
    println("  http://localhost:$port")
    println("  GraphQL Playground: http://localhost:$port/graphql")
    println("  Health Check: http://localhost:$port/health")
    println("========================================")
    println("")
    
    embeddedServer(Netty, port = port, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Inicializa clientes dos serviços
    val serviceClients = ServiceClients()
    
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
    
    install(StatusPages) {
        exception<ServiceException> { call, cause ->
            call.respond(
                HttpStatusCode.fromValue(cause.statusCode),
                mapOf("error" to cause.message)
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }
    }
    
    // Configura rotas REST
    configureRoutes(serviceClients)
    
    // Configura GraphQL
    configureGraphQL(serviceClients)
}
