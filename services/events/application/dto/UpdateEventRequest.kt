package events.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEventRequest(
        val name: String? = null,
        val description: String? = null,
        val venue: VenueRequest? = null,
        val startDate: String? = null, // ISO-8601 format
        val endDate: String? = null, // ISO-8601 format
        val imageUrl: String? = null
)
