package reservations.application.useCases

import java.util.UUID
import reservations.application.ports.outbound.ITicketsClient
import reservations.application.ports.outbound.IUnitOfWork
import reservations.domain.CancellationType
import reservations.domain.Reservation
import reservations.domain.ReservationStatus

/**
 * Use case para cancelar uma reserva.
 *
 * Regras de negócio:
 * - RN-R07: Só pode cancelar reserva ACTIVE
 * - RN-R08: Customer só cancela própria reserva
 * - RN-R09: Partner só cancela reservas do seu evento
 * - RN-R10: Cancelamento libera ingressos imediatamente
 */
class CancelReservationUseCase(
        private val unitOfWork: IUnitOfWork,
        private val ticketsClient: ITicketsClient
) {
    fun execute(
            reservationId: UUID,
            cancelledBy: UUID,
            reason: String?,
            cancellationType: CancellationType
    ): Reservation {
        val reservation =
                unitOfWork.reservationRepository.findById(reservationId)
                        ?: throw IllegalArgumentException("Reserva não encontrada: $reservationId")

        require(reservation.status == ReservationStatus.ACTIVE) {
            "Só pode cancelar reserva ACTIVE"
        }

        // Libera os ingressos no Tickets Service
        for (item in reservation.items) {
            try {
                ticketsClient.release(item.ticketTypeId, item.quantity)
            } catch (e: Exception) {
                // Log error but continue - melhor liberar parcialmente do que não liberar nada
                println("Erro ao liberar ingresso ${item.ticketTypeId}: ${e.message}")
            }
        }

        // Cancela a reserva
        val cancelledReservation =
                reservation.cancel(
                        cancelledBy = cancelledBy,
                        reason = reason,
                        cancellationType = cancellationType
                )

        return unitOfWork.reservationRepository.update(cancelledReservation)
    }
}
