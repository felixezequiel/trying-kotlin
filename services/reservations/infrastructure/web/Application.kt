package reservations.infrastructure.web

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
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import reservations.adapters.inbound.ReservationController
import reservations.adapters.outbound.InMemoryReservationStore
import reservations.adapters.outbound.TicketsClientAdapter
import reservations.adapters.outbound.UnitOfWorkAdapter
import reservations.application.dto.ErrorResponse
import reservations.application.useCases.CancelReservationUseCase
import reservations.application.useCases.ConvertReservationUseCase
import reservations.application.useCases.CreateReservationUseCase
import reservations.application.useCases.GetReservationUseCase
import reservations.application.useCases.ListCustomerReservationsUseCase
import reservations.application.useCases.ListEventReservationsUseCase

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
    // Para trocar para PostgreSQL, basta criar PostgresReservationStore e usar aqui
    val reservationStore = InMemoryReservationStore()
    val unitOfWork =
            UnitOfWorkAdapter(reservationStore.repository, reservationStore.transactionManager)
    val ticketsClient = TicketsClientAdapter()

    // Use Cases
    val createReservationUseCase = CreateReservationUseCase(unitOfWork, ticketsClient)
    val cancelReservationUseCase = CancelReservationUseCase(unitOfWork, ticketsClient)
    val convertReservationUseCase = ConvertReservationUseCase(unitOfWork)
    val getReservationUseCase = GetReservationUseCase(unitOfWork)
    val listCustomerReservationsUseCase = ListCustomerReservationsUseCase(unitOfWork)
    val listEventReservationsUseCase = ListEventReservationsUseCase(unitOfWork)

    // Controller
    val reservationController =
            ReservationController(
                    createReservationUseCase,
                    cancelReservationUseCase,
                    convertReservationUseCase,
                    getReservationUseCase,
                    listCustomerReservationsUseCase,
                    listEventReservationsUseCase
            )

    // Configuração de rotas REST
    routing {
        route("/reservations") {
            // POST /reservations - Cria reserva (CUSTOMER)
            post { reservationController.createReservation(call) }

            // GET /reservations/me - Minhas reservas (CUSTOMER)
            get("/me") { reservationController.listMyReservations(call) }

            // GET /reservations/event/{eventId} - Reservas do evento (PARTNER/ADMIN)
            get("/event/{eventId}") { reservationController.listEventReservations(call) }

            // Rotas com ID
            route("/{id}") {
                // GET /reservations/{id} - Busca por ID (Dono/PARTNER/ADMIN)
                get { reservationController.getReservationById(call) }

                // POST /reservations/{id}/cancel - Cancela reserva (CUSTOMER/PARTNER/ADMIN)
                post("/cancel") { reservationController.cancelReservation(call) }

                // POST /reservations/{id}/convert - Converte em Order (Internal)
                post("/convert") { reservationController.convertReservation(call) }
            }
        }
    }
}
