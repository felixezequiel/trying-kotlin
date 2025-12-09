package orders.application.useCases

import java.math.BigDecimal
import java.util.UUID
import orders.application.ports.outbound.IOrderRepository
import orders.domain.Order
import orders.domain.OrderItem
import orders.domain.valueObjects.Price
import orders.domain.valueObjects.Quantity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ListCustomerOrdersUseCaseTest {

    private lateinit var orderRepository: FakeOrderRepository
    private lateinit var useCase: ListCustomerOrdersUseCase

    @BeforeEach
    fun setup() {
        orderRepository = FakeOrderRepository()
        useCase = ListCustomerOrdersUseCase(orderRepository)
    }

    private fun createOrder(customerId: UUID): Order {
        val item =
                OrderItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.of(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        return Order.create(
                customerId = customerId,
                reservationId = UUID.randomUUID(),
                eventId = UUID.randomUUID(),
                items = listOf(item)
        )
    }

    @Test
    fun `deve retornar orders do customer`() {
        val customerId = UUID.randomUUID()
        val order1 = createOrder(customerId)
        val order2 = createOrder(customerId)
        val otherOrder = createOrder(UUID.randomUUID())

        orderRepository.save(order1)
        orderRepository.save(order2)
        orderRepository.save(otherOrder)

        val result = useCase.execute(customerId)

        assertEquals(2, result.size)
        assertTrue(result.all { it.customerId == customerId })
    }

    @Test
    fun `deve retornar lista vazia se customer nao tem orders`() {
        val customerId = UUID.randomUUID()
        val result = useCase.execute(customerId)
        assertTrue(result.isEmpty())
    }

    // Fake implementation
    private class FakeOrderRepository : IOrderRepository {
        private val orders = mutableMapOf<UUID, Order>()

        override fun save(order: Order): Order {
            orders[order.id] = order
            return order
        }

        override fun findById(id: UUID): Order? = orders[id]

        override fun findByCustomerId(customerId: UUID): List<Order> =
                orders.values.filter { it.customerId == customerId }

        override fun findByReservationId(reservationId: UUID): Order? =
                orders.values.find { it.reservationId == reservationId }

        override fun update(order: Order): Order {
            orders[order.id] = order
            return order
        }
    }
}
