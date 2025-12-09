package users.application.ports.outbound

import users.domain.Role
import users.domain.User
import users.domain.valueObjects.UserEmail

interface IUserRepository {
    suspend fun add(user: User): Long
    suspend fun getById(id: Long): User?
    suspend fun getUserByEmail(email: UserEmail): User?
    suspend fun getAll(): List<User>
    suspend fun update(user: User): Boolean
    suspend fun addRole(userId: Long, role: Role): Boolean
    suspend fun removeRole(userId: Long, role: Role): Boolean
}
