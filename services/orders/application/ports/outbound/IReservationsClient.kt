package orders.application.ports.outbound

import java.util.UUID

interface IReservationsClient {
    suspend fun getReservation(reservationId: UUID): ReservationInfo?
    suspend fun convertReservation(reservationId: UUID, orderId: UUID): Boolean
}

data class ReservationInfo(
        val id: UUID,
        val customerId: UUID,
        val eventId: UUID,
        val eventName: String,
        val items: List<ReservationItemInfo>,
        val totalAmount: String,
        val status: String
)

data class ReservationItemInfo(
        val id: UUID,
        val ticketTypeId: UUID,
        val ticketTypeName: String,
        val quantity: Int,
        val unitPrice: String,
        val subtotal: String
)
