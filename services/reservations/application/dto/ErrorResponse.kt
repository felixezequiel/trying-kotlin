package reservations.application.dto

import kotlinx.serialization.Serializable

@Serializable data class ErrorResponse(val message: String)
