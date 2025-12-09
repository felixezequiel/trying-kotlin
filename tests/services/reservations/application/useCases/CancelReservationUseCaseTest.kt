package services.reservations.application.useCases

import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reservations.application.useCases.CancelReservationUseCase
import reservations.domain.CancellationType
import reservations.domain.ReservationStatus
import services.reservations.FakeReservationRepository
import services.reservations.FakeTicketsClient
import services.reservations.FakeUnitOfWork
import services.reservations.TestHelpers

class CancelReservationUseCaseTest {

        private lateinit var reservationRepository: FakeReservationRepository
        private lateinit var unitOfWork: FakeUnitOfWork
        private lateinit var ticketsClient: FakeTicketsClient
        private lateinit var cancelReservationUseCase: CancelReservationUseCase

        @BeforeEach
        fun setUp() {
                reservationRepository = FakeReservationRepository()
                unitOfWork = FakeUnitOfWork(reservationRepository)
                ticketsClient = FakeTicketsClient()
                cancelReservationUseCase = CancelReservationUseCase(unitOfWork, ticketsClient)
        }

        @Test
        fun `deve cancelar reserva com sucesso`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val ticketTypeId = UUID.randomUUID()

                val ticketTypeInfo =
                        TestHelpers.createTestTicketTypeInfo(id = ticketTypeId, eventId = eventId)
                ticketsClient.addTicketType(ticketTypeInfo)

                val item =
                        TestHelpers.createTestReservationItem(
                                ticketTypeId = ticketTypeId,
                                quantity = 2
                        )
                val reservation =
                        TestHelpers.createTestReservation(
                                customerId = customerId,
                                eventId = eventId,
                                items = listOf(item)
                        )
                reservationRepository.save(reservation)

                // Simula que os ingressos estão reservados
                ticketsClient.reserve(ticketTypeId, item.quantity)
                assertEquals(2, ticketsClient.getReservedQuantity(ticketTypeId))

                // Act
                val cancelledReservation =
                        cancelReservationUseCase.execute(
                                reservationId = reservation.id,
                                cancelledBy = customerId,
                                reason = "Não poderei comparecer",
                                cancellationType = CancellationType.BY_CUSTOMER
                        )

                // Assert
                assertEquals(ReservationStatus.CANCELLED, cancelledReservation.status)
                assertEquals(customerId, cancelledReservation.cancelledBy)
                assertEquals("Não poderei comparecer", cancelledReservation.cancellationReason)
                assertEquals(CancellationType.BY_CUSTOMER, cancelledReservation.cancellationType)
                assertNotNull(cancelledReservation.cancelledAt)

                // Verifica que os ingressos foram liberados
                assertEquals(0, ticketsClient.getReservedQuantity(ticketTypeId))
        }

        @Test
        fun `deve cancelar reserva por partner`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val partnerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()

                val reservation =
                        TestHelpers.createTestReservation(
                                customerId = customerId,
                                eventId = eventId
                        )
                reservationRepository.save(reservation)

                // Act
                val cancelledReservation =
                        cancelReservationUseCase.execute(
                                reservationId = reservation.id,
                                cancelledBy = partnerId,
                                reason = "Evento cancelado",
                                cancellationType = CancellationType.BY_PARTNER
                        )

                // Assert
                assertEquals(ReservationStatus.CANCELLED, cancelledReservation.status)
                assertEquals(partnerId, cancelledReservation.cancelledBy)
                assertEquals(CancellationType.BY_PARTNER, cancelledReservation.cancellationType)
        }

        @Test
        fun `deve cancelar reserva por admin`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val adminId = UUID.randomUUID()
                val eventId = UUID.randomUUID()

                val reservation =
                        TestHelpers.createTestReservation(
                                customerId = customerId,
                                eventId = eventId
                        )
                reservationRepository.save(reservation)

                // Act
                val cancelledReservation =
                        cancelReservationUseCase.execute(
                                reservationId = reservation.id,
                                cancelledBy = adminId,
                                reason = "Fraude detectada",
                                cancellationType = CancellationType.BY_ADMIN
                        )

                // Assert
                assertEquals(ReservationStatus.CANCELLED, cancelledReservation.status)
                assertEquals(CancellationType.BY_ADMIN, cancelledReservation.cancellationType)
        }

        @Test
        fun `deve falhar quando reserva não existe`() = runTest {
                // Arrange
                val reservationId = UUID.randomUUID()
                val userId = UUID.randomUUID()

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        cancelReservationUseCase.execute(
                                                reservationId = reservationId,
                                                cancelledBy = userId,
                                                reason = null,
                                                cancellationType = CancellationType.BY_CUSTOMER
                                        )
                                }
                        }
                assertTrue(exception.message?.contains("Reserva não encontrada") == true)
        }

        @Test
        fun `deve falhar quando reserva já está cancelada`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val reservation = TestHelpers.createTestReservation(customerId = customerId)
                val cancelledReservation =
                        reservation.cancel(
                                cancelledBy = customerId,
                                reason = "Motivo anterior",
                                cancellationType = CancellationType.BY_CUSTOMER
                        )
                reservationRepository.save(cancelledReservation)

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        cancelReservationUseCase.execute(
                                                reservationId = cancelledReservation.id,
                                                cancelledBy = customerId,
                                                reason = "Novo motivo",
                                                cancellationType = CancellationType.BY_CUSTOMER
                                        )
                                }
                        }
                assertEquals("Só pode cancelar reserva ACTIVE", exception.message)
        }

        @Test
        fun `deve falhar quando reserva já foi convertida`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val orderId = UUID.randomUUID()
                val reservation = TestHelpers.createTestReservation(customerId = customerId)
                val convertedReservation = reservation.convert(orderId)
                reservationRepository.save(convertedReservation)

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        cancelReservationUseCase.execute(
                                                reservationId = convertedReservation.id,
                                                cancelledBy = customerId,
                                                reason = null,
                                                cancellationType = CancellationType.BY_CUSTOMER
                                        )
                                }
                        }
                assertEquals("Só pode cancelar reserva ACTIVE", exception.message)
        }
}
