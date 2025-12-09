package users.infrastructure.persistence

import users.domain.User

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
        users.add(user.copy(id = newId))
        return newId
    }
    
    fun findUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    fun getAllUsers(): List<User> {
        return users.toList()
    }
}
