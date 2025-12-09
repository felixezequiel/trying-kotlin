package tickets.application.dto

import kotlinx.serialization.Serializable

@Serializable data class ReleaseTicketsRequest(val ticketTypeId: String, val quantity: Int)
