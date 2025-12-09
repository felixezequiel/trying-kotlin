package reservations.domain

import java.time.Instant
import java.util.UUID
import reservations.domain.valueObjects.Price

data class Reservation(
        val id: UUID = UUID.randomUUID(),
        val customerId: UUID, // User com role CUSTOMER
        val eventId: UUID, // Referência ao Event (desnormalizado)
        val items: List<ReservationItem>,
        val totalAmount: Price, // Soma dos itens
        val status: ReservationStatus = ReservationStatus.ACTIVE,
        val createdAt: Instant = Instant.now(),
        val cancelledAt: Instant? = null,
        val cancelledBy: UUID? = null, // User que cancelou
        val cancellationReason: String? = null,
        val cancellationType: CancellationType? = null,
        val convertedAt: Instant? = null, // Quando virou Order
        val orderId: UUID? = null // Referência ao Order criado
) {
    init {
        require(items.isNotEmpty()) { "Reserva deve ter pelo menos 1 item" }
    }

    fun cancel(
            cancelledBy: UUID,
            reason: String?,
            cancellationType: CancellationType
    ): Reservation {
        require(status == ReservationStatus.ACTIVE) { "Só pode cancelar reserva ACTIVE" }
        return copy(
                status = ReservationStatus.CANCELLED,
                cancelledAt = Instant.now(),
                cancelledBy = cancelledBy,
                cancellationReason = reason,
                cancellationType = cancellationType
        )
    }

    fun convert(orderId: UUID): Reservation {
        require(status == ReservationStatus.ACTIVE) { "Só pode converter reserva ACTIVE" }
        return copy(
                status = ReservationStatus.CONVERTED,
                convertedAt = Instant.now(),
                orderId = orderId
        )
    }

    companion object {
        fun create(customerId: UUID, eventId: UUID, items: List<ReservationItem>): Reservation {
            val totalAmount = items.fold(Price.ZERO) { acc, item -> acc + item.subtotal }
            return Reservation(
                    customerId = customerId,
                    eventId = eventId,
                    items = items,
                    totalAmount = totalAmount
            )
        }
    }
}
