package bff.routes

import bff.clients.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.exceptions.NotFoundException

/** Rotas REST para o domínio de Tickets */
fun Route.ticketsRoutes(ticketsClient: ITicketsClient) {
    route("/api/ticket-types") {

        // POST /api/ticket-types - Criar tipo de ingresso
        post {
            val partnerId =
                    call.request.headers["X-Partner-Id"]
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Partner ID is required")
                            )
            val request = call.receive<CreateTicketTypeRequest>()
            val ticketType = ticketsClient.createTicketType(partnerId, request)
            call.respond(HttpStatusCode.Created, ticketType)
        }

        // GET /api/ticket-types/event/{eventId} - Listar por evento
        get("/event/{eventId}") {
            val eventId =
                    call.parameters["eventId"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Event ID is required")
                            )
            val ticketTypes = ticketsClient.listTicketTypesByEvent(eventId)
            call.respond(ticketTypes)
        }

        // POST /api/ticket-types/reserve - Reservar ingressos
        post("/reserve") {
            val request = call.receive<ReserveTicketsRequest>()
            val response = ticketsClient.reserveTickets(request)
            call.respond(response)
        }

        // POST /api/ticket-types/release - Liberar ingressos
        post("/release") {
            val request = call.receive<ReleaseTicketsRequest>()
            ticketsClient.releaseTickets(request)
            call.respond(HttpStatusCode.OK, mapOf("message" to "Tickets released successfully"))
        }

        // GET /api/ticket-types/{id} - Buscar por ID
        get("/{id}") {
            val id =
                    call.parameters["id"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Ticket type ID is required")
                            )
            val ticketType =
                    ticketsClient.getTicketTypeById(id)
                            ?: throw NotFoundException("Ticket type not found with id: $id")
            call.respond(ticketType)
        }

        // PUT /api/ticket-types/{id} - Atualizar tipo de ingresso
        put("/{id}") {
            val id =
                    call.parameters["id"]
                            ?: return@put call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Ticket type ID is required")
                            )
            val request = call.receive<UpdateTicketTypeRequest>()
            val ticketType = ticketsClient.updateTicketType(id, request)
            call.respond(ticketType)
        }

        // DELETE /api/ticket-types/{id} - Desativar tipo de ingresso
        delete("/{id}") {
            val id =
                    call.parameters["id"]
                            ?: return@delete call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Ticket type ID is required")
                            )
            ticketsClient.deactivateTicketType(id)
            call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Ticket type deactivated successfully")
            )
        }
    }
}
