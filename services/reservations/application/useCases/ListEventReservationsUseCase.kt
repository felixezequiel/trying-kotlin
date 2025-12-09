package reservations.application.useCases

import java.util.UUID
import reservations.application.ports.outbound.IReservationRepository
import reservations.domain.Reservation

/**
 * Use case para listar reservas de um evento.
 *
 * Regras de negócio:
 * - PARTNER só pode ver reservas de seus eventos
 * - ADMIN pode ver reservas de qualquer evento
 */
class ListEventReservationsUseCase(private val reservationRepository: IReservationRepository) {
    fun execute(eventId: UUID): List<Reservation> {
        return reservationRepository.findByEventId(eventId)
    }
}
