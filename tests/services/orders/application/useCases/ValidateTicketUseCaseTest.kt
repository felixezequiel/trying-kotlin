package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IIssuedTicketRepository
import orders.domain.IssuedTicket
import orders.domain.TicketStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidateTicketUseCaseTest {

    private lateinit var ticketRepository: FakeIssuedTicketRepository
    private lateinit var useCase: ValidateTicketUseCase

    @BeforeEach
    fun setup() {
        ticketRepository = FakeIssuedTicketRepository()
        useCase = ValidateTicketUseCase(ticketRepository)
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
    fun `deve validar ingresso valido`() {
        val ticket = createTicket()
        ticketRepository.save(ticket)

        val validatedTicket = useCase.execute(ticket.code.value)

        assertEquals(TicketStatus.USED, validatedTicket.status)
        assertNotNull(validatedTicket.usedAt)
    }

    @Test
    fun `deve lancar excecao para ingresso nao encontrado`() {
        val exception = assertThrows<IllegalArgumentException> { useCase.execute("TKT-NOTFOUND") }
        assertTrue(exception.message!!.contains("não encontrado"))
    }

    @Test
    fun `deve lancar excecao para ingresso ja usado`() {
        val ticket = createTicket()
        ticketRepository.save(ticket)

        // Usa o ingresso
        useCase.execute(ticket.code.value)

        // Tenta usar novamente
        val exception = assertThrows<IllegalStateException> { useCase.execute(ticket.code.value) }
        assertTrue(exception.message!!.contains("já utilizado"))
    }

    @Test
    fun `deve lancar excecao para ingresso cancelado`() {
        val ticket = createTicket().cancel()
        ticketRepository.save(ticket)

        val exception = assertThrows<IllegalStateException> { useCase.execute(ticket.code.value) }
        assertTrue(exception.message!!.contains("cancelado"))
    }

    // Fake implementation
    private class FakeIssuedTicketRepository : IIssuedTicketRepository {
        private val tickets = mutableMapOf<UUID, IssuedTicket>()

        override fun save(ticket: IssuedTicket): IssuedTicket {
            tickets[ticket.id] = ticket
            return ticket
        }

        override fun saveAll(tickets: List<IssuedTicket>): List<IssuedTicket> {
            tickets.forEach { this.tickets[it.id] = it }
            return tickets
        }

        override fun findById(id: UUID): IssuedTicket? = tickets[id]

        override fun findByCode(code: String): IssuedTicket? =
                tickets.values.find { it.code.value == code }

        override fun findByOrderId(orderId: UUID): List<IssuedTicket> =
                tickets.values.filter { it.orderId == orderId }

        override fun update(ticket: IssuedTicket): IssuedTicket {
            tickets[ticket.id] = ticket
            return ticket
        }

        override fun updateAll(tickets: List<IssuedTicket>): List<IssuedTicket> {
            tickets.forEach { this.tickets[it.id] = it }
            return tickets
        }
    }
}
