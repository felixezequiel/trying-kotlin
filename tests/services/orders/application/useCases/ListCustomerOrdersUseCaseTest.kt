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

class ListCustomerOrdersUseCaseTest {

    private lateinit var orderStore: InMemoryOrderStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var useCase: ListCustomerOrdersUseCase

    @BeforeEach
    fun setup() {
        orderStore = InMemoryOrderStore()
        unitOfWork =
                UnitOfWorkAdapter(
                        orderStore.orderRepository,
                        orderStore.issuedTicketRepository,
                        orderStore.transactionManager
                )
        useCase = ListCustomerOrdersUseCase(unitOfWork)
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

        orderStore.orderRepository.save(order1)
        orderStore.orderRepository.save(order2)
        orderStore.orderRepository.save(otherOrder)

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
}
