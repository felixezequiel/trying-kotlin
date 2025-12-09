package users.application.useCases

import users.application.ports.outbound.IUnitOfWork
import users.domain.Role

class RemoveRoleFromUserUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(userId: Long, role: Role): Boolean {
        val user =
                unitOfWork.userRepository.getById(userId)
                        ?: throw IllegalArgumentException("Usuário não encontrado")

        if (!user.roles.contains(role)) {
            return true // Já não possui o role
        }

        // Não permite remover CUSTOMER se for o único role
        if (role == Role.CUSTOMER && user.roles.size == 1) {
            throw IllegalStateException(
                    "Não é possível remover o role CUSTOMER quando é o único role do usuário"
            )
        }

        return unitOfWork.userRepository.removeRole(userId, role)
    }
}
