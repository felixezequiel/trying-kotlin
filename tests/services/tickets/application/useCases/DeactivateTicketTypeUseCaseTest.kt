import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.useCases.DeactivateTicketTypeUseCase
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.infrastructure.persistence.DatabaseContext

class DeactivateTicketTypeUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var ticketTypeRepository: TicketTypeRepositoryAdapter
    private lateinit var deactivateTicketTypeUseCase: DeactivateTicketTypeUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)
        deactivateTicketTypeUseCase = DeactivateTicketTypeUseCase(ticketTypeRepository)
    }

    private suspend fun createTicketType(
            status: TicketTypeStatus = TicketTypeStatus.ACTIVE
    ): TicketType {
        val ticketType =
                TicketType(
                        eventId = UUID.randomUUID(),
                        name = "VIP",
                        description = "Ingresso VIP",
                        price = BigDecimal("100.00"),
                        totalQuantity = 100,
                        availableQuantity = 100,
                        maxPerCustomer = 4,
                        salesStartDate = null,
                        salesEndDate = null,
                        status = status
                )
        ticketTypeRepository.add(ticketType)
        return ticketType
    }

    @Test
    fun `deve desativar tipo de ingresso ativo`() = runTest {
        // Arrange
        val ticketType = createTicketType(TicketTypeStatus.ACTIVE)

        // Act
        val result = deactivateTicketTypeUseCase.execute(ticketType.id)

        // Assert
        assertEquals(TicketTypeStatus.INACTIVE, result.status)
    }

    @Test
    fun `deve desativar tipo de ingresso pausado`() = runTest {
        // Arrange
        val ticketType = createTicketType(TicketTypeStatus.PAUSED)

        // Act
        val result = deactivateTicketTypeUseCase.execute(ticketType.id)

        // Assert
        assertEquals(TicketTypeStatus.INACTIVE, result.status)
    }

    @Test
    fun `deve falhar quando tipo de ingresso já está inativo`() = runTest {
        // Arrange
        val ticketType = createTicketType(TicketTypeStatus.INACTIVE)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        deactivateTicketTypeUseCase.execute(ticketType.id)
                    }
                }
        assertEquals("Tipo de ingresso já está inativo", exception.message)
    }

    @Test
    fun `deve falhar quando tipo de ingresso não existe`() = runTest {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        deactivateTicketTypeUseCase.execute(nonExistentId)
                    }
                }
        assertEquals("Tipo de ingresso não encontrado", exception.message)
    }
}
