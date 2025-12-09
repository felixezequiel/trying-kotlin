package orders.infrastructure.persistence

import java.util.UUID
import orders.domain.IssuedTicket
import orders.domain.Order

/**
 * Contexto de banco de dados para o serviço Orders. Armazenamento em memória até integração com
 * banco real.
 */
class DatabaseContext {
    private val orders = mutableListOf<Order>()
    private val issuedTickets = mutableListOf<IssuedTicket>()

    suspend fun <T> executeTransaction(block: suspend () -> T): T {
        val ordersSnapshot = orders.toList()
        val ticketsSnapshot = issuedTickets.toList()
        try {
            return block()
        } catch (e: Exception) {
            orders.clear()
            orders.addAll(ordersSnapshot)
            issuedTickets.clear()
            issuedTickets.addAll(ticketsSnapshot)
            throw e
        }
    }

    // Orders
    fun addOrder(order: Order): UUID {
        orders.add(order)
        return order.id
    }

    fun findOrderById(id: UUID): Order? {
        return orders.find { it.id == id }
    }

    fun findOrdersByCustomerId(customerId: UUID): List<Order> {
        return orders.filter { it.customerId == customerId }
    }

    fun findOrderByReservationId(reservationId: UUID): Order? {
        return orders.find { it.reservationId == reservationId }
    }

    fun updateOrder(order: Order): Boolean {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index == -1) return false
        orders[index] = order
        return true
    }

    // Issued Tickets
    fun addIssuedTicket(ticket: IssuedTicket): UUID {
        issuedTickets.add(ticket)
        return ticket.id
    }

    fun addIssuedTickets(tickets: List<IssuedTicket>): List<UUID> {
        issuedTickets.addAll(tickets)
        return tickets.map { it.id }
    }

    fun findIssuedTicketById(id: UUID): IssuedTicket? {
        return issuedTickets.find { it.id == id }
    }

    fun findIssuedTicketByCode(code: String): IssuedTicket? {
        return issuedTickets.find { it.code.value == code }
    }

    fun findIssuedTicketsByOrderId(orderId: UUID): List<IssuedTicket> {
        return issuedTickets.filter { it.orderId == orderId }
    }

    fun updateIssuedTicket(ticket: IssuedTicket): Boolean {
        val index = issuedTickets.indexOfFirst { it.id == ticket.id }
        if (index == -1) return false
        issuedTickets[index] = ticket
        return true
    }

    fun updateIssuedTickets(tickets: List<IssuedTicket>): Boolean {
        tickets.forEach { ticket ->
            val index = issuedTickets.indexOfFirst { it.id == ticket.id }
            if (index != -1) {
                issuedTickets[index] = ticket
            }
        }
        return true
    }
}
