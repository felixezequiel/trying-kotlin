package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.User
import users.domain.valueObjects.UserEmail
import users.domain.valueObjects.UserName

class UserUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun registerUser(name: String, email: String) {
        // Validação encapsulada nos Value Objects
        val userName = UserName.of(name)
        val userEmail = UserEmail.of(email)

        unitOfWork.runInTransaction {
            if (unitOfWork.userRepository.getUserByEmail(userEmail) != null) {
                throw IllegalStateException("Usuário com este e-mail já existe.")
            }

            val newUser = User(name = userName, email = userEmail)
            unitOfWork.userRepository.add(newUser)
        }
    }
}
