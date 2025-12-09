package orders.infrastructure.web

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
import orders.adapters.inbound.OrderController
import orders.adapters.inbound.TicketController
import orders.adapters.outbound.IssuedTicketRepositoryAdapter
import orders.adapters.outbound.MockPaymentGatewayAdapter
import orders.adapters.outbound.OrderRepositoryAdapter
import orders.adapters.outbound.ReservationsClientAdapter
import orders.application.dto.ErrorResponse
import orders.application.useCases.CreateOrderUseCase
import orders.application.useCases.GetIssuedTicketUseCase
import orders.application.useCases.GetOrderUseCase
import orders.application.useCases.ListCustomerOrdersUseCase
import orders.application.useCases.ListOrderTicketsUseCase
import orders.application.useCases.ProcessPaymentUseCase
import orders.application.useCases.RefundOrderUseCase
import orders.application.useCases.ValidateTicketUseCase
import orders.infrastructure.persistence.DatabaseContext

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
    val orderRepository = OrderRepositoryAdapter(dbContext)
    val issuedTicketRepository = IssuedTicketRepositoryAdapter(dbContext)
    val paymentGateway = MockPaymentGatewayAdapter()
    val reservationsClient = ReservationsClientAdapter()

    // Use Cases
    val createOrderUseCase = CreateOrderUseCase(orderRepository, reservationsClient)
    val processPaymentUseCase =
            ProcessPaymentUseCase(
                    orderRepository,
                    issuedTicketRepository,
                    paymentGateway,
                    reservationsClient
            )
    val getOrderUseCase = GetOrderUseCase(orderRepository)
    val listCustomerOrdersUseCase = ListCustomerOrdersUseCase(orderRepository)
    val listOrderTicketsUseCase = ListOrderTicketsUseCase(orderRepository, issuedTicketRepository)
    val refundOrderUseCase =
            RefundOrderUseCase(orderRepository, issuedTicketRepository, paymentGateway)
    val getIssuedTicketUseCase = GetIssuedTicketUseCase(issuedTicketRepository)
    val validateTicketUseCase = ValidateTicketUseCase(issuedTicketRepository)

    // Controllers
    val orderController =
            OrderController(
                    createOrderUseCase,
                    processPaymentUseCase,
                    getOrderUseCase,
                    listCustomerOrdersUseCase,
                    listOrderTicketsUseCase,
                    refundOrderUseCase
            )
    val ticketController = TicketController(getIssuedTicketUseCase, validateTicketUseCase)

    // Configuração de rotas REST
    routing {
        route("/orders") {
            // POST /orders - Cria order (CUSTOMER)
            post { orderController.createOrder(call) }

            // GET /orders/me - Meus orders (CUSTOMER)
            get("/me") { orderController.listMyOrders(call) }

            // Rotas com ID
            route("/{id}") {
                // GET /orders/{id} - Busca por ID (CUSTOMER/ADMIN)
                get { orderController.getOrderById(call) }

                // POST /orders/{id}/pay - Processa pagamento (CUSTOMER)
                post("/pay") { orderController.processPayment(call) }

                // POST /orders/{id}/refund - Reembolsa (ADMIN)
                post("/refund") { orderController.refundOrder(call) }

                // GET /orders/{id}/tickets - Ingressos do order (CUSTOMER)
                get("/tickets") { orderController.listOrderTickets(call) }
            }
        }

        route("/tickets") {
            // GET /tickets/{code} - Busca por código (Público)
            get("/{code}") { ticketController.getTicketByCode(call) }

            // POST /tickets/{code}/validate - Check-in (PARTNER/ADMIN)
            post("/{code}/validate") { ticketController.validateTicket(call) }
        }
    }
}
