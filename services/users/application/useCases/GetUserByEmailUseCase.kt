package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.User

class GetUserByEmailUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun execute(email: String): User? {
        return unitOfWork.userRepository().getUserByEmail(email)
    }
}
