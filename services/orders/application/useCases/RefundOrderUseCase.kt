package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IPaymentGateway
import orders.application.ports.outbound.IUnitOfWork
import orders.domain.Order
import orders.domain.PaymentStatus
import orders.domain.TicketStatus

class RefundOrderUseCase(
        private val unitOfWork: IUnitOfWork,
        private val paymentGateway: IPaymentGateway
) {

    suspend fun execute(orderId: UUID, reason: String?): Order {
        return unitOfWork.runInTransaction {
            // Busca o pedido
            val order =
                    unitOfWork.orderRepository.findById(orderId)
                            ?: throw IllegalArgumentException("Pedido não encontrado")

            // Valida que o pedido está PAID
            if (order.paymentStatus != PaymentStatus.PAID) {
                throw IllegalStateException("Só pode reembolsar pedido pago")
            }

            // Busca os ingressos do pedido
            val tickets = unitOfWork.issuedTicketRepository.findByOrderId(orderId)

            // Verifica se algum ingresso já foi usado
            val usedTickets = tickets.filter { it.status == TicketStatus.USED }
            if (usedTickets.isNotEmpty()) {
                throw IllegalStateException(
                        "Não pode reembolsar: ${usedTickets.size} ingresso(s) já utilizado(s)"
                )
            }

            // Processa reembolso no gateway
            val transactionId =
                    order.transactionId
                            ?: throw IllegalStateException(
                                    "Pedido não possui transação de pagamento"
                            )

            val refundResult = paymentGateway.refund(transactionId, order.totalAmount.value)

            if (!refundResult.success) {
                throw IllegalStateException("Falha no reembolso: ${refundResult.errorMessage}")
            }

            // Cancela todos os ingressos
            val cancelledTickets =
                    tickets.filter { it.status == TicketStatus.VALID }.map { it.cancel() }
            unitOfWork.issuedTicketRepository.updateAll(cancelledTickets)

            // Marca pedido como reembolsado
            val refundedOrder = order.refund()
            unitOfWork.orderRepository.update(refundedOrder)
        }
    }
}
