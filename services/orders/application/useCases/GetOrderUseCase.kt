package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IOrderRepository
import orders.domain.Order

class GetOrderUseCase(private val orderRepository: IOrderRepository) {

    fun execute(orderId: UUID): Order? {
        return orderRepository.findById(orderId)
    }
}
