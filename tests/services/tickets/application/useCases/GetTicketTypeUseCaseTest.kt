import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.useCases.GetTicketTypeUseCase
import tickets.domain.TicketType
import tickets.infrastructure.persistence.DatabaseContext

class GetTicketTypeUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var ticketTypeRepository: TicketTypeRepositoryAdapter
    private lateinit var getTicketTypeUseCase: GetTicketTypeUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)
        getTicketTypeUseCase = GetTicketTypeUseCase(ticketTypeRepository)
    }

    @Test
    fun `deve retornar tipo de ingresso existente`() = runTest {
        // Arrange
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
                        salesEndDate = null
                )
        ticketTypeRepository.add(ticketType)

        // Act
        val result = getTicketTypeUseCase.execute(ticketType.id)

        // Assert
        assertNotNull(result)
        assertEquals(ticketType.id, result?.id)
        assertEquals("VIP", result?.name)
    }

    @Test
    fun `deve retornar null quando tipo de ingresso não existe`() = runTest {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = getTicketTypeUseCase.execute(nonExistentId)

        // Assert
        assertNull(result)
    }
}
