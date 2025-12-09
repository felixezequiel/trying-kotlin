package reservations.application.useCases

import java.util.UUID
import reservations.application.ports.outbound.IUnitOfWork
import reservations.domain.Reservation
import reservations.domain.ReservationStatus

/**
 * Use case para converter uma reserva em pedido. Chamado internamente pelo Orders Service quando um
 * pedido é criado.
 */
class ConvertReservationUseCase(private val unitOfWork: IUnitOfWork) {
    fun execute(reservationId: UUID, orderId: UUID): Reservation {
        val reservation =
                unitOfWork.reservationRepository.findById(reservationId)
                        ?: throw IllegalArgumentException("Reserva não encontrada: $reservationId")

        require(reservation.status == ReservationStatus.ACTIVE) {
            "Só pode converter reserva ACTIVE"
        }

        val convertedReservation = reservation.convert(orderId)
        return unitOfWork.reservationRepository.update(convertedReservation)
    }
}
