package reservations.application.dto

import kotlinx.serialization.Serializable
import reservations.domain.ReservationItem

@Serializable
data class ReservationItemResponse(
        val id: String,
        val ticketTypeId: String,
        val ticketTypeName: String,
        val quantity: Int,
        val unitPrice: String,
        val subtotal: String
) {
    companion object {
        fun fromDomain(item: ReservationItem): ReservationItemResponse {
            return ReservationItemResponse(
                    id = item.id.toString(),
                    ticketTypeId = item.ticketTypeId.toString(),
                    ticketTypeName = item.ticketTypeName,
                    quantity = item.quantity.value,
                    unitPrice = item.unitPrice.value.toString(),
                    subtotal = item.subtotal.value.toString()
            )
        }
    }
}
