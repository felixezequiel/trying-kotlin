package bff.routes

import bff.clients.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.exceptions.NotFoundException

/** Rotas REST para o domínio de Reservations */
fun Route.reservationsRoutes(reservationsClient: IReservationsClient) {
    route("/api/reservations") {

        // POST /api/reservations - Criar reserva
        post {
            val request = call.receive<CreateReservationRequest>()
            val reservation = reservationsClient.createReservation(request)
            call.respond(HttpStatusCode.Created, reservation)
        }

        // GET /api/reservations/me - Listar minhas reservas
        get("/me") {
            val customerId =
                    call.request.headers["X-Customer-Id"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Customer ID header is required")
                            )
            val reservations = reservationsClient.listMyReservations(customerId)
            call.respond(reservations)
        }

        // GET /api/reservations/event/{eventId} - Listar reservas do evento
        get("/event/{eventId}") {
            val eventId =
                    call.parameters["eventId"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Event ID is required")
                            )
            val reservations = reservationsClient.listEventReservations(eventId)
            call.respond(reservations)
        }

        // GET /api/reservations/{id} - Buscar reserva por ID
        get("/{id}") {
            val id =
                    call.parameters["id"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Reservation ID is required")
                            )
            val reservation =
                    reservationsClient.getReservationById(id)
                            ?: throw NotFoundException("Reservation not found with id: $id")
            call.respond(reservation)
        }

        // POST /api/reservations/{id}/cancel - Cancelar reserva
        post("/{id}/cancel") {
            val id =
                    call.parameters["id"]
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Reservation ID is required")
                            )
            val reservation = reservationsClient.cancelReservation(id)
            call.respond(reservation)
        }

        // POST /api/reservations/{id}/convert - Converter reserva em pedido
        post("/{id}/convert") {
            val id =
                    call.parameters["id"]
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Reservation ID is required")
                            )
            val reservation = reservationsClient.convertReservation(id)
            call.respond(reservation)
        }
    }
}
