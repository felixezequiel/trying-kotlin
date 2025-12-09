package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IUnitOfWork
import orders.domain.IssuedTicket

class ListOrderTicketsUseCase(private val unitOfWork: IUnitOfWork) {

    fun execute(customerId: UUID, orderId: UUID): List<IssuedTicket> {
        // Busca o pedido
        val order =
                unitOfWork.orderRepository.findById(orderId)
                        ?: throw IllegalArgumentException("Pedido não encontrado")

        // Valida que o pedido pertence ao customer
        if (order.customerId != customerId) {
            throw IllegalArgumentException("Pedido não pertence ao customer")
        }

        return unitOfWork.issuedTicketRepository.findByOrderId(orderId)
    }
}
