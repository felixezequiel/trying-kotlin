package users.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String
)
