package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IOrderRepository
import orders.domain.Order

class ListCustomerOrdersUseCase(private val orderRepository: IOrderRepository) {

    fun execute(customerId: UUID): List<Order> {
        return orderRepository.findByCustomerId(customerId)
    }
}
