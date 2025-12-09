package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.User

class GetAllUsersUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun execute(): List<User> {
        return unitOfWork.userRepository.getAll()
    }
}
