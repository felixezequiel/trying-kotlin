package bff.routes

import bff.clients.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.exceptions.NotFoundException

/** Rotas REST para o domínio de Events */
fun Route.eventsRoutes(eventsClient: IEventsClient) {
        route("/api/events") {

                // POST /api/events - Criar evento
                post {
                        val partnerId =
                                call.request.headers["X-Partner-Id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val request = call.receive<CreateEventRequest>()
                        val event = eventsClient.createEvent(partnerId, request)
                        call.respond(HttpStatusCode.Created, event)
                }

                // GET /api/events - Listar eventos públicos
                get {
                        val events = eventsClient.listPublicEvents()
                        call.respond(events)
                }

                // GET /api/events/admin - Listar todos eventos (admin)
                get("/admin") {
                        val status = call.request.queryParameters["status"]
                        val events = eventsClient.listEventsByStatus(status)
                        call.respond(events)
                }

                // GET /api/events/partner/{partnerId} - Listar eventos do partner
                get("/partner/{partnerId}") {
                        val partnerId =
                                call.parameters["partnerId"]
                                        ?: return@get call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val events = eventsClient.listPartnerEvents(partnerId)
                        call.respond(events)
                }

                // GET /api/events/{id} - Buscar evento por ID
                get("/{id}") {
                        val id =
                                call.parameters["id"]
                                        ?: return@get call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Event ID is required")
                                        )
                        val event =
                                eventsClient.getEventById(id)
                                        ?: throw NotFoundException("Event not found with id: $id")
                        call.respond(event)
                }

                // PUT /api/events/{id} - Atualizar evento
                put("/{id}") {
                        val id =
                                call.parameters["id"]
                                        ?: return@put call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Event ID is required")
                                        )
                        val partnerId =
                                call.request.headers["X-Partner-Id"]
                                        ?: return@put call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val request = call.receive<UpdateEventRequest>()
                        val event = eventsClient.updateEvent(partnerId, id, request)
                        call.respond(event)
                }

                // POST /api/events/{id}/publish - Publicar evento
                post("/{id}/publish") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Event ID is required")
                                        )
                        val partnerId =
                                call.request.headers["X-Partner-Id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val event = eventsClient.publishEvent(partnerId, id)
                        call.respond(event)
                }

                // POST /api/events/{id}/cancel - Cancelar evento
                post("/{id}/cancel") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Event ID is required")
                                        )
                        val partnerId = call.request.headers["X-Partner-Id"]
                        val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false
                        val event = eventsClient.cancelEvent(partnerId, id, isAdmin)
                        call.respond(event)
                }

                // POST /api/events/{id}/finish - Finalizar evento
                post("/{id}/finish") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Event ID is required")
                                        )
                        val partnerId = call.request.headers["X-Partner-Id"]
                        val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false
                        val event = eventsClient.finishEvent(partnerId, id, isAdmin)
                        call.respond(event)
                }
        }
}
