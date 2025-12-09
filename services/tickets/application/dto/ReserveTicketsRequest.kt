package tickets.application.dto

import kotlinx.serialization.Serializable

@Serializable data class ReserveTicketsRequest(val ticketTypeId: String, val quantity: Int)
