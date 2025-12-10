package bff.routes

import bff.clients.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.exceptions.NotFoundException

/** Rotas REST para o domínio de Orders */
fun Route.ordersRoutes(ordersClient: IOrdersClient) {
    route("/api/orders") {

        // POST /api/orders - Criar pedido
        post {
            val request = call.receive<CreateOrderRequest>()
            val order = ordersClient.createOrder(request)
            call.respond(HttpStatusCode.Created, order)
        }

        // GET /api/orders/me - Listar meus pedidos
        get("/me") {
            val customerId =
                    call.request.headers["X-Customer-Id"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Customer ID header is required")
                            )
            val orders = ordersClient.listMyOrders(customerId)
            call.respond(orders)
        }

        // GET /api/orders/{id} - Buscar pedido por ID
        get("/{id}") {
            val id =
                    call.parameters["id"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Order ID is required")
                            )
            val order =
                    ordersClient.getOrderById(id)
                            ?: throw NotFoundException("Order not found with id: $id")
            call.respond(order)
        }

        // POST /api/orders/{id}/pay - Processar pagamento
        post("/{id}/pay") {
            val id =
                    call.parameters["id"]
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Order ID is required")
                            )
            val request = call.receive<ProcessPaymentRequest>()
            val order = ordersClient.processPayment(id, request)
            call.respond(order)
        }

        // POST /api/orders/{id}/refund - Reembolsar pedido
        post("/{id}/refund") {
            val id =
                    call.parameters["id"]
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Order ID is required")
                            )
            val order = ordersClient.refundOrder(id)
            call.respond(order)
        }

        // GET /api/orders/{id}/tickets - Listar ingressos do pedido
        get("/{id}/tickets") {
            val id =
                    call.parameters["id"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Order ID is required")
                            )
            val tickets = ordersClient.listOrderTickets(id)
            call.respond(tickets)
        }
    }

    // Rotas de ingressos emitidos
    route("/api/tickets") {

        // GET /api/tickets/{code} - Buscar ingresso por código
        get("/{code}") {
            val code =
                    call.parameters["code"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Ticket code is required")
                            )
            val ticket =
                    ordersClient.getTicketByCode(code)
                            ?: throw NotFoundException("Ticket not found with code: $code")
            call.respond(ticket)
        }

        // POST /api/tickets/{code}/validate - Validar ingresso (check-in)
        post("/{code}/validate") {
            val code =
                    call.parameters["code"]
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Ticket code is required")
                            )
            val ticket = ordersClient.validateTicket(code)
            call.respond(ticket)
        }
    }
}
