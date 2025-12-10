package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Events */
interface IEventsClient {
    suspend fun createEvent(request: CreateEventRequest): EventResponse
    suspend fun getEventById(id: String): EventResponse?
    suspend fun listPublicEvents(): List<EventResponse>
    suspend fun listEventsByStatus(status: String?): List<EventResponse>
    suspend fun listPartnerEvents(partnerId: String): List<EventResponse>
    suspend fun updateEvent(id: String, request: UpdateEventRequest): EventResponse
    suspend fun publishEvent(id: String): EventResponse
    suspend fun cancelEvent(id: String): EventResponse
    suspend fun finishEvent(id: String): EventResponse
}

/** Cliente HTTP para o serviço de Events */
class EventsClient(private val httpClient: HttpClient, private val baseUrl: String) :
        IEventsClient {

    override suspend fun createEvent(request: CreateEventRequest): EventResponse {
        val response =
                httpClient.post("$baseUrl/events") {
                    contentType(ContentType.Application.Json)
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

    override suspend fun updateEvent(id: String, request: UpdateEventRequest): EventResponse {
        val response =
                httpClient.put("$baseUrl/events/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to update event", response.status.value)
        }
        return response.body()
    }

    override suspend fun publishEvent(id: String): EventResponse {
        val response = httpClient.post("$baseUrl/events/$id/publish")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to publish event", response.status.value)
        }
        return response.body()
    }

    override suspend fun cancelEvent(id: String): EventResponse {
        val response = httpClient.post("$baseUrl/events/$id/cancel")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to cancel event", response.status.value)
        }
        return response.body()
    }

    override suspend fun finishEvent(id: String): EventResponse {
        val response = httpClient.post("$baseUrl/events/$id/finish")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to finish event", response.status.value)
        }
        return response.body()
    }
}

// DTOs
@Serializable
data class CreateEventRequest(
        val partnerId: String,
        val name: String,
        val description: String,
        val startDate: String,
        val endDate: String,
        val location: String
)

@Serializable
data class UpdateEventRequest(
        val name: String? = null,
        val description: String? = null,
        val startDate: String? = null,
        val endDate: String? = null,
        val location: String? = null
)

@Serializable
data class EventResponse(
        val id: String,
        val partnerId: String,
        val name: String,
        val description: String,
        val status: String,
        val startDate: String,
        val endDate: String,
        val location: String
)
