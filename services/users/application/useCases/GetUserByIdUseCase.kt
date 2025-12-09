package users.application.useCases

import users.application.ports.outbound.IUserRepository
import users.domain.User

class GetUserByIdUseCase(private val userRepository: IUserRepository) {
    
    suspend fun execute(userId: Long): User? {
        return userRepository.getById(userId)
    }
}
