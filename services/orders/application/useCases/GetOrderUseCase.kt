package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IUnitOfWork
import orders.domain.Order

class GetOrderUseCase(private val unitOfWork: IUnitOfWork) {

    fun execute(orderId: UUID): Order? {
        return unitOfWork.orderRepository.findById(orderId)
    }
}
