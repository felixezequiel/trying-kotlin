package orders.adapters.outbound

import java.util.UUID
import orders.application.ports.outbound.IIssuedTicketRepository
import orders.application.ports.outbound.IOrderRepository
import orders.application.ports.outbound.IOrderStore
import orders.application.ports.outbound.ITransactionManager
import orders.domain.IssuedTicket
import orders.domain.Order

/**
 * Store em memória que gerencia orders, tickets e transações. Esta classe encapsula tanto os
 * repositórios quanto o gerenciador de transações para persistência em memória.
 *
 * Implementa IOrderStore para garantir que qualquer implementação (Postgres, etc.) tenha o mesmo
 * contrato.
 */
class InMemoryOrderStore : IOrderStore {
    private val orders = mutableListOf<Order>()
    private val issuedTickets = mutableListOf<IssuedTicket>()

    override val orderRepository: IOrderRepository = InMemoryOrderRepository()
    override val issuedTicketRepository: IIssuedTicketRepository = InMemoryIssuedTicketRepository()
    override val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()

    private inner class InMemoryOrderRepository : IOrderRepository {
        override fun save(order: Order): Order {
            orders.add(order)
            return order
        }

        override fun findById(id: UUID): Order? {
            return orders.find { it.id == id }
        }

        override fun findByCustomerId(customerId: UUID): List<Order> {
            return orders.filter { it.customerId == customerId }
        }

        override fun findByReservationId(reservationId: UUID): Order? {
            return orders.find { it.reservationId == reservationId }
        }

        override fun update(order: Order): Order {
            val index = orders.indexOfFirst { it.id == order.id }
            if (index != -1) {
                orders[index] = order
            }
            return order
        }
    }

    private inner class InMemoryIssuedTicketRepository : IIssuedTicketRepository {
        override fun save(ticket: IssuedTicket): IssuedTicket {
            issuedTickets.add(ticket)
            return ticket
        }

        override fun saveAll(tickets: List<IssuedTicket>): List<IssuedTicket> {
            issuedTickets.addAll(tickets)
            return tickets
        }

        override fun findById(id: UUID): IssuedTicket? {
            return issuedTickets.find { it.id == id }
        }

        override fun findByCode(code: String): IssuedTicket? {
            return issuedTickets.find { it.code.value == code }
        }

        override fun findByOrderId(orderId: UUID): List<IssuedTicket> {
            return issuedTickets.filter { it.orderId == orderId }
        }

        override fun update(ticket: IssuedTicket): IssuedTicket {
            val index = issuedTickets.indexOfFirst { it.id == ticket.id }
            if (index != -1) {
                issuedTickets[index] = ticket
            }
            return ticket
        }

        override fun updateAll(tickets: List<IssuedTicket>): List<IssuedTicket> {
            tickets.forEach { ticket ->
                val index = issuedTickets.indexOfFirst { it.id == ticket.id }
                if (index != -1) {
                    issuedTickets[index] = ticket
                }
            }
            return tickets
        }
    }

    private inner class InMemoryTransactionManagerImpl : ITransactionManager {
        override suspend fun <T> execute(block: suspend () -> T): T {
            println("Iniciando transação...")
            val ordersSnapshot = orders.toList()
            val ticketsSnapshot = issuedTickets.toList()
            try {
                val result = block()
                println("Transação concluída com sucesso (commit).")
                return result
            } catch (e: Exception) {
                println("Transação falhou (rollback): ${e.message}")
                orders.clear()
                orders.addAll(ordersSnapshot)
                issuedTickets.clear()
                issuedTickets.addAll(ticketsSnapshot)
                throw e
            }
        }
    }
}
