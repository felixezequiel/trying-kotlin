package events.application.dto

import events.domain.Event
import events.domain.EventStatus
import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
        val id: String,
        val partnerId: String,
        val name: String,
        val description: String,
        val venue: VenueResponse,
        val startDate: String,
        val endDate: String,
        val status: EventStatus,
        val imageUrl: String?,
        val createdAt: String,
        val publishedAt: String?
) {
    companion object {
        fun fromDomain(event: Event): EventResponse {
            return EventResponse(
                    id = event.id.toString(),
                    partnerId = event.partnerId.toString(),
                    name = event.name.value,
                    description = event.description.value,
                    venue = VenueResponse.fromDomain(event.venue),
                    startDate = event.startDate.toString(),
                    endDate = event.endDate.toString(),
                    status = event.status,
                    imageUrl = event.imageUrl,
                    createdAt = event.createdAt.toString(),
                    publishedAt = event.publishedAt?.toString()
            )
        }
    }
}
