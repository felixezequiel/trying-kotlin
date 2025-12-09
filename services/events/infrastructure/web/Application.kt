package events.infrastructure.web

import events.adapters.inbound.EventController
import events.adapters.outbound.InMemoryEventStore
import events.adapters.outbound.UnitOfWorkAdapter
import events.application.dto.ErrorResponse
import events.application.useCases.CancelEventUseCase
import events.application.useCases.CreateEventUseCase
import events.application.useCases.FinishEventUseCase
import events.application.useCases.GetEventUseCase
import events.application.useCases.ListEventsUseCase
import events.application.useCases.PublishEventUseCase
import events.application.useCases.UpdateEventUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

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

    // Inicialização das dependências (Composição Root)
    // Para trocar para PostgreSQL, basta criar PostgresEventStore e usar aqui
    val eventStore = InMemoryEventStore()
    val unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)

    // Use Cases
    val createEventUseCase = CreateEventUseCase(unitOfWork)
    val updateEventUseCase = UpdateEventUseCase(unitOfWork)
    val publishEventUseCase = PublishEventUseCase(unitOfWork)
    val cancelEventUseCase = CancelEventUseCase(unitOfWork)
    val finishEventUseCase = FinishEventUseCase(unitOfWork)
    val getEventUseCase = GetEventUseCase(unitOfWork)
    val listEventsUseCase = ListEventsUseCase(unitOfWork)

    // Controller
    val eventController =
            EventController(
                    createEventUseCase,
                    updateEventUseCase,
                    publishEventUseCase,
                    cancelEventUseCase,
                    finishEventUseCase,
                    getEventUseCase,
                    listEventsUseCase
            )

    // Configuração de rotas REST
    routing {
        route("/events") {
            // POST /events - Cria evento (PARTNER)
            post { eventController.createEvent(call) }

            // GET /events - Lista eventos públicos (Público)
            get { eventController.listPublicEvents(call) }

            // GET /events/admin - Lista todos eventos com filtros (ADMIN)
            get("/admin") { eventController.listEventsByStatus(call) }

            // GET /events/partner/{partnerId} - Lista eventos do partner (PARTNER dono)
            get("/partner/{partnerId}") { eventController.listPartnerEvents(call) }

            // Rotas com ID
            route("/{id}") {
                // GET /events/{id} - Busca por ID (Público* - DRAFT só visível pelo dono)
                get { eventController.getEventById(call) }

                // PUT /events/{id} - Atualiza evento (PARTNER dono)
                put { eventController.updateEvent(call) }

                // POST /events/{id}/publish - Publica evento (PARTNER dono)
                post("/publish") { eventController.publishEvent(call) }

                // POST /events/{id}/cancel - Cancela evento (PARTNER dono / ADMIN)
                post("/cancel") { eventController.cancelEvent(call) }

                // POST /events/{id}/finish - Finaliza evento (PARTNER dono / ADMIN)
                post("/finish") { eventController.finishEvent(call) }
            }
        }
    }
}
