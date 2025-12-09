package reservations.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateReservationRequest(
        val eventId: String,
        val items: List<CreateReservationItemRequest>
)
