package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Events */
interface IEventsClient {
    suspend fun createEvent(partnerId: String, request: CreateEventRequest): EventResponse
    suspend fun getEventById(id: String): EventResponse?
    suspend fun listPublicEvents(): List<EventResponse>
    suspend fun listEventsByStatus(status: String?): List<EventResponse>
    suspend fun listPartnerEvents(partnerId: String): List<EventResponse>
    suspend fun updateEvent(
            partnerId: String,
            id: String,
            request: UpdateEventRequest
    ): EventResponse
    suspend fun publishEvent(partnerId: String, id: String): EventResponse
    suspend fun cancelEvent(partnerId: String?, id: String, isAdmin: Boolean): EventResponse
    suspend fun finishEvent(partnerId: String?, id: String, isAdmin: Boolean): EventResponse
}

/** Cliente HTTP para o serviço de Events */
class EventsClient(private val httpClient: HttpClient, private val baseUrl: String) :
        IEventsClient {

    override suspend fun createEvent(
            partnerId: String,
            request: CreateEventRequest
    ): EventResponse {
        val response =
                httpClient.post("$baseUrl/events") {
                    contentType(ContentType.Application.Json)
                    header("X-Partner-Id", partnerId)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to create event", response.status.value)
        }
        return response.body()
    }

    override suspend fun getEventById(id: String): EventResponse? {
        val response = httpClient.get("$baseUrl/events/$id")
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get event", response.status.value)
        }
        return response.body()
    }

    override suspend fun listPublicEvents(): List<EventResponse> {
        val response = httpClient.get("$baseUrl/events")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list events", response.status.value)
        }
        return response.body()
    }

    override suspend fun listEventsByStatus(status: String?): List<EventResponse> {
        val response =
                httpClient.get("$baseUrl/events/admin") { status?.let { parameter("status", it) } }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list events", response.status.value)
        }
        return response.body()
    }

    override suspend fun listPartnerEvents(partnerId: String): List<EventResponse> {
        val response = httpClient.get("$baseUrl/events/partner/$partnerId")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list partner events", response.status.value)
        }
        return response.body()
    }

    override suspend fun updateEvent(
            partnerId: String,
            id: String,
            request: UpdateEventRequest
    ): EventResponse {
        val response =
                httpClient.put("$baseUrl/events/$id") {
                    contentType(ContentType.Application.Json)
                    header("X-Partner-Id", partnerId)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to update event", response.status.value)
        }
        return response.body()
    }

    override suspend fun publishEvent(partnerId: String, id: String): EventResponse {
        val response =
                httpClient.post("$baseUrl/events/$id/publish") { header("X-Partner-Id", partnerId) }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to publish event", response.status.value)
        }
        return response.body()
    }

    override suspend fun cancelEvent(
            partnerId: String?,
            id: String,
            isAdmin: Boolean
    ): EventResponse {
        val response =
                httpClient.post("$baseUrl/events/$id/cancel") {
                    partnerId?.let { header("X-Partner-Id", it) }
                    header("X-Is-Admin", isAdmin.toString())
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to cancel event", response.status.value)
        }
        return response.body()
    }

    override suspend fun finishEvent(
            partnerId: String?,
            id: String,
            isAdmin: Boolean
    ): EventResponse {
        val response =
                httpClient.post("$baseUrl/events/$id/finish") {
                    partnerId?.let { header("X-Partner-Id", it) }
                    header("X-Is-Admin", isAdmin.toString())
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to finish event", response.status.value)
        }
        return response.body()
    }
}

// DTOs
@Serializable
data class VenueRequest(
        val name: String,
        val address: String,
        val city: String,
        val state: String,
        val zipCode: String,
        val capacity: Int? = null
)

@Serializable
data class VenueResponse(
        val name: String,
        val address: String,
        val city: String,
        val state: String,
        val zipCode: String,
        val capacity: Int? = null
)

@Serializable
data class CreateEventRequest(
        val name: String,
        val description: String,
        val venue: VenueRequest,
        val startDate: String,
        val endDate: String,
        val imageUrl: String? = null
)

@Serializable
data class UpdateEventRequest(
        val name: String? = null,
        val description: String? = null,
        val venue: VenueRequest? = null,
        val startDate: String? = null,
        val endDate: String? = null,
        val imageUrl: String? = null
)

@Serializable
data class EventResponse(
        val id: String,
        val partnerId: String,
        val name: String,
        val description: String,
        val venue: VenueResponse,
        val startDate: String,
        val endDate: String,
        val status: String,
        val imageUrl: String? = null,
        val createdAt: String,
        val publishedAt: String? = null
)
