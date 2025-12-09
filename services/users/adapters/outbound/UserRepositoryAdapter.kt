package users.adapters.outbound

import users.application.ports.outbound.IUserRepository
import users.domain.Role
import users.domain.User
import users.domain.valueObjects.UserEmail
import users.infrastructure.persistence.DatabaseContext

class UserRepositoryAdapter(private val dbContext: DatabaseContext) : IUserRepository {
    override suspend fun add(user: User): Long {
        return dbContext.addUser(user)
    }

    override suspend fun getById(id: Long): User? {
        return dbContext.findById(id)
    }

    override suspend fun getUserByEmail(email: UserEmail): User? {
        // Infrastructure faz a conversão para tipo primitivo
        return dbContext.findUserByEmail(email.value)
    }

    override suspend fun getAll(): List<User> {
        return dbContext.getAllUsers()
    }

    override suspend fun update(user: User): Boolean {
        return dbContext.updateUser(user)
    }

    override suspend fun addRole(userId: Long, role: Role): Boolean {
        return dbContext.addRoleToUser(userId, role)
    }

    override suspend fun removeRole(userId: Long, role: Role): Boolean {
        return dbContext.removeRoleFromUser(userId, role)
    }
}
