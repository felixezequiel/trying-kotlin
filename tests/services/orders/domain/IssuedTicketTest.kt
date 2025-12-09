package orders.domain

import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IssuedTicketTest {

    private fun createTicket(): IssuedTicket {
        return IssuedTicket.create(
                orderId = UUID.randomUUID(),
                orderItemId = UUID.randomUUID(),
                ticketTypeId = UUID.randomUUID(),
                ticketTypeName = "VIP",
                eventId = UUID.randomUUID(),
                eventName = "Show de Rock",
                customerId = UUID.randomUUID()
        )
    }

    @Test
    fun `deve criar IssuedTicket com codigo gerado`() {
        val ticket = createTicket()

        assertNotNull(ticket.id)
        assertTrue(ticket.code.value.startsWith("TKT-"))
        assertTrue(ticket.qrCode.startsWith("QR:"))
        assertEquals(TicketStatus.VALID, ticket.status)
        assertNotNull(ticket.issuedAt)
        assertNull(ticket.usedAt)
    }

    @Test
    fun `deve usar ingresso valido`() {
        val ticket = createTicket()
        val usedTicket = ticket.use()

        assertEquals(TicketStatus.USED, usedTicket.status)
        assertNotNull(usedTicket.usedAt)
    }

    @Test
    fun `nao deve usar ingresso ja usado`() {
        val ticket = createTicket().use()
        val exception = assertThrows<IllegalArgumentException> { ticket.use() }
        assertTrue(exception.message!!.contains("não está válido"))
    }

    @Test
    fun `nao deve usar ingresso cancelado`() {
        val ticket = createTicket().cancel()
        val exception = assertThrows<IllegalArgumentException> { ticket.use() }
        assertTrue(exception.message!!.contains("não está válido"))
    }

    @Test
    fun `deve cancelar ingresso valido`() {
        val ticket = createTicket()
        val cancelledTicket = ticket.cancel()

        assertEquals(TicketStatus.CANCELLED, cancelledTicket.status)
    }

    @Test
    fun `nao deve cancelar ingresso ja usado`() {
        val ticket = createTicket().use()
        val exception = assertThrows<IllegalArgumentException> { ticket.cancel() }
        assertTrue(exception.message!!.contains("VALID"))
    }

    @Test
    fun `codigos devem ser unicos`() {
        val tickets = (1..50).map { createTicket() }
        val codes = tickets.map { it.code.value }
        assertEquals(50, codes.toSet().size)
    }
}
