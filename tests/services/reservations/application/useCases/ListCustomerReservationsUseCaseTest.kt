package services.reservations.application.useCases

import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reservations.application.useCases.ListCustomerReservationsUseCase
import services.reservations.FakeReservationRepository
import services.reservations.TestHelpers

class ListCustomerReservationsUseCaseTest {

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var listCustomerReservationsUseCase: ListCustomerReservationsUseCase

    @BeforeEach
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        listCustomerReservationsUseCase = ListCustomerReservationsUseCase(reservationRepository)
    }

    @Test
    fun `deve listar reservas do cliente`() = runTest {
        // Arrange
        val customerId = UUID.randomUUID()
        val otherCustomerId = UUID.randomUUID()

        val reservation1 = TestHelpers.createTestReservation(customerId = customerId)
        val reservation2 = TestHelpers.createTestReservation(customerId = customerId)
        val reservation3 = TestHelpers.createTestReservation(customerId = otherCustomerId)

        reservationRepository.save(reservation1)
        reservationRepository.save(reservation2)
        reservationRepository.save(reservation3)

        // Act
        val result = listCustomerReservationsUseCase.execute(customerId)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { it.customerId == customerId })
    }

    @Test
    fun `deve retornar lista vazia quando cliente não tem reservas`() = runTest {
        // Arrange
        val customerId = UUID.randomUUID()

        // Act
        val result = listCustomerReservationsUseCase.execute(customerId)

        // Assert
        assertTrue(result.isEmpty())
    }
}
