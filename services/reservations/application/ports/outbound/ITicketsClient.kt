package reservations.application.ports.outbound

import java.util.UUID
import reservations.domain.valueObjects.Price
import reservations.domain.valueObjects.Quantity

/** Port para comunicação com o Tickets Service. Responsável por reservar e liberar ingressos. */
interface ITicketsClient {
    /**
     * Reserva ingressos de um tipo específico.
     * @param ticketTypeId ID do tipo de ingresso
     * @param quantity Quantidade a reservar
     * @return Informações do ticket type (nome, preço) para desnormalização
     * @throws IllegalStateException se não houver ingressos disponíveis
     */
    fun reserve(ticketTypeId: UUID, quantity: Quantity): TicketTypeInfo

    /**
     * Libera ingressos previamente reservados.
     * @param ticketTypeId ID do tipo de ingresso
     * @param quantity Quantidade a liberar
     */
    fun release(ticketTypeId: UUID, quantity: Quantity)

    /**
     * Busca informações de um tipo de ingresso.
     * @param ticketTypeId ID do tipo de ingresso
     * @return Informações do ticket type ou null se não encontrado
     */
    fun getTicketType(ticketTypeId: UUID): TicketTypeInfo?
}

/** Informações do tipo de ingresso retornadas pelo Tickets Service. */
data class TicketTypeInfo(
        val id: UUID,
        val eventId: UUID,
        val name: String,
        val price: Price,
        val availableQuantity: Quantity,
        val maxPerCustomer: Quantity
)
