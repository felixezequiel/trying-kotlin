package users.application.ports.outbound

import users.domain.User

interface IUserRepository {
    suspend fun add(user: User): Long
    suspend fun getUserByEmail(email: String): User?
    suspend fun getAll(): List<User>
}
