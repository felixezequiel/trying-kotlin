package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.User
import users.domain.valueObjects.UserEmail

class GetUserByEmailUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun execute(email: String): User? {
        val userEmail = UserEmail.of(email)
        return unitOfWork.userRepository.getUserByEmail(userEmail)
    }
}
