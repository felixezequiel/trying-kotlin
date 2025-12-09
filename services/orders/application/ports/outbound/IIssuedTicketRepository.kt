package orders.application.ports.outbound

import java.util.UUID
import orders.domain.IssuedTicket

interface IIssuedTicketRepository {
    fun save(ticket: IssuedTicket): IssuedTicket
    fun saveAll(tickets: List<IssuedTicket>): List<IssuedTicket>
    fun findById(id: UUID): IssuedTicket?
    fun findByCode(code: String): IssuedTicket?
    fun findByOrderId(orderId: UUID): List<IssuedTicket>
    fun update(ticket: IssuedTicket): IssuedTicket
    fun updateAll(tickets: List<IssuedTicket>): List<IssuedTicket>
}
