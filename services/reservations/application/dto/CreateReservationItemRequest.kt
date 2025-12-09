package reservations.application.dto

import kotlinx.serialization.Serializable

@Serializable data class CreateReservationItemRequest(val ticketTypeId: String, val quantity: Int)
