package services.users

import users.domain.Role
import users.domain.User
import users.domain.valueObjects.UserEmail
import users.domain.valueObjects.UserName

object TestHelpers {
    fun createTestUser(
            id: Long = 0L,
            name: String = "Test User",
            email: String = "test@example.com",
            roles: Set<Role> = setOf(Role.CUSTOMER)
    ): User {
        return User(id = id, name = UserName.of(name), email = UserEmail.of(email), roles = roles)
    }
}
