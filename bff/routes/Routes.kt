package bff.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import bff.clients.ServiceClients

/**
 * Configura todas as rotas REST do BFF
 */
fun Application.configureRoutes(serviceClients: ServiceClients) {
    routing {
        // Health check
        get("/health") {
            call.respond(mapOf("status" to "healthy"))
        }
        
        // Rotas de usuários
        usersRoutes(serviceClients.users)
        
        // Adicione novas rotas aqui conforme novos serviços forem criados
        // ordersRoutes(serviceClients.orders)
        // productsRoutes(serviceClients.products)
    }
}
