package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Tickets */
interface ITicketsClient {
    suspend fun createTicketType(request: CreateTicketTypeRequest): TicketTypeResponse
    suspend fun getTicketTypeById(id: String): TicketTypeResponse?
    suspend fun listTicketTypesByEvent(eventId: String): List<TicketTypeResponse>
    suspend fun updateTicketType(id: String, request: UpdateTicketTypeRequest): TicketTypeResponse
    suspend fun deactivateTicketType(id: String)
    suspend fun reserveTickets(request: ReserveTicketsRequest): ReserveTicketsResponse
    suspend fun releaseTickets(request: ReleaseTicketsRequest)
}

/** Cliente HTTP para o serviço de Tickets */
class TicketsClient(private val httpClient: HttpClient, private val baseUrl: String) :
        ITicketsClient {

    override suspend fun createTicketType(request: CreateTicketTypeRequest): TicketTypeResponse {
        val response =
                httpClient.post("$baseUrl/ticket-types") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to create ticket type", response.status.value)
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
            id: String,
            request: UpdateTicketTypeRequest
    ): TicketTypeResponse {
        val response =
                httpClient.put("$baseUrl/ticket-types/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to update ticket type", response.status.value)
        }
        return response.body()
    }

    override suspend fun deactivateTicketType(id: String) {
        val response = httpClient.delete("$baseUrl/ticket-types/$id")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to deactivate ticket type", response.status.value)
        }
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
        val price: Double,
        val quantity: Int
)

@Serializable
data class UpdateTicketTypeRequest(
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val quantity: Int? = null
)

@Serializable
data class TicketTypeResponse(
        val id: String,
        val eventId: String,
        val name: String,
        val description: String,
        val price: Double,
        val quantity: Int,
        val availableQuantity: Int,
        val active: Boolean
)

@Serializable data class ReserveTicketsRequest(val ticketTypeId: String, val quantity: Int)

@Serializable
data class ReserveTicketsResponse(
        val ticketTypeId: String,
        val reservedQuantity: Int,
        val remainingQuantity: Int
)

@Serializable data class ReleaseTicketsRequest(val ticketTypeId: String, val quantity: Int)
