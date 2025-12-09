package orders.domain

import java.math.BigDecimal
import java.util.UUID
import orders.domain.valueObjects.Price
import orders.domain.valueObjects.Quantity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OrderItemTest {

    @Test
    fun `deve criar OrderItem com subtotal calculado`() {
        val item =
                OrderItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.of(3),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )

        assertEquals("VIP", item.ticketTypeName)
        assertEquals(3, item.quantity.value)
        assertEquals(BigDecimal("100.00"), item.unitPrice.value)
        assertEquals(BigDecimal("300.00"), item.subtotal.value)
    }

    @Test
    fun `deve calcular subtotal corretamente para quantidade 1`() {
        val item =
                OrderItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "Pista",
                        quantity = Quantity.of(1),
                        unitPrice = Price.of(BigDecimal("50.00"))
                )

        assertEquals(BigDecimal("50.00"), item.subtotal.value)
    }

    @Test
    fun `deve gerar ID unico`() {
        val item1 =
                OrderItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.of(1),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        val item2 =
                OrderItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.of(1),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )

        assertNotEquals(item1.id, item2.id)
    }
}
