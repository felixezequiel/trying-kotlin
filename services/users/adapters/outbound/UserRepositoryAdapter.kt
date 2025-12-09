package users.adapters.outbound

import users.domain.User
import users.application.ports.outbound.IUserRepository
import users.infrastructure.persistence.DatabaseContext

class UserRepositoryAdapter(private val dbContext: DatabaseContext) : IUserRepository {
    override suspend fun add(user: User): Long {
        return dbContext.addUser(user)
    }

    override suspend fun getUserByEmail(email: String): User? {
        return dbContext.findUserByEmail(email)
    }

    override suspend fun getAll(): List<User> {
        return dbContext.getAllUsers()
    }
}
