package orders.application.useCases

import java.util.UUID
import orders.application.dto.CreateOrderRequest
import orders.application.ports.outbound.IReservationsClient
import orders.application.ports.outbound.IUnitOfWork
import orders.domain.Order
import orders.domain.OrderItem
import orders.domain.valueObjects.Price
import orders.domain.valueObjects.Quantity

class CreateOrderUseCase(
        private val unitOfWork: IUnitOfWork,
        private val reservationsClient: IReservationsClient
) {

    suspend fun execute(customerId: UUID, request: CreateOrderRequest): Order {
        return unitOfWork.runInTransaction {
            val reservationId = UUID.fromString(request.reservationId)

            // Verifica se já existe order para esta reserva
            val existingOrder = unitOfWork.orderRepository.findByReservationId(reservationId)
            if (existingOrder != null) {
                throw IllegalStateException("Já existe um pedido para esta reserva")
            }

            // Busca a reserva
            val reservation =
                    reservationsClient.getReservation(reservationId)
                            ?: throw IllegalArgumentException("Reserva não encontrada")

            // Valida que a reserva pertence ao customer
            if (reservation.customerId != customerId) {
                throw IllegalArgumentException("Reserva não pertence ao customer")
            }

            // Valida que a reserva está ACTIVE
            if (reservation.status != "ACTIVE") {
                throw IllegalStateException("Reserva não está ativa")
            }

            // Converte itens da reserva para itens do pedido
            val orderItems =
                    reservation.items.map { item ->
                        OrderItem.create(
                                ticketTypeId = item.ticketTypeId,
                                ticketTypeName = item.ticketTypeName,
                                quantity = Quantity.of(item.quantity),
                                unitPrice = Price.fromString(item.unitPrice)
                        )
                    }

            // Cria o pedido
            val order =
                    Order.create(
                            customerId = customerId,
                            reservationId = reservationId,
                            eventId = reservation.eventId,
                            items = orderItems
                    )

            unitOfWork.orderRepository.save(order)
        }
    }
}
