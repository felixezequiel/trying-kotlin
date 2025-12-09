package users.application.useCases

import users.domain.User
import users.application.ports.outbound.IUnitOfWork

class UserUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun registerUser(name: String, email: String) {
        if (name.isBlank() || email.isBlank()) {
            throw IllegalArgumentException("Nome e e-mail não podem estar vazios.")
        }

        unitOfWork.runInTransaction {
            val userRepository = unitOfWork.userRepository()
            
            if (userRepository.getUserByEmail(email) != null) {
                throw IllegalStateException("Usuário com este e-mail já existe.")
            }
            
            val newUser = User(name = name, email = email)
            val userId = userRepository.add(newUser)
            println("Usuário registrado com ID: $userId")
        }
    }
}
