package tickets.adapters.inbound

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import tickets.application.dto.CreateTicketTypeRequest
import tickets.application.dto.ErrorResponse
import tickets.application.dto.ReleaseTicketsRequest
import tickets.application.dto.ReserveTicketsRequest
import tickets.application.dto.TicketTypeResponse
import tickets.application.dto.UpdateTicketTypeRequest
import tickets.application.useCases.ActivateTicketTypeUseCase
import tickets.application.useCases.CreateTicketTypeUseCase
import tickets.application.useCases.DeactivateTicketTypeUseCase
import tickets.application.useCases.GetTicketTypeUseCase
import tickets.application.useCases.ListTicketTypesByEventUseCase
import tickets.application.useCases.ReleaseTicketsUseCase
import tickets.application.useCases.ReserveTicketsUseCase
import tickets.application.useCases.UpdateTicketTypeUseCase

class TicketTypeController(
        private val createTicketTypeUseCase: CreateTicketTypeUseCase,
        private val updateTicketTypeUseCase: UpdateTicketTypeUseCase,
        private val deactivateTicketTypeUseCase: DeactivateTicketTypeUseCase,
        private val activateTicketTypeUseCase: ActivateTicketTypeUseCase,
        private val getTicketTypeUseCase: GetTicketTypeUseCase,
        private val listTicketTypesByEventUseCase: ListTicketTypesByEventUseCase,
        private val reserveTicketsUseCase: ReserveTicketsUseCase,
        private val releaseTicketsUseCase: ReleaseTicketsUseCase
) {

    // POST /ticket-types - Cria tipo de ingresso (PARTNER)
    suspend fun createTicketType(call: ApplicationCall) {
        try {
            // TODO: Validar que o partner é dono do evento (RN-T01)
            // Por enquanto, apenas verificamos se há um partner autenticado
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            if (partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Partner não autenticado"))
                return
            }

            val request = call.receive<CreateTicketTypeRequest>()
            val ticketTypeId = createTicketTypeUseCase.execute(request)

            val ticketType = getTicketTypeUseCase.execute(ticketTypeId)
            if (ticketType != null) {
                call.respond(HttpStatusCode.Created, TicketTypeResponse.fromDomain(ticketType))
            } else {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Tipo de ingresso criado mas não encontrado")
                )
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /ticket-types/{id} - Busca por ID (Público)
    suspend fun getTicketTypeById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val ticketTypeId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val ticketType = getTicketTypeUseCase.execute(ticketTypeId)
            if (ticketType != null) {
                call.respond(HttpStatusCode.OK, TicketTypeResponse.fromDomain(ticketType))
            } else {
                call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Tipo de ingresso não encontrado")
                )
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // PUT /ticket-types/{id} - Atualiza tipo (PARTNER dono)
    suspend fun updateTicketType(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            if (partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Partner não autenticado"))
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val ticketTypeId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request = call.receive<UpdateTicketTypeRequest>()
            val updatedTicketType = updateTicketTypeUseCase.execute(ticketTypeId, request)
            call.respond(HttpStatusCode.OK, TicketTypeResponse.fromDomain(updatedTicketType))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Tipo de ingresso não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // DELETE /ticket-types/{id} - Desativa tipo (PARTNER / ADMIN)
    suspend fun deactivateTicketType(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            if (!isAdmin && partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Não autenticado"))
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val ticketTypeId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val deactivatedTicketType = deactivateTicketTypeUseCase.execute(ticketTypeId)
            call.respond(HttpStatusCode.OK, TicketTypeResponse.fromDomain(deactivatedTicketType))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Tipo de ingresso não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /ticket-types/event/{eventId} - Lista por evento (Público)
    suspend fun listTicketTypesByEvent(call: ApplicationCall) {
        try {
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

            val ticketTypes = listTicketTypesByEventUseCase.execute(eventId)
            call.respond(HttpStatusCode.OK, ticketTypes.map { TicketTypeResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /ticket-types/reserve - Reserva (decrementa) (Internal)
    suspend fun reserveTickets(call: ApplicationCall) {
        try {
            // Endpoint interno - chamado pelo Reservations service via BFF
            val request = call.receive<ReserveTicketsRequest>()
            val response = reserveTicketsUseCase.execute(request)
            call.respond(HttpStatusCode.OK, response)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível reservar")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /ticket-types/release - Libera (incrementa) (Internal)
    suspend fun releaseTickets(call: ApplicationCall) {
        try {
            // Endpoint interno - chamado pelo Reservations service via BFF
            val request = call.receive<ReleaseTicketsRequest>()
            val ticketType = releaseTicketsUseCase.execute(request)
            call.respond(HttpStatusCode.OK, TicketTypeResponse.fromDomain(ticketType))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível liberar")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /ticket-types/{id}/activate - Ativa tipo (PARTNER / ADMIN)
    suspend fun activateTicketType(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            if (!isAdmin && partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Não autenticado"))
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val ticketTypeId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val activatedTicketType = activateTicketTypeUseCase.execute(ticketTypeId)
            call.respond(HttpStatusCode.OK, TicketTypeResponse.fromDomain(activatedTicketType))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Tipo de ingresso não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Não foi possível ativar")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }
}
