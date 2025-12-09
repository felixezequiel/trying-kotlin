package users.application.dto

import kotlinx.serialization.Serializable
import users.domain.Role

@Serializable
data class AddRoleRequest(
    val role: Role
)
