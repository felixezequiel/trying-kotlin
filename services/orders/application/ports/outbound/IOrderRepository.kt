package orders.application.ports.outbound

import java.util.UUID
import orders.domain.Order

interface IOrderRepository {
    fun save(order: Order): Order
    fun findById(id: UUID): Order?
    fun findByCustomerId(customerId: UUID): List<Order>
    fun findByReservationId(reservationId: UUID): Order?
    fun update(order: Order): Order
}
