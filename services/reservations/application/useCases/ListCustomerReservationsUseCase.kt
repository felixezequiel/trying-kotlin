package reservations.application.useCases

import java.util.UUID
import reservations.application.ports.outbound.IUnitOfWork
import reservations.domain.Reservation

/**
 * Use case para listar reservas de um cliente.
 *
 * Regras de negócio:
 * - CUSTOMER só pode ver suas próprias reservas
 */
class ListCustomerReservationsUseCase(private val unitOfWork: IUnitOfWork) {
    fun execute(customerId: UUID): List<Reservation> {
        return unitOfWork.reservationRepository.findByCustomerId(customerId)
    }
}
