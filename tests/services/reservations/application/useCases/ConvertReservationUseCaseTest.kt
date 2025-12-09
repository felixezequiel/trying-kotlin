package services.reservations.application.useCases

import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reservations.application.useCases.ConvertReservationUseCase
import reservations.domain.CancellationType
import reservations.domain.ReservationStatus
import services.reservations.FakeReservationRepository
import services.reservations.FakeUnitOfWork
import services.reservations.TestHelpers

class ConvertReservationUseCaseTest {

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var unitOfWork: FakeUnitOfWork
    private lateinit var convertReservationUseCase: ConvertReservationUseCase

    @BeforeEach
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        unitOfWork = FakeUnitOfWork(reservationRepository)
        convertReservationUseCase = ConvertReservationUseCase(unitOfWork)
    }

    @Test
    fun `deve converter reserva com sucesso`() = runTest {
        // Arrange
        val customerId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val reservation = TestHelpers.createTestReservation(customerId = customerId)
        reservationRepository.save(reservation)

        // Act
        val convertedReservation = convertReservationUseCase.execute(reservation.id, orderId)

        // Assert
        assertEquals(ReservationStatus.CONVERTED, convertedReservation.status)
        assertEquals(orderId, convertedReservation.orderId)
        assertNotNull(convertedReservation.convertedAt)
    }

    @Test
    fun `deve falhar quando reserva não existe`() = runTest {
        // Arrange
        val reservationId = UUID.randomUUID()
        val orderId = UUID.randomUUID()

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        convertReservationUseCase.execute(reservationId, orderId)
                    }
                }
        assertTrue(exception.message?.contains("Reserva não encontrada") == true)
    }

    @Test
    fun `deve falhar quando reserva já está cancelada`() = runTest {
        // Arrange
        val customerId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val reservation = TestHelpers.createTestReservation(customerId = customerId)
        val cancelledReservation =
                reservation.cancel(
                        cancelledBy = customerId,
                        reason = "Motivo",
                        cancellationType = CancellationType.BY_CUSTOMER
                )
        reservationRepository.save(cancelledReservation)

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        convertReservationUseCase.execute(cancelledReservation.id, orderId)
                    }
                }
        assertEquals("Só pode converter reserva ACTIVE", exception.message)
    }

    @Test
    fun `deve falhar quando reserva já foi convertida`() = runTest {
        // Arrange
        val customerId = UUID.randomUUID()
        val orderId1 = UUID.randomUUID()
        val orderId2 = UUID.randomUUID()
        val reservation = TestHelpers.createTestReservation(customerId = customerId)
        val convertedReservation = reservation.convert(orderId1)
        reservationRepository.save(convertedReservation)

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        convertReservationUseCase.execute(convertedReservation.id, orderId2)
                    }
                }
        assertEquals("Só pode converter reserva ACTIVE", exception.message)
    }
}
