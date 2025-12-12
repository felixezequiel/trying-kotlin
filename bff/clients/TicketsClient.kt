package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Tickets */
interface ITicketsClient {
    suspend fun createTicketType(
            partnerId: String,
            request: CreateTicketTypeRequest
    ): TicketTypeResponse
    suspend fun getTicketTypeById(id: String): TicketTypeResponse?
    suspend fun listTicketTypesByEvent(eventId: String): List<TicketTypeResponse>
    suspend fun updateTicketType(
            partnerId: String,
            id: String,
            request: UpdateTicketTypeRequest
    ): TicketTypeResponse
    suspend fun deactivateTicketType(partnerId: String, id: String)
    suspend fun activateTicketType(partnerId: String, id: String): TicketTypeResponse
    suspend fun reserveTickets(request: ReserveTicketsRequest): ReserveTicketsResponse
    suspend fun releaseTickets(request: ReleaseTicketsRequest)
}

/** Cliente HTTP para o serviço de Tickets */
class TicketsClient(private val httpClient: HttpClient, private val baseUrl: String) :
        ITicketsClient {

    override suspend fun createTicketType(
            partnerId: String,
            request: CreateTicketTypeRequest
    ): TicketTypeResponse {
        val response =
                httpClient.post("$baseUrl/ticket-types") {
                    contentType(ContentType.Application.Json)
                    header("X-Partner-Id", partnerId)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw ServiceException(
                    "Failed to create ticket type: $errorBody",
                    response.status.value
            )
        }
        return response.body()
    }

    override suspend fun getTicketTypeById(id: String): TicketTypeResponse? {
        val response = httpClient.get("$baseUrl/ticket-types/$id")
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get ticket type", response.status.value)
        }
        return response.body()
    }

    override suspend fun listTicketTypesByEvent(eventId: String): List<TicketTypeResponse> {
        val response = httpClient.get("$baseUrl/ticket-types/event/$eventId")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list ticket types", response.status.value)
        }
        return response.body()
    }

    override suspend fun updateTicketType(
            partnerId: String,
            id: String,
            request: UpdateTicketTypeRequest
    ): TicketTypeResponse {
        val response =
                httpClient.put("$baseUrl/ticket-types/$id") {
                    contentType(ContentType.Application.Json)
                    header("X-Partner-Id", partnerId)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to update ticket type", response.status.value)
        }
        return response.body()
    }

    override suspend fun deactivateTicketType(partnerId: String, id: String) {
        val response =
                httpClient.delete("$baseUrl/ticket-types/$id") { header("X-Partner-Id", partnerId) }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to deactivate ticket type", response.status.value)
        }
    }

    override suspend fun activateTicketType(partnerId: String, id: String): TicketTypeResponse {
        val response =
                httpClient.post("$baseUrl/ticket-types/$id/activate") {
                    contentType(ContentType.Application.Json)
                    header("X-Partner-Id", partnerId)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to activate ticket type", response.status.value)
        }
        return response.body()
    }

    override suspend fun reserveTickets(request: ReserveTicketsRequest): ReserveTicketsResponse {
        val response =
                httpClient.post("$baseUrl/ticket-types/reserve") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to reserve tickets", response.status.value)
        }
        return response.body()
    }

    override suspend fun releaseTickets(request: ReleaseTicketsRequest) {
        val response =
                httpClient.post("$baseUrl/ticket-types/release") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to release tickets", response.status.value)
        }
    }
}

// DTOs
@Serializable
data class CreateTicketTypeRequest(
        val eventId: String,
        val name: String,
        val description: String,
        val price: String, // BigDecimal como String para serialização
        val totalQuantity: Int,
        val maxPerCustomer: Int,
        val salesStartDate: String? = null, // ISO-8601 format
        val salesEndDate: String? = null // ISO-8601 format
)

@Serializable
data class UpdateTicketTypeRequest(
        val name: String? = null,
        val description: String? = null,
        val price: String? = null,
        val totalQuantity: Int? = null,
        val maxPerCustomer: Int? = null,
        val salesStartDate: String? = null,
        val salesEndDate: String? = null
)

@Serializable
data class TicketTypeResponse(
        val id: String,
        val eventId: String,
        val name: String,
        val description: String,
        val price: String,
        val totalQuantity: Int,
        val availableQuantity: Int,
        val maxPerCustomer: Int,
        val salesStartDate: String? = null,
        val salesEndDate: String? = null,
        val status: String,
        val createdAt: String
)

@Serializable data class ReserveTicketsRequest(val ticketTypeId: String, val quantity: Int)

@Serializable
data class ReserveTicketsResponse(
        val ticketTypeId: String,
        val reservedQuantity: Int,
        val remainingQuantity: Int
)

@Serializable data class ReleaseTicketsRequest(val ticketTypeId: String, val quantity: Int)
