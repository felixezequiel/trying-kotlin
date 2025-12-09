package tickets.application.dto

import kotlinx.serialization.Serializable
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus

@Serializable
data class TicketTypeResponse(
        val id: String,
        val eventId: String,
        val name: String,
        val description: String,
        val price: String,
        val totalQuantity: Int,
        val availableQuantity: Int,
        val maxPerCustomer: Int,
        val salesStartDate: String?,
        val salesEndDate: String?,
        val status: TicketTypeStatus,
        val createdAt: String
) {
    companion object {
        fun fromDomain(ticketType: TicketType): TicketTypeResponse {
            return TicketTypeResponse(
                    id = ticketType.id.toString(),
                    eventId = ticketType.eventId.toString(),
                    name = ticketType.name.value,
                    description = ticketType.description,
                    price = ticketType.price.value.toString(),
                    totalQuantity = ticketType.totalQuantity.value,
                    availableQuantity = ticketType.availableQuantity.value,
                    maxPerCustomer = ticketType.maxPerCustomer.value,
                    salesStartDate = ticketType.salesStartDate?.toString(),
                    salesEndDate = ticketType.salesEndDate?.toString(),
                    status = ticketType.status,
                    createdAt = ticketType.createdAt.toString()
            )
        }
    }
}
