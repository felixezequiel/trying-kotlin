package orders.application.useCases

import java.util.UUID
import orders.application.dto.ProcessPaymentRequest
import orders.application.ports.outbound.IPaymentGateway
import orders.application.ports.outbound.IReservationsClient
import orders.application.ports.outbound.IUnitOfWork
import orders.application.ports.outbound.PaymentRequest
import orders.domain.IssuedTicket
import orders.domain.Order
import orders.domain.PaymentStatus

data class PaymentProcessResult(
        val order: Order,
        val tickets: List<IssuedTicket>?,
        val errorMessage: String?
)

class ProcessPaymentUseCase(
        private val unitOfWork: IUnitOfWork,
        private val paymentGateway: IPaymentGateway,
        private val reservationsClient: IReservationsClient
) {

        suspend fun execute(
                customerId: UUID,
                orderId: UUID,
                request: ProcessPaymentRequest
        ): PaymentProcessResult {
                return unitOfWork.runInTransaction {
                        // Busca o pedido
                        val order =
                                unitOfWork.orderRepository.findById(orderId)
                                        ?: throw IllegalArgumentException("Pedido não encontrado")

                        // Valida que o pedido pertence ao customer
                        if (order.customerId != customerId) {
                                throw IllegalArgumentException("Pedido não pertence ao customer")
                        }

                        // Valida que o pedido está PENDING
                        if (order.paymentStatus != PaymentStatus.PENDING) {
                                throw IllegalStateException("Pedido não está pendente de pagamento")
                        }

                        // Inicia processamento
                        val processingOrder = order.startProcessing(request.paymentMethod)
                        unitOfWork.orderRepository.update(processingOrder)

                        // Processa pagamento
                        val paymentResult =
                                paymentGateway.processPayment(
                                        PaymentRequest(
                                                orderId = orderId,
                                                amount = order.totalAmount.value,
                                                paymentMethod = request.paymentMethod,
                                                customerEmail =
                                                        "customer@example.com" // TODO: Buscar email
                                                // do customer
                                                )
                                )

                        if (paymentResult.success) {
                                // Marca como pago
                                val paidOrder =
                                        processingOrder.markAsPaid(paymentResult.transactionId!!)
                                unitOfWork.orderRepository.update(paidOrder)

                                // Converte a reserva
                                reservationsClient.convertReservation(order.reservationId, orderId)

                                // Busca informações do evento para os tickets
                                val reservation =
                                        reservationsClient.getReservation(order.reservationId)
                                val eventName = reservation?.eventName ?: "Evento"

                                // Emite os ingressos
                                val tickets = issueTickets(paidOrder, eventName)

                                PaymentProcessResult(
                                        order = paidOrder,
                                        tickets = tickets,
                                        errorMessage = null
                                )
                        } else {
                                // Marca como falho
                                val failedOrder = processingOrder.markAsFailed()
                                unitOfWork.orderRepository.update(failedOrder)

                                PaymentProcessResult(
                                        order = failedOrder,
                                        tickets = null,
                                        errorMessage = paymentResult.errorMessage
                                )
                        }
                }
        }

        private fun issueTickets(order: Order, eventName: String): List<IssuedTicket> {
                val tickets = mutableListOf<IssuedTicket>()

                for (item in order.items) {
                        // Gera um ingresso para cada unidade
                        repeat(item.quantity.value) {
                                val ticket =
                                        IssuedTicket.create(
                                                orderId = order.id,
                                                orderItemId = item.id,
                                                ticketTypeId = item.ticketTypeId,
                                                ticketTypeName = item.ticketTypeName,
                                                eventId = order.eventId,
                                                eventName = eventName,
                                                customerId = order.customerId
                                        )
                                tickets.add(ticket)
                        }
                }

                return unitOfWork.issuedTicketRepository.saveAll(tickets)
        }
}
