package bff.routes

import bff.clients.ServiceClients
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Configura todas as rotas REST do BFF */
fun Application.configureRoutes(serviceClients: ServiceClients) {
    routing {
        // Health check
        get("/health") { call.respond(mapOf("status" to "healthy")) }

        // Rotas de usuários
        usersRoutes(serviceClients.users)

        // Rotas de eventos
        eventsRoutes(serviceClients.events)

        // Rotas de parceiros
        partnersRoutes(serviceClients.partners)

        // Rotas de ingressos
        ticketsRoutes(serviceClients.tickets)

        // Rotas de pedidos e ingressos emitidos
        ordersRoutes(serviceClients.orders)

        // Rotas de reservas
        reservationsRoutes(serviceClients.reservations)
    }
}
