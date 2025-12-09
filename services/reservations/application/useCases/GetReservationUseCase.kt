package reservations.application.useCases

import java.util.UUID
import reservations.application.ports.outbound.IUnitOfWork
import reservations.domain.Reservation

/** Use case para buscar uma reserva por ID. */
class GetReservationUseCase(private val unitOfWork: IUnitOfWork) {
    fun execute(reservationId: UUID): Reservation? {
        return unitOfWork.reservationRepository.findById(reservationId)
    }
}
