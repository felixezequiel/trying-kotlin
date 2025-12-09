package orders.adapters.outbound

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import orders.application.ports.outbound.IReservationsClient
import orders.application.ports.outbound.ReservationInfo
import orders.application.ports.outbound.ReservationItemInfo
import shared.utils.EnvConfig

class ReservationsClientAdapter : IReservationsClient {

    private val reservationsServiceUrl =
            EnvConfig.get("RESERVATIONS_SERVICE_URL", "http://localhost:8085")

    private val client =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(
                            Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                    )
                }
            }

    override suspend fun getReservation(reservationId: UUID): ReservationInfo? {
        return try {
            val response: ReservationResponse =
                    client.get("$reservationsServiceUrl/reservations/$reservationId").body()
            response.toReservationInfo()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun convertReservation(reservationId: UUID, orderId: UUID): Boolean {
        return try {
            client.post("$reservationsServiceUrl/reservations/$reservationId/convert") {
                contentType(ContentType.Application.Json)
                setBody(ConvertRequest(orderId.toString()))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    @Serializable private data class ConvertRequest(val orderId: String)

    @Serializable
    private data class ReservationResponse(
            val id: String,
            val customerId: String,
            val eventId: String,
            val items: List<ReservationItemResponse>,
            val totalAmount: String,
            val status: String
    ) {
        fun toReservationInfo(): ReservationInfo {
            return ReservationInfo(
                    id = UUID.fromString(id),
                    customerId = UUID.fromString(customerId),
                    eventId = UUID.fromString(eventId),
                    eventName = "Event", // TODO: Buscar nome do evento
                    items = items.map { it.toReservationItemInfo() },
                    totalAmount = totalAmount,
                    status = status
            )
        }
    }

    @Serializable
    private data class ReservationItemResponse(
            val id: String,
            val ticketTypeId: String,
            val ticketTypeName: String,
            val quantity: Int,
            val unitPrice: String,
            val subtotal: String
    ) {
        fun toReservationItemInfo(): ReservationItemInfo {
            return ReservationItemInfo(
                    id = UUID.fromString(id),
                    ticketTypeId = UUID.fromString(ticketTypeId),
                    ticketTypeName = ticketTypeName,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    subtotal = subtotal
            )
        }
    }
}
