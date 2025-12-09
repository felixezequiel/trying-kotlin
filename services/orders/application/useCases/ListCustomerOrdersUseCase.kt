package orders.application.useCases

import java.util.UUID
import orders.application.ports.outbound.IUnitOfWork
import orders.domain.Order

class ListCustomerOrdersUseCase(private val unitOfWork: IUnitOfWork) {

    fun execute(customerId: UUID): List<Order> {
        return unitOfWork.orderRepository.findByCustomerId(customerId)
    }
}
