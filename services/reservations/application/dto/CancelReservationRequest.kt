package reservations.application.dto

import kotlinx.serialization.Serializable

@Serializable data class CancelReservationRequest(val reason: String? = null)
