package reservations.adapters.outbound

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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import reservations.application.ports.outbound.ITicketsClient
import reservations.application.ports.outbound.TicketTypeInfo
import reservations.domain.valueObjects.Price
import reservations.domain.valueObjects.Quantity
import shared.utils.EnvConfig

class TicketsClientAdapter : ITicketsClient {

    private val ticketsServiceUrl = EnvConfig.get("TICKETS_SERVICE_URL", "http://localhost:8084")

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

    override fun reserve(ticketTypeId: UUID, quantity: Quantity): TicketTypeInfo {
        return runBlocking {
            val reserveResponse =
                    client.post("$ticketsServiceUrl/ticket-types/reserve") {
                        contentType(ContentType.Application.Json)
                        setBody(ReserveTicketsRequest(ticketTypeId.toString(), quantity.value))
                    }

            if (reserveResponse.status.value != 200) {
                throw IllegalStateException("Não foi possível reservar ingressos")
            }

            val reserveResult = reserveResponse.body<ReserveTicketsResponse>()
            
            // Fetch full ticket type info after successful reservation
            val ticketTypeResponse = client.get("$ticketsServiceUrl/ticket-types/$ticketTypeId").body<TicketTypeResponse>()
            ticketTypeResponse.toTicketTypeInfo()
        }
    }

    override fun release(ticketTypeId: UUID, quantity: Quantity) {
        runBlocking {
            val response =
                    client.post("$ticketsServiceUrl/ticket-types/release") {
                        contentType(ContentType.Application.Json)
                        setBody(ReleaseTicketsRequest(ticketTypeId.toString(), quantity.value))
                    }

            if (response.status.value != 200) {
                throw IllegalStateException("Não foi possível liberar ingressos")
            }
        }
    }

    override fun getTicketType(ticketTypeId: UUID): TicketTypeInfo? {
        return runBlocking {
            try {
                val response = client.get("$ticketsServiceUrl/ticket-types/$ticketTypeId")

                if (response.status.value == 404) {
                    return@runBlocking null
                }

                if (response.status.value != 200) {
                    throw IllegalStateException("Erro ao buscar tipo de ingresso")
                }

                val ticketTypeResponse = response.body<TicketTypeResponse>()
                ticketTypeResponse.toTicketTypeInfo()
            } catch (e: Exception) {
                null
            }
        }
    }

    @Serializable
    private data class ReserveTicketsRequest(val ticketTypeId: String, val quantity: Int)

    @Serializable
    private data class ReleaseTicketsRequest(val ticketTypeId: String, val quantity: Int)

    @Serializable
    private data class ReserveTicketsResponse(
        val success: Boolean,
        val ticketTypeId: String,
        val reservedQuantity: Int,
        val unitPrice: String,
        val remainingQuantity: Int
    )

    @Serializable
    private data class TicketTypeResponse(
            val id: String,
            val eventId: String,
            val name: String,
            val price: String,
            val totalQuantity: Int,
            val availableQuantity: Int,
            val maxPerCustomer: Int,
            val status: String,
            val salesStartDate: String? = null,
            val salesEndDate: String? = null
    ) {
        fun toTicketTypeInfo(): TicketTypeInfo {
            return TicketTypeInfo(
                    id = UUID.fromString(id),
                    eventId = UUID.fromString(eventId),
                    name = name,
                    price = Price.fromString(price),
                    availableQuantity = Quantity.of(availableQuantity),
                    maxPerCustomer = Quantity.of(maxPerCustomer)
            )
        }
    }
}
