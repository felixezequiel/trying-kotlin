package orders.adapters.inbound

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import orders.application.dto.ErrorResponse
import orders.application.dto.IssuedTicketResponse
import orders.application.useCases.GetIssuedTicketUseCase
import orders.application.useCases.ValidateTicketUseCase

class TicketController(
        private val getIssuedTicketUseCase: GetIssuedTicketUseCase,
        private val validateTicketUseCase: ValidateTicketUseCase
) {

    // GET /tickets/{code} - Busca ingresso por código (Público)
    suspend fun getTicketByCode(call: ApplicationCall) {
        try {
            val code = call.parameters["code"]
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Código é obrigatório"))
                return
            }

            val ticket = getIssuedTicketUseCase.execute(code)
            if (ticket != null) {
                call.respond(HttpStatusCode.OK, IssuedTicketResponse.fromDomain(ticket))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Ingresso não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /tickets/{code}/validate - Check-in (PARTNER/ADMIN)
    suspend fun validateTicket(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            if (!isAdmin && partnerIdStr == null) {
                call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Apenas partner ou admin pode validar")
                )
                return
            }

            val code = call.parameters["code"]
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Código é obrigatório"))
                return
            }

            val ticket = validateTicketUseCase.execute(code)
            call.respond(HttpStatusCode.OK, IssuedTicketResponse.fromDomain(ticket))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Ingresso não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.Conflict, ErrorResponse(e.message ?: "Ingresso inválido"))
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }
}
