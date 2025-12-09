package orders.adapters.outbound

import java.util.UUID
import orders.application.ports.outbound.IOrderRepository
import orders.domain.Order
import orders.infrastructure.persistence.DatabaseContext

class OrderRepositoryAdapter(private val dbContext: DatabaseContext) : IOrderRepository {

    override fun save(order: Order): Order {
        dbContext.addOrder(order)
        return order
    }

    override fun findById(id: UUID): Order? {
        return dbContext.findOrderById(id)
    }

    override fun findByCustomerId(customerId: UUID): List<Order> {
        return dbContext.findOrdersByCustomerId(customerId)
    }

    override fun findByReservationId(reservationId: UUID): Order? {
        return dbContext.findOrderByReservationId(reservationId)
    }

    override fun update(order: Order): Order {
        dbContext.updateOrder(order)
        return order
    }
}
