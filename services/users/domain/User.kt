package users.domain

import java.time.Instant

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val roles: Set<Role> = setOf(Role.CUSTOMER),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
