package users.infrastructure.persistence

import users.domain.User

class DatabaseContext {
    // Simulação de operações de banco de dados
    private val users = mutableListOf<User>()
    
    // Simulação de transação com rollback real
    suspend fun <T> executeTransaction(block: suspend () -> T): T {
        println("Iniciando transação...")
        // Cria snapshot do estado atual para rollback
        val snapshot = users.toList()
        try {
            val result = block()
            println("Transação concluída com sucesso (commit).")
            return result
        } catch (e: Exception) {
            println("Transação falhou (rollback): ${e.message}")
            // Restaura o estado anterior (rollback)
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
}

