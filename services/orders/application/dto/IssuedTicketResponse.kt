package orders.application.dto

import kotlinx.serialization.Serializable
import orders.domain.IssuedTicket
import orders.domain.TicketStatus

@Serializable
data class IssuedTicketResponse(
        val id: String,
        val code: String,
        val qrCode: String,
        val ticketTypeName: String,
        val eventName: String,
        val status: TicketStatus,
        val issuedAt: String,
        val usedAt: String?
) {
    companion object {
        fun fromDomain(ticket: IssuedTicket): IssuedTicketResponse {
            return IssuedTicketResponse(
                    id = ticket.id.toString(),
                    code = ticket.code.value,
                    qrCode = ticket.qrCode,
                    ticketTypeName = ticket.ticketTypeName,
                    eventName = ticket.eventName,
                    status = ticket.status,
                    issuedAt = ticket.issuedAt.toString(),
                    usedAt = ticket.usedAt?.toString()
            )
        }
    }
}
