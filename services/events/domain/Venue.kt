package events.domain

data class Venue(
        val name: String,
        val address: String,
        val city: String,
        val state: String,
        val zipCode: String,
        val capacity: Int?
)
