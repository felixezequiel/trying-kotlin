package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Orders */
interface IOrdersClient {
    suspend fun createOrder(request: CreateOrderRequest): OrderResponse
    suspend fun getOrderById(id: String): OrderResponse?
    suspend fun listMyOrders(customerId: String): List<OrderResponse>
    suspend fun processPayment(id: String, request: ProcessPaymentRequest): OrderResponse
    suspend fun refundOrder(id: String): OrderResponse
    suspend fun listOrderTickets(id: String): List<IssuedTicketResponse>
    suspend fun getTicketByCode(code: String): IssuedTicketResponse?
    suspend fun validateTicket(code: String): IssuedTicketResponse
}

/** Cliente HTTP para o serviço de Orders */
class OrdersClient(private val httpClient: HttpClient, private val baseUrl: String) :
        IOrdersClient {

    override suspend fun createOrder(request: CreateOrderRequest): OrderResponse {
        val response =
                httpClient.post("$baseUrl/orders") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to create order", response.status.value)
        }
        return response.body()
    }

    override suspend fun getOrderById(id: String): OrderResponse? {
        val response = httpClient.get("$baseUrl/orders/$id")
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get order", response.status.value)
        }
        return response.body()
    }

    override suspend fun listMyOrders(customerId: String): List<OrderResponse> {
        val response = httpClient.get("$baseUrl/orders/me") { header("X-Customer-Id", customerId) }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list orders", response.status.value)
        }
        return response.body()
    }

    override suspend fun processPayment(id: String, request: ProcessPaymentRequest): OrderResponse {
        val response =
                httpClient.post("$baseUrl/orders/$id/pay") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to process payment", response.status.value)
        }
        return response.body()
    }

    override suspend fun refundOrder(id: String): OrderResponse {
        val response = httpClient.post("$baseUrl/orders/$id/refund")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to refund order", response.status.value)
        }
        return response.body()
    }

    override suspend fun listOrderTickets(id: String): List<IssuedTicketResponse> {
        val response = httpClient.get("$baseUrl/orders/$id/tickets")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list order tickets", response.status.value)
        }
        return response.body()
    }

    override suspend fun getTicketByCode(code: String): IssuedTicketResponse? {
        val response = httpClient.get("$baseUrl/tickets/$code")
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get ticket", response.status.value)
        }
        return response.body()
    }

    override suspend fun validateTicket(code: String): IssuedTicketResponse {
        val response = httpClient.post("$baseUrl/tickets/$code/validate")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to validate ticket", response.status.value)
        }
        return response.body()
    }
}

// DTOs
@Serializable data class CreateOrderRequest(val reservationId: String)

@Serializable
data class ProcessPaymentRequest(
        val paymentMethod: String,
        val paymentDetails: Map<String, String> = emptyMap()
)

@Serializable
data class OrderResponse(
        val id: String,
        val customerId: String,
        val reservationId: String,
        val status: String,
        val totalAmount: Double,
        val paymentId: String? = null,
        val createdAt: String,
        val paidAt: String? = null
)

@Serializable
data class IssuedTicketResponse(
        val id: String,
        val orderId: String,
        val ticketTypeId: String,
        val code: String,
        val status: String,
        val usedAt: String? = null
)
