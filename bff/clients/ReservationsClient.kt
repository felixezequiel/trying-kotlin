package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Reservations */
interface IReservationsClient {
    suspend fun createReservation(request: CreateReservationRequest): ReservationResponse
    suspend fun getReservationById(id: String): ReservationResponse?
    suspend fun listMyReservations(customerId: String): List<ReservationResponse>
    suspend fun listEventReservations(eventId: String): List<ReservationResponse>
    suspend fun cancelReservation(id: String): ReservationResponse
    suspend fun convertReservation(id: String): ReservationResponse
}

/** Cliente HTTP para o serviço de Reservations */
class ReservationsClient(private val httpClient: HttpClient, private val baseUrl: String) :
        IReservationsClient {

    override suspend fun createReservation(request: CreateReservationRequest): ReservationResponse {
        val response =
                httpClient.post("$baseUrl/reservations") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to create reservation", response.status.value)
        }
        return response.body()
    }

    override suspend fun getReservationById(id: String): ReservationResponse? {
        val response = httpClient.get("$baseUrl/reservations/$id")
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get reservation", response.status.value)
        }
        return response.body()
    }

    override suspend fun listMyReservations(customerId: String): List<ReservationResponse> {
        val response =
                httpClient.get("$baseUrl/reservations/me") { header("X-Customer-Id", customerId) }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list reservations", response.status.value)
        }
        return response.body()
    }

    override suspend fun listEventReservations(eventId: String): List<ReservationResponse> {
        val response = httpClient.get("$baseUrl/reservations/event/$eventId")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list event reservations", response.status.value)
        }
        return response.body()
    }

    override suspend fun cancelReservation(id: String): ReservationResponse {
        val response = httpClient.post("$baseUrl/reservations/$id/cancel")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to cancel reservation", response.status.value)
        }
        return response.body()
    }

    override suspend fun convertReservation(id: String): ReservationResponse {
        val response = httpClient.post("$baseUrl/reservations/$id/convert")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to convert reservation", response.status.value)
        }
        return response.body()
    }
}

// DTOs
@Serializable
data class CreateReservationRequest(
        val customerId: String,
        val eventId: String,
        val items: List<ReservationItemRequest>
)

@Serializable data class ReservationItemRequest(val ticketTypeId: String, val quantity: Int)

@Serializable
data class ReservationResponse(
        val id: String,
        val customerId: String,
        val eventId: String,
        val status: String,
        val items: List<ReservationItemResponse>,
        val totalAmount: Double,
        val expiresAt: String,
        val createdAt: String
)

@Serializable
data class ReservationItemResponse(
        val ticketTypeId: String,
        val quantity: Int,
        val unitPrice: Double,
        val subtotal: Double
)
