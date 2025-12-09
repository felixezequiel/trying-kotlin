package tickets.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReserveTicketsResponse(
        val success: Boolean,
        val ticketTypeId: String,
        val reservedQuantity: Int,
        val unitPrice: String, // Preço no momento da reserva
        val remainingQuantity: Int
)
