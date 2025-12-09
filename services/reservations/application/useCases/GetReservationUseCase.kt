package reservations.application.useCases

import java.util.UUID
import reservations.application.ports.outbound.IReservationRepository
import reservations.domain.Reservation

/** Use case para buscar uma reserva por ID. */
class GetReservationUseCase(private val reservationRepository: IReservationRepository) {
    fun execute(reservationId: UUID): Reservation? {
        return reservationRepository.findById(reservationId)
    }
}
