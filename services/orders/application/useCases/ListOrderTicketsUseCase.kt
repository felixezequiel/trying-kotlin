package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IIssuedTicketRepository
import orders.application.ports.outbound.IOrderRepository
import orders.domain.IssuedTicket

class ListOrderTicketsUseCase(
        private val orderRepository: IOrderRepository,
        private val issuedTicketRepository: IIssuedTicketRepository
) {

    fun execute(customerId: UUID, orderId: UUID): List<IssuedTicket> {
        // Busca o pedido
        val order =
                orderRepository.findById(orderId)
                        ?: throw IllegalArgumentException("Pedido não encontrado")

        // Valida que o pedido pertence ao customer
        if (order.customerId != customerId) {
            throw IllegalArgumentException("Pedido não pertence ao customer")
        }

        return issuedTicketRepository.findByOrderId(orderId)
    }
}
