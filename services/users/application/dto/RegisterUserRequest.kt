package users.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val name: String,
    val email: String
)
