package users.infrastructure.persistence

import users.domain.Role
import users.domain.User
import java.time.Instant

class DatabaseContext {
    private val users = mutableListOf<User>()
    
    suspend fun <T> executeTransaction(block: suspend () -> T): T {
        println("Iniciando transação...")
        val snapshot = users.toList()
        try {
            val result = block()
            println("Transação concluída com sucesso (commit).")
            return result
        } catch (e: Exception) {
            println("Transação falhou (rollback): ${e.message}")
            users.clear()
            users.addAll(snapshot)
            throw e
        }
    }
    
    fun addUser(user: User): Long {
        if (users.any { it.email == user.email }) {
            throw IllegalArgumentException("E-mail já cadastrado")
        }
        val newId = users.size + 1L
        val now = Instant.now()
        users.add(user.copy(id = newId, createdAt = now, updatedAt = now))
        return newId
    }
    
    fun findById(id: Long): User? {
        return users.find { it.id == id }
    }
    
    fun findUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    fun getAllUsers(): List<User> {
        return users.toList()
    }
    
    fun updateUser(user: User): Boolean {
        val index = users.indexOfFirst { it.id == user.id }
        if (index == -1) return false
        users[index] = user.copy(updatedAt = Instant.now())
        return true
    }
    
    fun addRoleToUser(userId: Long, role: Role): Boolean {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return false
        val user = users[index]
        if (user.roles.contains(role)) return true // Já possui o role
        users[index] = user.copy(
            roles = user.roles + role,
            updatedAt = Instant.now()
        )
        return true
    }
    
    fun removeRoleFromUser(userId: Long, role: Role): Boolean {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return false
        val user = users[index]
        if (!user.roles.contains(role)) return true // Já não possui o role
        // Não permite remover CUSTOMER se for o único role
        if (role == Role.CUSTOMER && user.roles.size == 1) {
            throw IllegalStateException("Não é possível remover o role CUSTOMER quando é o único role do usuário")
        }
        users[index] = user.copy(
            roles = user.roles - role,
            updatedAt = Instant.now()
        )
        return true
    }
}
