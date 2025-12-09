package orders.application.useCases

import java.util.UUID
import kotlinx.coroutines.runBlocking
import orders.adapters.outbound.InMemoryOrderStore
import orders.adapters.outbound.UnitOfWorkAdapter
import orders.application.dto.CreateOrderRequest
import orders.application.ports.outbound.IReservationsClient
import orders.application.ports.outbound.ReservationInfo
import orders.application.ports.outbound.ReservationItemInfo
import orders.domain.PaymentStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateOrderUseCaseTest {

        private lateinit var orderStore: InMemoryOrderStore
        private lateinit var unitOfWork: UnitOfWorkAdapter
        private lateinit var reservationsClient: FakeReservationsClient
        private lateinit var useCase: CreateOrderUseCase

        @BeforeEach
        fun setup() {
                orderStore = InMemoryOrderStore()
                unitOfWork =
                        UnitOfWorkAdapter(
                                orderStore.orderRepository,
                                orderStore.issuedTicketRepository,
                                orderStore.transactionManager
                        )
                reservationsClient = FakeReservationsClient()
                useCase = CreateOrderUseCase(unitOfWork, reservationsClient)
        }

        @Test
        fun `deve criar order a partir de reserva ativa`() = runBlocking {
                val customerId = UUID.randomUUID()
                val reservationId = UUID.randomUUID()
                val eventId = UUID.randomUUID()

                reservationsClient.addReservation(
                        ReservationInfo(
                                id = reservationId,
                                customerId = customerId,
                                eventId = eventId,
                                eventName = "Show",
                                items =
                                        listOf(
                                                ReservationItemInfo(
                                                        id = UUID.randomUUID(),
                                                        ticketTypeId = UUID.randomUUID(),
                                                        ticketTypeName = "VIP",
                                                        quantity = 2,
                                                        unitPrice = "100.00",
                                                        subtotal = "200.00"
                                                )
                                        ),
                                totalAmount = "200.00",
                                status = "ACTIVE"
                        )
                )

                val request = CreateOrderRequest(reservationId.toString())
                val order = useCase.execute(customerId, request)

                assertEquals(customerId, order.customerId)
                assertEquals(reservationId, order.reservationId)
                assertEquals(eventId, order.eventId)
                assertEquals(PaymentStatus.PENDING, order.paymentStatus)
                assertEquals(1, order.items.size)
                assertEquals("VIP", order.items[0].ticketTypeName)
        }

        @Test
        fun `deve lancar excecao se reserva nao encontrada`() = runBlocking {
                val customerId = UUID.randomUUID()
                val reservationId = UUID.randomUUID()

                val request = CreateOrderRequest(reservationId.toString())

                val exception =
                        assertThrows<IllegalArgumentException> {
                                useCase.execute(customerId, request)
                        }
                assertTrue(exception.message!!.contains("não encontrada"))
        }

        @Test
        fun `deve lancar excecao se reserva nao pertence ao customer`() = runBlocking {
                val customerId = UUID.randomUUID()
                val otherCustomerId = UUID.randomUUID()
                val reservationId = UUID.randomUUID()

                reservationsClient.addReservation(
                        ReservationInfo(
                                id = reservationId,
                                customerId = otherCustomerId,
                                eventId = UUID.randomUUID(),
                                eventName = "Show",
                                items =
                                        listOf(
                                                ReservationItemInfo(
                                                        id = UUID.randomUUID(),
                                                        ticketTypeId = UUID.randomUUID(),
                                                        ticketTypeName = "VIP",
                                                        quantity = 1,
                                                        unitPrice = "100.00",
                                                        subtotal = "100.00"
                                                )
                                        ),
                                totalAmount = "100.00",
                                status = "ACTIVE"
                        )
                )

                val request = CreateOrderRequest(reservationId.toString())

                val exception =
                        assertThrows<IllegalArgumentException> {
                                useCase.execute(customerId, request)
                        }
                assertTrue(exception.message!!.contains("não pertence"))
        }

        @Test
        fun `deve lancar excecao se reserva nao esta ativa`() = runBlocking {
                val customerId = UUID.randomUUID()
                val reservationId = UUID.randomUUID()

                reservationsClient.addReservation(
                        ReservationInfo(
                                id = reservationId,
                                customerId = customerId,
                                eventId = UUID.randomUUID(),
                                eventName = "Show",
                                items =
                                        listOf(
                                                ReservationItemInfo(
                                                        id = UUID.randomUUID(),
                                                        ticketTypeId = UUID.randomUUID(),
                                                        ticketTypeName = "VIP",
                                                        quantity = 1,
                                                        unitPrice = "100.00",
                                                        subtotal = "100.00"
                                                )
                                        ),
                                totalAmount = "100.00",
                                status = "CANCELLED"
                        )
                )

                val request = CreateOrderRequest(reservationId.toString())

                val exception =
                        assertThrows<IllegalStateException> { useCase.execute(customerId, request) }
                assertTrue(exception.message!!.contains("não está ativa"))
        }

        @Test
        fun `deve lancar excecao se ja existe order para reserva`() = runBlocking {
                val customerId = UUID.randomUUID()
                val reservationId = UUID.randomUUID()

                reservationsClient.addReservation(
                        ReservationInfo(
                                id = reservationId,
                                customerId = customerId,
                                eventId = UUID.randomUUID(),
                                eventName = "Show",
                                items =
                                        listOf(
                                                ReservationItemInfo(
                                                        id = UUID.randomUUID(),
                                                        ticketTypeId = UUID.randomUUID(),
                                                        ticketTypeName = "VIP",
                                                        quantity = 1,
                                                        unitPrice = "100.00",
                                                        subtotal = "100.00"
                                                )
                                        ),
                                totalAmount = "100.00",
                                status = "ACTIVE"
                        )
                )

                // Cria primeiro order
                val request = CreateOrderRequest(reservationId.toString())
                useCase.execute(customerId, request)

                // Tenta criar segundo order
                val exception =
                        assertThrows<IllegalStateException> { useCase.execute(customerId, request) }
                assertTrue(exception.message!!.contains("Já existe"))
        }

        // Fake implementations
        private class FakeReservationsClient : IReservationsClient {
                private val reservations = mutableMapOf<UUID, ReservationInfo>()

                fun addReservation(reservation: ReservationInfo) {
                        reservations[reservation.id] = reservation
                }

                override suspend fun getReservation(reservationId: UUID): ReservationInfo? =
                        reservations[reservationId]

                override suspend fun convertReservation(
                        reservationId: UUID,
                        orderId: UUID
                ): Boolean = true
        }
}
