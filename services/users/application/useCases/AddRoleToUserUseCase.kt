package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.Role

class AddRoleToUserUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(userId: Long, role: Role): Boolean {
        val user =
                unitOfWork.userRepository.getById(userId)
                        ?: throw IllegalArgumentException("Usuário não encontrado")

        if (user.roles.contains(role)) {
            return true // Já possui o role
        }

        return unitOfWork.userRepository.addRole(userId, role)
    }
}
