package orders.application.useCases

import java.math.BigDecimal
import java.util.UUID
import orders.adapters.outbound.InMemoryOrderStore
import orders.adapters.outbound.UnitOfWorkAdapter
import orders.domain.Order
import orders.domain.OrderItem
import orders.domain.valueObjects.Price
import orders.domain.valueObjects.Quantity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetOrderUseCaseTest {

    private lateinit var orderStore: InMemoryOrderStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var useCase: GetOrderUseCase

    @BeforeEach
    fun setup() {
        orderStore = InMemoryOrderStore()
        unitOfWork =
                UnitOfWorkAdapter(
                        orderStore.orderRepository,
                        orderStore.issuedTicketRepository,
                        orderStore.transactionManager
                )
        useCase = GetOrderUseCase(unitOfWork)
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
        orderStore.orderRepository.save(order)

        val result = useCase.execute(order.id)

        assertNotNull(result)
        assertEquals(order.id, result!!.id)
    }

    @Test
    fun `deve retornar null para order inexistente`() {
        val result = useCase.execute(UUID.randomUUID())
        assertNull(result)
    }
}
