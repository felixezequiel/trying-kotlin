package tickets.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTicketTypeRequest(
        val name: String? = null,
        val description: String? = null,
        val price: String? = null, // BigDecimal como String
        val totalQuantity: Int? = null,
        val maxPerCustomer: Int? = null,
        val salesStartDate: String? = null, // ISO-8601 format
        val salesEndDate: String? = null, // ISO-8601 format
        val status: String? = null // TicketTypeStatus como String
)
