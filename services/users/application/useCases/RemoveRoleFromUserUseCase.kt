package users.application.useCases

import users.application.ports.outbound.IUserRepository
import users.domain.Role

class RemoveRoleFromUserUseCase(private val userRepository: IUserRepository) {
    
    suspend fun execute(userId: Long, role: Role): Boolean {
        val user = userRepository.getById(userId)
            ?: throw IllegalArgumentException("Usuário não encontrado")
        
        if (!user.roles.contains(role)) {
            return true // Já não possui o role
        }
        
        // Não permite remover CUSTOMER se for o único role
        if (role == Role.CUSTOMER && user.roles.size == 1) {
            throw IllegalStateException("Não é possível remover o role CUSTOMER quando é o único role do usuário")
        }
        
        return userRepository.removeRole(userId, role)
    }
}
