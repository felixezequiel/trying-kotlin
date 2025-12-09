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

class GetOrderUseCaseTest {

    private lateinit var orderRepository: FakeOrderRepository
    private lateinit var useCase: GetOrderUseCase

    @BeforeEach
    fun setup() {
        orderRepository = FakeOrderRepository()
        useCase = GetOrderUseCase(orderRepository)
    }

    private fun createOrder(): Order {
        val item =
                OrderItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.of(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        return Order.create(
                customerId = UUID.randomUUID(),
                reservationId = UUID.randomUUID(),
                eventId = UUID.randomUUID(),
                items = listOf(item)
        )
    }

    @Test
    fun `deve retornar order existente`() {
        val order = createOrder()
        orderRepository.save(order)

        val result = useCase.execute(order.id)

        assertNotNull(result)
        assertEquals(order.id, result!!.id)
    }

    @Test
    fun `deve retornar null para order inexistente`() {
        val result = useCase.execute(UUID.randomUUID())
        assertNull(result)
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
