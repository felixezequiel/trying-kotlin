package orders.adapters.inbound

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import orders.application.dto.CreateOrderRequest
import orders.application.dto.ErrorResponse
import orders.application.dto.IssuedTicketResponse
import orders.application.dto.OrderResponse
import orders.application.dto.PaymentResponse
import orders.application.dto.ProcessPaymentRequest
import orders.application.dto.RefundRequest
import orders.application.useCases.CreateOrderUseCase
import orders.application.useCases.GetOrderUseCase
import orders.application.useCases.ListCustomerOrdersUseCase
import orders.application.useCases.ListOrderTicketsUseCase
import orders.application.useCases.ProcessPaymentUseCase
import orders.application.useCases.RefundOrderUseCase

class OrderController(
        private val createOrderUseCase: CreateOrderUseCase,
        private val processPaymentUseCase: ProcessPaymentUseCase,
        private val getOrderUseCase: GetOrderUseCase,
        private val listCustomerOrdersUseCase: ListCustomerOrdersUseCase,
        private val listOrderTicketsUseCase: ListOrderTicketsUseCase,
        private val refundOrderUseCase: RefundOrderUseCase
) {

    // POST /orders - Cria order a partir de reservation (CUSTOMER)
    suspend fun createOrder(call: ApplicationCall) {
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

            val request = call.receive<CreateOrderRequest>()
            val order = createOrderUseCase.execute(customerId, request)
            call.respond(HttpStatusCode.Created, OrderResponse.fromDomain(order))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível criar pedido")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /orders/{id}/pay - Processa pagamento (CUSTOMER)
    suspend fun processPayment(call: ApplicationCall) {
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

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val orderId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request = call.receive<ProcessPaymentRequest>()
            val result = processPaymentUseCase.execute(customerId, orderId, request)

            val response =
                    PaymentResponse(
                            orderId = result.order.id.toString(),
                            paymentStatus = result.order.paymentStatus,
                            transactionId = result.order.transactionId,
                            tickets = result.tickets?.map { IssuedTicketResponse.fromDomain(it) }
                    )

            call.respond(HttpStatusCode.OK, response)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível processar pagamento")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /orders/{id} - Busca order (CUSTOMER/ADMIN)
    suspend fun getOrderById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val orderId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val order = getOrderUseCase.execute(orderId)
            if (order != null) {
                // TODO: Validar permissão (dono ou admin)
                call.respond(HttpStatusCode.OK, OrderResponse.fromDomain(order))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Pedido não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /orders/me - Meus orders (CUSTOMER)
    suspend fun listMyOrders(call: ApplicationCall) {
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

            val orders = listCustomerOrdersUseCase.execute(customerId)
            call.respond(HttpStatusCode.OK, orders.map { OrderResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // GET /orders/{id}/tickets - Ingressos do order (CUSTOMER)
    suspend fun listOrderTickets(call: ApplicationCall) {
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

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val orderId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val tickets = listOrderTicketsUseCase.execute(customerId, orderId)
            call.respond(HttpStatusCode.OK, tickets.map { IssuedTicketResponse.fromDomain(it) })
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    // POST /orders/{id}/refund - Reembolsa order (ADMIN)
    suspend fun refundOrder(call: ApplicationCall) {
        try {
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false
            if (!isAdmin) {
                call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Apenas admin pode reembolsar")
                )
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val orderId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request =
                    try {
                        call.receive<RefundRequest>()
                    } catch (e: Exception) {
                        RefundRequest(null)
                    }

            val order = refundOrderUseCase.execute(orderId, request.reason)
            call.respond(HttpStatusCode.OK, OrderResponse.fromDomain(order))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Não foi possível reembolsar")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }
}
