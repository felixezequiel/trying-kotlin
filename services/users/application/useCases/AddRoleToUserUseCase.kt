package users.application.useCases

import users.application.ports.outbound.IUserRepository
import users.domain.Role

class AddRoleToUserUseCase(private val userRepository: IUserRepository) {
    
    suspend fun execute(userId: Long, role: Role): Boolean {
        val user = userRepository.getById(userId)
            ?: throw IllegalArgumentException("Usuário não encontrado")
        
        if (user.roles.contains(role)) {
            return true // Já possui o role
        }
        
        return userRepository.addRole(userId, role)
    }
}
