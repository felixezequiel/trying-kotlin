package services.reservations.application.useCases

import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reservations.application.useCases.ListEventReservationsUseCase
import services.reservations.FakeReservationRepository
import services.reservations.FakeUnitOfWork
import services.reservations.TestHelpers

class ListEventReservationsUseCaseTest {

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var unitOfWork: FakeUnitOfWork
    private lateinit var listEventReservationsUseCase: ListEventReservationsUseCase

    @BeforeEach
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        unitOfWork = FakeUnitOfWork(reservationRepository)
        listEventReservationsUseCase = ListEventReservationsUseCase(unitOfWork)
    }

    @Test
    fun `deve listar reservas do evento`() = runTest {
        // Arrange
        val eventId = UUID.randomUUID()
        val otherEventId = UUID.randomUUID()

        val reservation1 = TestHelpers.createTestReservation(eventId = eventId)
        val reservation2 = TestHelpers.createTestReservation(eventId = eventId)
        val reservation3 = TestHelpers.createTestReservation(eventId = otherEventId)

        reservationRepository.save(reservation1)
        reservationRepository.save(reservation2)
        reservationRepository.save(reservation3)

        // Act
        val result = listEventReservationsUseCase.execute(eventId)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { it.eventId == eventId })
    }

    @Test
    fun `deve retornar lista vazia quando evento não tem reservas`() = runTest {
        // Arrange
        val eventId = UUID.randomUUID()

        // Act
        val result = listEventReservationsUseCase.execute(eventId)

        // Assert
        assertTrue(result.isEmpty())
    }
}
