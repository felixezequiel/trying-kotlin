package orders.domain

import java.math.BigDecimal
import java.util.UUID
import orders.domain.valueObjects.Price
import orders.domain.valueObjects.Quantity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderTest {

    private fun createOrderItem(
            ticketTypeName: String = "VIP",
            quantity: Int = 2,
            unitPrice: BigDecimal = BigDecimal("100.00")
    ): OrderItem {
        return OrderItem.create(
                ticketTypeId = UUID.randomUUID(),
                ticketTypeName = ticketTypeName,
                quantity = Quantity.of(quantity),
                unitPrice = Price.of(unitPrice)
        )
    }

    private fun createOrder(items: List<OrderItem> = listOf(createOrderItem())): Order {
        return Order.create(
                customerId = UUID.randomUUID(),
                reservationId = UUID.randomUUID(),
                eventId = UUID.randomUUID(),
                items = items
        )
    }

    @Test
    fun `deve criar Order com itens`() {
        val item1 = createOrderItem("VIP", 2, BigDecimal("100.00"))
        val item2 = createOrderItem("Pista", 3, BigDecimal("50.00"))
        val order = createOrder(listOf(item1, item2))

        assertEquals(2, order.items.size)
        assertEquals(PaymentStatus.PENDING, order.paymentStatus)
        assertNull(order.paymentMethod)
        assertNull(order.transactionId)
        assertNull(order.paidAt)
    }

    @Test
    fun `deve calcular total corretamente`() {
        val item1 = createOrderItem("VIP", 2, BigDecimal("100.00")) // 200.00
        val item2 = createOrderItem("Pista", 3, BigDecimal("50.00")) // 150.00
        val order = createOrder(listOf(item1, item2))

        assertEquals(BigDecimal("350.00"), order.totalAmount.value)
    }

    @Test
    fun `deve lancar excecao para Order sem itens`() {
        val exception =
                assertThrows<IllegalArgumentException> {
                    Order.create(
                            customerId = UUID.randomUUID(),
                            reservationId = UUID.randomUUID(),
                            eventId = UUID.randomUUID(),
                            items = emptyList()
                    )
                }
        assertTrue(exception.message!!.contains("pelo menos 1 item"))
    }

    @Test
    fun `deve iniciar processamento de pagamento`() {
        val order = createOrder()
        val processingOrder = order.startProcessing("credit_card")

        assertEquals(PaymentStatus.PROCESSING, processingOrder.paymentStatus)
        assertEquals("credit_card", processingOrder.paymentMethod)
    }

    @Test
    fun `nao deve iniciar processamento se nao estiver PENDING`() {
        val order = createOrder().startProcessing("credit_card")
        val exception = assertThrows<IllegalArgumentException> { order.startProcessing("pix") }
        assertTrue(exception.message!!.contains("PENDING"))
    }

    @Test
    fun `deve marcar como pago`() {
        val order = createOrder().startProcessing("credit_card")
        val paidOrder = order.markAsPaid("TXN-12345678")

        assertEquals(PaymentStatus.PAID, paidOrder.paymentStatus)
        assertEquals("TXN-12345678", paidOrder.transactionId)
        assertNotNull(paidOrder.paidAt)
    }

    @Test
    fun `nao deve marcar como pago se nao estiver PROCESSING`() {
        val order = createOrder()
        val exception = assertThrows<IllegalArgumentException> { order.markAsPaid("TXN-12345678") }
        assertTrue(exception.message!!.contains("PROCESSING"))
    }

    @Test
    fun `deve marcar como falho`() {
        val order = createOrder().startProcessing("credit_card")
        val failedOrder = order.markAsFailed()

        assertEquals(PaymentStatus.FAILED, failedOrder.paymentStatus)
    }

    @Test
    fun `nao deve marcar como falho se nao estiver PROCESSING`() {
        val order = createOrder()
        val exception = assertThrows<IllegalArgumentException> { order.markAsFailed() }
        assertTrue(exception.message!!.contains("PROCESSING"))
    }

    @Test
    fun `deve reembolsar order pago`() {
        val order = createOrder().startProcessing("credit_card").markAsPaid("TXN-12345678")
        val refundedOrder = order.refund()

        assertEquals(PaymentStatus.REFUNDED, refundedOrder.paymentStatus)
        assertNotNull(refundedOrder.refundedAt)
    }

    @Test
    fun `nao deve reembolsar se nao estiver PAID`() {
        val order = createOrder()
        val exception = assertThrows<IllegalArgumentException> { order.refund() }
        assertTrue(exception.message!!.contains("PAID"))
    }
}
