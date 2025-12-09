package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.User

class GetUserByIdUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(userId: Long): User? {
        return unitOfWork.userRepository.getById(userId)
    }
}
