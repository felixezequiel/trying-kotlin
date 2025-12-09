package orders.domain

import java.time.Instant
import java.util.UUID
import orders.domain.valueObjects.TicketCode

data class IssuedTicket(
        val id: UUID = UUID.randomUUID(),
        val orderId: UUID,
        val orderItemId: UUID,
        val ticketTypeId: UUID,
        val ticketTypeName: String,
        val eventId: UUID,
        val eventName: String,
        val customerId: UUID,
        val code: TicketCode,
        val qrCode: String,
        val status: TicketStatus = TicketStatus.VALID,
        val issuedAt: Instant = Instant.now(),
        val usedAt: Instant? = null
) {
    fun use(): IssuedTicket {
        require(status == TicketStatus.VALID) { "Ingresso não está válido para uso" }
        return copy(status = TicketStatus.USED, usedAt = Instant.now())
    }

    fun cancel(): IssuedTicket {
        require(status == TicketStatus.VALID) { "Só pode cancelar ingresso VALID" }
        return copy(status = TicketStatus.CANCELLED)
    }

    companion object {
        fun create(
                orderId: UUID,
                orderItemId: UUID,
                ticketTypeId: UUID,
                ticketTypeName: String,
                eventId: UUID,
                eventName: String,
                customerId: UUID
        ): IssuedTicket {
            val code = TicketCode.generate()
            return IssuedTicket(
                    orderId = orderId,
                    orderItemId = orderItemId,
                    ticketTypeId = ticketTypeId,
                    ticketTypeName = ticketTypeName,
                    eventId = eventId,
                    eventName = eventName,
                    customerId = customerId,
                    code = code,
                    qrCode = generateQrCode(code)
            )
        }

        private fun generateQrCode(code: TicketCode): String {
            // Gera dados para QR Code (pode ser expandido para incluir mais informações)
            return "QR:${code.value}"
        }
    }
}
