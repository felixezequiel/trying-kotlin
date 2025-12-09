package reservations.adapters.inbound

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import reservations.application.dto.CancelReservationRequest
import reservations.application.dto.ConvertReservationRequest
import reservations.application.dto.CreateReservationRequest
import reservations.application.dto.ErrorResponse
import reservations.application.dto.ReservationResponse
import reservations.application.useCases.CancelReservationUseCase
import reservations.application.useCases.ConvertReservationUseCase
import reservations.application.useCases.CreateReservationUseCase
import reservations.application.useCases.GetReservationUseCase
import reservations.application.useCases.ListCustomerReservationsUseCase
import reservations.application.useCases.ListEventReservationsUseCase
import reservations.domain.CancellationType

class ReservationController(
        private val createReservationUseCase: CreateReservationUseCase,
        private val cancelReservationUseCase: CancelReservationUseCase,
        private val convertReservationUseCase: ConvertReservationUseCase,
        private val getReservationUseCase: GetReservationUseCase,
        private val listCustomerReservationsUseCase: ListCustomerReservationsUseCase,
        private val listEventReservationsUseCase: ListEventReservationsUseCase
) {

    // POST /reservations - Cria reserva (CUSTOMER)
    suspend fun createReservation(call: ApplicationCall) {
        try {
            val customerIdStr = call.request.headers["X-Customer-Id"]
            if (customerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Customer não autenticado"))
                return
            }

            val customerId =
                    try {
                        UUID.fromString(customerIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Customer ID inválido")
                        )
                        return
                    }

            val request = call.receive<CreateReservationRequest>()
            val reservation = createReservationUseCase.execute(customerId, request)
            call.respond(HttpStatusCode.Created, ReservationResponse.fromDomain(reservation))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível criar reserva")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /reservations/{id} - Busca por ID (Dono/PARTNER/ADMIN)
    suspend fun getReservationById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val reservationId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val reservation = getReservationUseCase.execute(reservationId)
            if (reservation != null) {
                // TODO: Validar permissão (dono, partner do evento, ou admin)
                call.respond(HttpStatusCode.OK, ReservationResponse.fromDomain(reservation))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Reserva não encontrada"))
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /reservations/{id}/cancel - Cancela reserva (CUSTOMER/PARTNER/ADMIN)
    suspend fun cancelReservation(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val reservationId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            // Determina quem está cancelando e o tipo de cancelamento
            val customerIdStr = call.request.headers["X-Customer-Id"]
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            val (cancelledBy, cancellationType) =
                    when {
                        isAdmin -> {
                            val adminId =
                                    call.request.headers["X-User-Id"]
                                            ?: return call.respond(
                                                    HttpStatusCode.BadRequest,
                                                    ErrorResponse("User ID é obrigatório")
                                            )
                            UUID.fromString(adminId) to CancellationType.BY_ADMIN
                        }
                        partnerIdStr != null -> {
                            UUID.fromString(partnerIdStr) to CancellationType.BY_PARTNER
                        }
                        customerIdStr != null -> {
                            UUID.fromString(customerIdStr) to CancellationType.BY_CUSTOMER
                        }
                        else -> {
                            return call.respond(
                                    HttpStatusCode.Unauthorized,
                                    ErrorResponse("Não autenticado")
                            )
                        }
                    }

            val request =
                    try {
                        call.receive<CancelReservationRequest>()
                    } catch (e: Exception) {
                        CancelReservationRequest(null)
                    }

            val reservation =
                    cancelReservationUseCase.execute(
                            reservationId = reservationId,
                            cancelledBy = cancelledBy,
                            reason = request.reason,
                            cancellationType = cancellationType
                    )

            call.respond(HttpStatusCode.OK, ReservationResponse.fromDomain(reservation))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível cancelar")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /reservations/{id}/convert - Converte em Order (Internal)
    suspend fun convertReservation(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val reservationId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request = call.receive<ConvertReservationRequest>()
            val orderId =
                    try {
                        UUID.fromString(request.orderId)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Order ID inválido"))
                        return
                    }

            val reservation = convertReservationUseCase.execute(reservationId, orderId)
            call.respond(HttpStatusCode.OK, ReservationResponse.fromDomain(reservation))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível converter")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /reservations/me - Minhas reservas (CUSTOMER)
    suspend fun listMyReservations(call: ApplicationCall) {
        try {
            val customerIdStr = call.request.headers["X-Customer-Id"]
            if (customerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Customer não autenticado"))
                return
            }

            val customerId =
                    try {
                        UUID.fromString(customerIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Customer ID inválido")
                        )
                        return
                    }

            val reservations = listCustomerReservationsUseCase.execute(customerId)
            call.respond(HttpStatusCode.OK, reservations.map { ReservationResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /reservations/event/{eventId} - Reservas do evento (PARTNER/ADMIN)
    suspend fun listEventReservations(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            if (!isAdmin && partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Não autenticado"))
                return
            }

            val eventIdStr = call.parameters["eventId"]
            if (eventIdStr == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Event ID é obrigatório"))
                return
            }

            val eventId =
                    try {
                        UUID.fromString(eventIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Event ID inválido"))
                        return
                    }

            // TODO: Validar que o partner é dono do evento (se não for admin)

            val reservations = listEventReservationsUseCase.execute(eventId)
            call.respond(HttpStatusCode.OK, reservations.map { ReservationResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }
}
