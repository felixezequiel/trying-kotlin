package events.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
        val name: String,
        val description: String,
        val venue: VenueRequest,
        val startDate: String, // ISO-8601 format
        val endDate: String, // ISO-8601 format
        val imageUrl: String? = null
)
