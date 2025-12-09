package services.reservations.application.useCases

import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reservations.application.useCases.GetReservationUseCase
import services.reservations.FakeReservationRepository
import services.reservations.FakeUnitOfWork
import services.reservations.TestHelpers

class GetReservationUseCaseTest {

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var unitOfWork: FakeUnitOfWork
    private lateinit var getReservationUseCase: GetReservationUseCase

    @BeforeEach
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        unitOfWork = FakeUnitOfWork(reservationRepository)
        getReservationUseCase = GetReservationUseCase(unitOfWork)
    }

    @Test
    fun `deve retornar reserva quando existe`() = runTest {
        // Arrange
        val customerId = UUID.randomUUID()
        val reservation = TestHelpers.createTestReservation(customerId = customerId)
        reservationRepository.save(reservation)

        // Act
        val result = getReservationUseCase.execute(reservation.id)

        // Assert
        assertNotNull(result)
        assertEquals(reservation.id, result?.id)
        assertEquals(customerId, result?.customerId)
    }

    @Test
    fun `deve retornar null quando reserva não existe`() = runTest {
        // Arrange
        val reservationId = UUID.randomUUID()

        // Act
        val result = getReservationUseCase.execute(reservationId)

        // Assert
        assertNull(result)
    }
}
