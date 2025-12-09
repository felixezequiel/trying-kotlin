package services.reservations

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import reservations.application.ports.outbound.ITicketsClient
import reservations.application.ports.outbound.TicketTypeInfo
import reservations.domain.valueObjects.Quantity

/** Fake client para testes unitários. */
class FakeTicketsClient : ITicketsClient {
    private val ticketTypes = ConcurrentHashMap<UUID, TicketTypeInfo>()
    private val reservedQuantities = ConcurrentHashMap<UUID, Int>()

    fun addTicketType(ticketTypeInfo: TicketTypeInfo) {
        ticketTypes[ticketTypeInfo.id] = ticketTypeInfo
        reservedQuantities[ticketTypeInfo.id] = 0
    }

    override fun reserve(ticketTypeId: UUID, quantity: Quantity): TicketTypeInfo {
        val ticketType =
                ticketTypes[ticketTypeId]
                        ?: throw IllegalArgumentException(
                                "Tipo de ingresso não encontrado: $ticketTypeId"
                        )

        val currentReserved = reservedQuantities[ticketTypeId] ?: 0
        val available = ticketType.availableQuantity.value - currentReserved

        if (quantity.value > available) {
            throw IllegalStateException("Não há ingressos disponíveis suficientes")
        }

        reservedQuantities[ticketTypeId] = currentReserved + quantity.value
        return ticketType
    }

    override fun release(ticketTypeId: UUID, quantity: Quantity) {
        val currentReserved = reservedQuantities[ticketTypeId] ?: 0
        reservedQuantities[ticketTypeId] = maxOf(0, currentReserved - quantity.value)
    }

    override fun getTicketType(ticketTypeId: UUID): TicketTypeInfo? {
        return ticketTypes[ticketTypeId]
    }

    fun getReservedQuantity(ticketTypeId: UUID): Int {
        return reservedQuantities[ticketTypeId] ?: 0
    }

    fun clear() {
        ticketTypes.clear()
        reservedQuantities.clear()
    }
}
