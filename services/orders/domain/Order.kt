package orders.domain

import java.time.Instant
import java.util.UUID
import orders.domain.valueObjects.Price

data class Order(
        val id: UUID = UUID.randomUUID(),
        val customerId: UUID,
        val reservationId: UUID,
        val eventId: UUID,
        val items: List<OrderItem>,
        val totalAmount: Price,
        val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
        val paymentMethod: String? = null,
        val transactionId: String? = null,
        val createdAt: Instant = Instant.now(),
        val paidAt: Instant? = null,
        val refundedAt: Instant? = null
) {
    init {
        require(items.isNotEmpty()) { "Order deve ter pelo menos 1 item" }
    }

    fun startProcessing(paymentMethod: String): Order {
        require(paymentStatus == PaymentStatus.PENDING) {
            "Só pode processar pagamento de order PENDING"
        }
        return copy(paymentStatus = PaymentStatus.PROCESSING, paymentMethod = paymentMethod)
    }

    fun markAsPaid(transactionId: String): Order {
        require(paymentStatus == PaymentStatus.PROCESSING) {
            "Só pode marcar como pago order PROCESSING"
        }
        return copy(
                paymentStatus = PaymentStatus.PAID,
                transactionId = transactionId,
                paidAt = Instant.now()
        )
    }

    fun markAsFailed(): Order {
        require(paymentStatus == PaymentStatus.PROCESSING) {
            "Só pode marcar como falho order PROCESSING"
        }
        return copy(paymentStatus = PaymentStatus.FAILED)
    }

    fun refund(): Order {
        require(paymentStatus == PaymentStatus.PAID) { "Só pode reembolsar order PAID" }
        return copy(paymentStatus = PaymentStatus.REFUNDED, refundedAt = Instant.now())
    }

    companion object {
        fun create(
                customerId: UUID,
                reservationId: UUID,
                eventId: UUID,
                items: List<OrderItem>
        ): Order {
            val totalAmount = items.fold(Price.ZERO) { acc, item -> acc + item.subtotal }
            return Order(
                    customerId = customerId,
                    reservationId = reservationId,
                    eventId = eventId,
                    items = items,
                    totalAmount = totalAmount
            )
        }
    }
}
