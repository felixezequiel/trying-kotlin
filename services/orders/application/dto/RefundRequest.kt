package orders.application.dto

import kotlinx.serialization.Serializable

@Serializable data class RefundRequest(val reason: String? = null)
