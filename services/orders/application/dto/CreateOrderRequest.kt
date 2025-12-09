package orders.application.dto

import kotlinx.serialization.Serializable

@Serializable data class CreateOrderRequest(val reservationId: String)
