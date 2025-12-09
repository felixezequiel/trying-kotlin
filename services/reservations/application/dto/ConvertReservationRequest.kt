package reservations.application.dto

import kotlinx.serialization.Serializable

@Serializable data class ConvertReservationRequest(val orderId: String)
