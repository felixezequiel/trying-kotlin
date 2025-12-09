package orders.application.useCases

import java.util.UUID
import kotlinx.coroutines.runBlocking
import orders.adapters.outbound.InMemoryOrderStore
import orders.adapters.outbound.UnitOfWorkAdapter
import orders.domain.IssuedTicket
import orders.domain.TicketStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidateTicketUseCaseTest {

    private lateinit var orderStore: InMemoryOrderStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var useCase: ValidateTicketUseCase

    @BeforeEach
    fun setup() {
        orderStore = InMemoryOrderStore()
        unitOfWork =
                UnitOfWorkAdapter(
                        orderStore.orderRepository,
                        orderStore.issuedTicketRepository,
                        orderStore.transactionManager
                )
        useCase = ValidateTicketUseCase(unitOfWork)
    }

    private fun createTicket(): IssuedTicket {
        return IssuedTicket.create(
                orderId = UUID.randomUUID(),
                orderItemId = UUID.randomUUID(),
                ticketTypeId = UUID.randomUUID(),
                ticketTypeName = "VIP",
                eventId = UUID.randomUUID(),
                eventName = "Show",
                customerId = UUID.randomUUID()
        )
    }

    @Test
    fun `deve validar ingresso valido`() = runBlocking {
        val ticket = createTicket()
        orderStore.issuedTicketRepository.save(ticket)

        val validatedTicket = useCase.execute(ticket.code.value)

        assertEquals(TicketStatus.USED, validatedTicket.status)
        assertNotNull(validatedTicket.usedAt)
    }

    @Test
    fun `deve lancar excecao para ingresso nao encontrado`() = runBlocking {
        val exception = assertThrows<IllegalArgumentException> { useCase.execute("TKT-NOTFOUND") }
        assertTrue(exception.message!!.contains("não encontrado"))
    }

    @Test
    fun `deve lancar excecao para ingresso ja usado`() = runBlocking {
        val ticket = createTicket()
        orderStore.issuedTicketRepository.save(ticket)

        // Usa o ingresso
        useCase.execute(ticket.code.value)

        // Tenta usar novamente
        val exception = assertThrows<IllegalStateException> { useCase.execute(ticket.code.value) }
        assertTrue(exception.message!!.contains("já utilizado"))
    }

    @Test
    fun `deve lancar excecao para ingresso cancelado`() = runBlocking {
        val ticket = createTicket().cancel()
        orderStore.issuedTicketRepository.save(ticket)

        val exception = assertThrows<IllegalStateException> { useCase.execute(ticket.code.value) }
        assertTrue(exception.message!!.contains("cancelado"))
    }
}
