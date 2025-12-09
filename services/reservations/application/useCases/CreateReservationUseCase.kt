package reservations.application.useCases

import java.util.UUID
import reservations.application.dto.CreateReservationRequest
import reservations.application.ports.outbound.IReservationRepository
import reservations.application.ports.outbound.ITicketsClient
import reservations.application.ports.outbound.TicketTypeInfo
import reservations.domain.Reservation
import reservations.domain.ReservationItem
import reservations.domain.valueObjects.Quantity

/**
 * Use case para criar uma reserva.
 *
 * Regras de negócio:
 * - RN-R01: Apenas CUSTOMER pode criar reserva (validado no controller)
 * - RN-R02: Reserva deve ter pelo menos 1 item
 * - RN-R03: Quantidade por item deve respeitar maxPerCustomer do TicketType
 * - RN-R04: Todos os TicketTypes devem ser do mesmo Event
 * - RN-R05: Event deve estar PUBLISHED (validado pelo Tickets Service)
 */
class CreateReservationUseCase(
        private val reservationRepository: IReservationRepository,
        private val ticketsClient: ITicketsClient
) {
    fun execute(customerId: UUID, request: CreateReservationRequest): Reservation {
        require(request.items.isNotEmpty()) { "Reserva deve ter pelo menos 1 item" }

        val eventId = UUID.fromString(request.eventId)
        val reservedItems = mutableListOf<ReservedItemInfo>()

        try {
            // Reserva cada item no Tickets Service
            for (itemRequest in request.items) {
                val ticketTypeId = UUID.fromString(itemRequest.ticketTypeId)
                val quantity = Quantity.positive(itemRequest.quantity)

                // Valida que o ticket type pertence ao evento correto
                val ticketInfo =
                        ticketsClient.getTicketType(ticketTypeId)
                                ?: throw IllegalArgumentException(
                                        "Tipo de ingresso não encontrado: $ticketTypeId"
                                )

                if (ticketInfo.eventId != eventId) {
                    throw IllegalArgumentException("Todos os ingressos devem ser do mesmo evento")
                }

                // Valida maxPerCustomer
                if (quantity > ticketInfo.maxPerCustomer) {
                    throw IllegalArgumentException(
                            "Quantidade máxima por cliente é ${ticketInfo.maxPerCustomer.value} para ${ticketInfo.name}"
                    )
                }

                // Reserva no Tickets Service
                val reservedInfo = ticketsClient.reserve(ticketTypeId, quantity)
                reservedItems.add(ReservedItemInfo(ticketTypeId, quantity, reservedInfo))
            }

            // Cria os itens da reserva
            val items =
                    reservedItems.map { reserved ->
                        ReservationItem.create(
                                ticketTypeId = reserved.ticketTypeId,
                                ticketTypeName = reserved.ticketInfo.name,
                                quantity = reserved.quantity,
                                unitPrice = reserved.ticketInfo.price
                        )
                    }

            // Cria e salva a reserva
            val reservation =
                    Reservation.create(customerId = customerId, eventId = eventId, items = items)

            return reservationRepository.save(reservation)
        } catch (e: Exception) {
            // Rollback: libera os ingressos já reservados
            for (reserved in reservedItems) {
                try {
                    ticketsClient.release(reserved.ticketTypeId, reserved.quantity)
                } catch (releaseError: Exception) {
                    // Log error but continue rollback
                    println(
                            "Erro ao liberar ingresso ${reserved.ticketTypeId}: ${releaseError.message}"
                    )
                }
            }
            throw e
        }
    }

    private data class ReservedItemInfo(
            val ticketTypeId: UUID,
            val quantity: Quantity,
            val ticketInfo: TicketTypeInfo
    )
}
