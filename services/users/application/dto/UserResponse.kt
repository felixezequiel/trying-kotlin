package users.application.dto

import kotlinx.serialization.Serializable
import users.domain.Role
import users.domain.User

@Serializable
data class UserResponse(
        val id: Long,
        val name: String,
        val email: String,
        val roles: Set<Role>,
        val createdAt: String
) {
    companion object {
        fun fromDomain(user: User): UserResponse {
            return UserResponse(
                    id = user.id,
                    name = user.name.value,
                    email = user.email.value,
                    roles = user.roles,
                    createdAt = user.createdAt.toString()
            )
        }
    }
}
