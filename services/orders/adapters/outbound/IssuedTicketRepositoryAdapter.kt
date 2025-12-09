package orders.adapters.outbound

import java.util.UUID
import orders.application.ports.outbound.IIssuedTicketRepository
import orders.domain.IssuedTicket
import orders.infrastructure.persistence.DatabaseContext

class IssuedTicketRepositoryAdapter(private val dbContext: DatabaseContext) :
        IIssuedTicketRepository {

    override fun save(ticket: IssuedTicket): IssuedTicket {
        dbContext.addIssuedTicket(ticket)
        return ticket
    }

    override fun saveAll(tickets: List<IssuedTicket>): List<IssuedTicket> {
        dbContext.addIssuedTickets(tickets)
        return tickets
    }

    override fun findById(id: UUID): IssuedTicket? {
        return dbContext.findIssuedTicketById(id)
    }

    override fun findByCode(code: String): IssuedTicket? {
        return dbContext.findIssuedTicketByCode(code)
    }

    override fun findByOrderId(orderId: UUID): List<IssuedTicket> {
        return dbContext.findIssuedTicketsByOrderId(orderId)
    }

    override fun update(ticket: IssuedTicket): IssuedTicket {
        dbContext.updateIssuedTicket(ticket)
        return ticket
    }

    override fun updateAll(tickets: List<IssuedTicket>): List<IssuedTicket> {
        dbContext.updateIssuedTickets(tickets)
        return tickets
    }
}
