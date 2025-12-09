package services.users

import java.time.Instant
import users.application.ports.outbound.IUserRepository
import users.domain.Role
import users.domain.User
import users.domain.valueObjects.UserEmail

/** Fake repository para testes unitários. */
class FakeUserRepository : IUserRepository {
    private val users = mutableListOf<User>()

    override suspend fun add(user: User): Long {
        if (users.any { it.email.value == user.email.value }) {
            throw IllegalArgumentException("Usuário com este e-mail já existe.")
        }
        val newId = users.size + 1L
        val now = Instant.now()
        users.add(user.copy(id = newId, createdAt = now, updatedAt = now))
        return newId
    }

    override suspend fun getById(id: Long): User? {
        return users.find { it.id == id }
    }

    override suspend fun getUserByEmail(email: UserEmail): User? {
        return users.find { it.email.value == email.value }
    }

    override suspend fun getAll(): List<User> {
        return users.toList()
    }

    override suspend fun update(user: User): Boolean {
        val index = users.indexOfFirst { it.id == user.id }
        if (index == -1) return false
        users[index] = user.copy(updatedAt = Instant.now())
        return true
    }

    override suspend fun addRole(userId: Long, role: Role): Boolean {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return false
        val user = users[index]
        if (user.roles.contains(role)) return true
        users[index] = user.copy(roles = user.roles + role, updatedAt = Instant.now())
        return true
    }

    override suspend fun removeRole(userId: Long, role: Role): Boolean {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return false
        val user = users[index]
        if (!user.roles.contains(role)) return true
        if (role == Role.CUSTOMER && user.roles.size == 1) {
            throw IllegalStateException(
                    "Não é possível remover o role CUSTOMER quando é o único role do usuário"
            )
        }
        users[index] = user.copy(roles = user.roles - role, updatedAt = Instant.now())
        return true
    }

    fun clear() {
        users.clear()
    }

    fun count(): Int = users.size
}
