package events.application.dto

import events.domain.Venue
import kotlinx.serialization.Serializable

@Serializable
data class VenueResponse(
        val name: String,
        val address: String,
        val city: String,
        val state: String,
        val zipCode: String,
        val capacity: Int?
) {
    companion object {
        fun fromDomain(venue: Venue): VenueResponse {
            return VenueResponse(
                    name = venue.name,
                    address = venue.address,
                    city = venue.city,
                    state = venue.state,
                    zipCode = venue.zipCode,
                    capacity = venue.capacity
            )
        }
    }
}
