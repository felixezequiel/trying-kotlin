package tickets.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTicketTypeRequest(
        val eventId: String,
        val name: String,
        val description: String,
        val price: String, // BigDecimal como String para serialização
        val totalQuantity: Int,
        val maxPerCustomer: Int,
        val salesStartDate: String?, // ISO-8601 format
        val salesEndDate: String? // ISO-8601 format
)
