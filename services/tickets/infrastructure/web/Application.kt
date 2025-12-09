package tickets.infrastructure.web

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import tickets.adapters.inbound.TicketTypeController
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.dto.ErrorResponse
import tickets.application.useCases.CreateTicketTypeUseCase
import tickets.application.useCases.DeactivateTicketTypeUseCase
import tickets.application.useCases.GetTicketTypeUseCase
import tickets.application.useCases.ListTicketTypesByEventUseCase
import tickets.application.useCases.ReleaseTicketsUseCase
import tickets.application.useCases.ReserveTicketsUseCase
import tickets.application.useCases.UpdateTicketTypeUseCase
import tickets.infrastructure.persistence.DatabaseContext

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
        )
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno: ${cause.message}")
            )
        }
    }

    // Inicialização das dependências
    val dbContext = DatabaseContext()
    val ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)

    // Use Cases
    val createTicketTypeUseCase = CreateTicketTypeUseCase(ticketTypeRepository)
    val updateTicketTypeUseCase = UpdateTicketTypeUseCase(ticketTypeRepository)
    val deactivateTicketTypeUseCase = DeactivateTicketTypeUseCase(ticketTypeRepository)
    val getTicketTypeUseCase = GetTicketTypeUseCase(ticketTypeRepository)
    val listTicketTypesByEventUseCase = ListTicketTypesByEventUseCase(ticketTypeRepository)
    val reserveTicketsUseCase = ReserveTicketsUseCase(ticketTypeRepository)
    val releaseTicketsUseCase = ReleaseTicketsUseCase(ticketTypeRepository)

    // Controller
    val ticketTypeController =
            TicketTypeController(
                    createTicketTypeUseCase,
                    updateTicketTypeUseCase,
                    deactivateTicketTypeUseCase,
                    getTicketTypeUseCase,
                    listTicketTypesByEventUseCase,
                    reserveTicketsUseCase,
                    releaseTicketsUseCase
            )

    // Configuração de rotas REST
    routing {
        route("/ticket-types") {
            // POST /ticket-types - Cria tipo de ingresso (PARTNER)
            post { ticketTypeController.createTicketType(call) }

            // GET /ticket-types/event/{eventId} - Lista por evento (Público)
            get("/event/{eventId}") { ticketTypeController.listTicketTypesByEvent(call) }

            // POST /ticket-types/reserve - Reserva (decrementa) (Internal)
            post("/reserve") { ticketTypeController.reserveTickets(call) }

            // POST /ticket-types/release - Libera (incrementa) (Internal)
            post("/release") { ticketTypeController.releaseTickets(call) }

            // Rotas com ID
            route("/{id}") {
                // GET /ticket-types/{id} - Busca por ID (Público)
                get { ticketTypeController.getTicketTypeById(call) }

                // PUT /ticket-types/{id} - Atualiza tipo (PARTNER dono)
                put { ticketTypeController.updateTicketType(call) }

                // DELETE /ticket-types/{id} - Desativa tipo (PARTNER / ADMIN)
                delete { ticketTypeController.deactivateTicketType(call) }
            }
        }
    }
}
