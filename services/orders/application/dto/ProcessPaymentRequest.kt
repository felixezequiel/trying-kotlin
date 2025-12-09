package orders.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProcessPaymentRequest(
        val paymentMethod: String // "credit_card", "pix", "mock"
)
