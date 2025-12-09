package events.domain

import java.time.Instant
import java.util.UUID

data class Event(
        val id: UUID = UUID.randomUUID(),
        val partnerId: UUID, // Referência ao Partner
        val name: String,
        val description: String,
        val venue: Venue,
        val startDate: Instant,
        val endDate: Instant,
        val status: EventStatus = EventStatus.DRAFT,
        val imageUrl: String? = null,
        val createdAt: Instant = Instant.now(),
        val publishedAt: Instant? = null,
        val updatedAt: Instant = Instant.now()
)
