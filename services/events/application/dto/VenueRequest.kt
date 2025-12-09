package events.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class VenueRequest(
        val name: String,
        val address: String,
        val city: String,
        val state: String,
        val zipCode: String,
        val capacity: Int? = null
)
