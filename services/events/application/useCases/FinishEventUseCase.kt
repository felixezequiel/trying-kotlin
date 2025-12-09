package events.application.useCases

import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.EventStatus
import java.time.Instant
import java.util.UUID

class FinishEventUseCase(private val eventRepository: IEventRepository) {

    suspend fun execute(eventId: UUID, partnerId: UUID?, isAdmin: Boolean = false): Event {
        val event =
                eventRepository.getById(eventId)
                        ?: throw IllegalArgumentException("Evento não encontrado")

        // Verificar permissão: dono do evento ou admin
        if (!isAdmin && (partnerId == null || event.partnerId != partnerId)) {
            throw IllegalStateException("Você não tem permissão para finalizar este evento")
        }

        // Só pode finalizar evento PUBLISHED
        if (event.status != EventStatus.PUBLISHED) {
            throw IllegalStateException("Apenas eventos publicados podem ser finalizados")
        }

        val finishedEvent = event.copy(status = EventStatus.FINISHED, updatedAt = Instant.now())

        eventRepository.update(finishedEvent)
        return finishedEvent
    }
}
