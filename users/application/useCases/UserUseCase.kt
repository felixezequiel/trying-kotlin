package users.application.useCases

import users.domain.User
import users.application.ports.out.IUnitOfWork

class UserUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun registerUser(name: String, email: String) {
        // Lógica de negócio, validações, etc.
        if (name.isBlank() || email.isBlank()) {
            throw IllegalArgumentException("Nome e e-mail não podem estar vazios.")
        }

        // Usa a UoW para agrupar operações em uma única transação
        unitOfWork.runInTransaction {
            val userRepository = unitOfWork.userRepository()
            
            // Verifica se o usuário já existe antes de adicionar
            if (userRepository.getUserByEmail(email) != null) {
                throw IllegalStateException("Usuário com este e-mail já existe.")
            }
            
            val newUser = User(name = name, email = email)
            val userId = userRepository.add(newUser)
            println("Usuário registrado com ID: $userId")
            
            // Se houvesse outra operação crítica (ex: adicionar perfil, enviar email), 
            // ela estaria aqui dentro do runInTransaction() e tudo falharia ou teria sucesso junto.
        }
    }
}

