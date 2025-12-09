package orders.application.dto

import kotlinx.serialization.Serializable
import orders.domain.PaymentStatus

@Serializable
data class PaymentResponse(
        val orderId: String,
        val paymentStatus: PaymentStatus,
        val transactionId: String?,
        val tickets: List<IssuedTicketResponse>?
)
