package events.application.useCases

import events.application.ports.outbound.IUnitOfWork
import events.domain.Event
import events.domain.EventStatus
import java.time.Instant
import java.util.UUID

class CancelEventUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(eventId: UUID, partnerId: UUID?, isAdmin: Boolean = false): Event {
        return unitOfWork.runInTransaction {
            val eventRepository = unitOfWork.eventRepository
            val event =
                    eventRepository.getById(eventId)
                            ?: throw IllegalArgumentException("Evento não encontrado")

            // Verificar permissão: dono do evento ou admin
            if (!isAdmin && (partnerId == null || event.partnerId != partnerId)) {
                throw IllegalStateException("Você não tem permissão para cancelar este evento")
            }

            // Não pode cancelar evento já FINISHED
            if (event.status == EventStatus.FINISHED) {
                throw IllegalStateException("Não é possível cancelar evento já finalizado")
            }

            // Não pode cancelar evento já CANCELLED
            if (event.status == EventStatus.CANCELLED) {
                throw IllegalStateException("Evento já está cancelado")
            }

            // RN-E07: Cancelar evento cancela todas as reservas
            // TODO: Notificar Tickets service para cancelar reservas

            val cancelledEvent =
                    event.copy(status = EventStatus.CANCELLED, updatedAt = Instant.now())

            eventRepository.update(cancelledEvent)
            cancelledEvent
        }
    }
}
