import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.tickets.TestHelpers
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.dto.UpdateTicketTypeRequest
import tickets.application.useCases.UpdateTicketTypeUseCase
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Quantity
import tickets.infrastructure.persistence.DatabaseContext

class UpdateTicketTypeUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var ticketTypeRepository: TicketTypeRepositoryAdapter
    private lateinit var updateTicketTypeUseCase: UpdateTicketTypeUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)
        updateTicketTypeUseCase = UpdateTicketTypeUseCase(ticketTypeRepository)
    }

    private suspend fun createTicketType(): TicketType {
        val ticketType = TestHelpers.createTestTicketType()
        ticketTypeRepository.add(ticketType)
        return ticketType
    }

    @Test
    fun `deve atualizar nome do tipo de ingresso`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        val request = UpdateTicketTypeRequest(name = "Super VIP")

        // Act
        val result = updateTicketTypeUseCase.execute(ticketType.id, request)

        // Assert
        assertEquals("Super VIP", result.name.value)
        assertEquals(ticketType.description, result.description)
    }

    @Test
    fun `deve atualizar preço do tipo de ingresso`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        val request = UpdateTicketTypeRequest(price = "200.00")

        // Act
        val result = updateTicketTypeUseCase.execute(ticketType.id, request)

        // Assert
        assertEquals(BigDecimal("200.00"), result.price.value)
    }

    @Test
    fun `deve atualizar status do tipo de ingresso`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        val request = UpdateTicketTypeRequest(status = "PAUSED")

        // Act
        val result = updateTicketTypeUseCase.execute(ticketType.id, request)

        // Assert
        assertEquals(TicketTypeStatus.PAUSED, result.status)
    }

    @Test
    fun `deve recalcular availableQuantity ao aumentar totalQuantity`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        // Simular que 20 ingressos foram vendidos
        ticketTypeRepository.decrementAvailableQuantity(ticketType.id, Quantity.of(20))

        val request = UpdateTicketTypeRequest(totalQuantity = 150)

        // Act
        val result = updateTicketTypeUseCase.execute(ticketType.id, request)

        // Assert
        assertEquals(150, result.totalQuantity.value)
        assertEquals(130, result.availableQuantity.value) // 150 - 20 vendidos
    }

    @Test
    fun `deve falhar quando tipo de ingresso não existe`() = runTest {
        // Arrange
        val nonExistentId = UUID.randomUUID()
        val request = UpdateTicketTypeRequest(name = "Novo Nome")

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        updateTicketTypeUseCase.execute(nonExistentId, request)
                    }
                }
        assertEquals("Tipo de ingresso não encontrado", exception.message)
    }

    @Test
    fun `deve falhar quando preço é negativo`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        val request = UpdateTicketTypeRequest(price = "-50.00")

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        updateTicketTypeUseCase.execute(ticketType.id, request)
                    }
                }
        assertEquals("Preço deve ser maior ou igual a zero", exception.message)
    }
}
