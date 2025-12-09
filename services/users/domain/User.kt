package users.domain

import java.time.Instant
import users.domain.valueObjects.UserEmail
import users.domain.valueObjects.UserName

data class User(
        val id: Long = 0,
        val name: UserName,
        val email: UserEmail,
        val roles: Set<Role> = setOf(Role.CUSTOMER),
        val createdAt: Instant = Instant.now(),
        val updatedAt: Instant = Instant.now()
)
