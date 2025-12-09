package reservations.application.dto

import kotlinx.serialization.Serializable
import reservations.domain.CancellationType
import reservations.domain.Reservation
import reservations.domain.ReservationStatus

@Serializable
data class ReservationResponse(
        val id: String,
        val customerId: String,
        val eventId: String,
        val items: List<ReservationItemResponse>,
        val totalAmount: String,
        val status: ReservationStatus,
        val createdAt: String,
        val cancelledAt: String?,
        val cancelledBy: String?,
        val cancellationReason: String?,
        val cancellationType: CancellationType?,
        val convertedAt: String?,
        val orderId: String?
) {
    companion object {
        fun fromDomain(reservation: Reservation): ReservationResponse {
            return ReservationResponse(
                    id = reservation.id.toString(),
                    customerId = reservation.customerId.toString(),
                    eventId = reservation.eventId.toString(),
                    items = reservation.items.map { ReservationItemResponse.fromDomain(it) },
                    totalAmount = reservation.totalAmount.value.toString(),
                    status = reservation.status,
                    createdAt = reservation.createdAt.toString(),
                    cancelledAt = reservation.cancelledAt?.toString(),
                    cancelledBy = reservation.cancelledBy?.toString(),
                    cancellationReason = reservation.cancellationReason,
                    cancellationType = reservation.cancellationType,
                    convertedAt = reservation.convertedAt?.toString(),
                    orderId = reservation.orderId?.toString()
            )
        }
    }
}
