import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.tickets.TestHelpers
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.dto.ReleaseTicketsRequest
import tickets.application.useCases.ReleaseTicketsUseCase
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.infrastructure.persistence.DatabaseContext

class ReleaseTicketsUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var ticketTypeRepository: TicketTypeRepositoryAdapter
    private lateinit var releaseTicketsUseCase: ReleaseTicketsUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)
        releaseTicketsUseCase = ReleaseTicketsUseCase(ticketTypeRepository)
    }

    private suspend fun createTicketType(
            totalQuantity: Int = 100,
            availableQuantity: Int = 50,
            status: TicketTypeStatus = TicketTypeStatus.ACTIVE
    ): TicketType {
        val ticketType =
                TestHelpers.createTestTicketType(
                        totalQuantity = totalQuantity,
                        availableQuantity = availableQuantity,
                        status = status
                )
        ticketTypeRepository.add(ticketType)
        return ticketType
    }

    @Test
    fun `deve liberar ingressos com sucesso`() = runTest {
        // Arrange
        val ticketType = createTicketType(totalQuantity = 100, availableQuantity = 50)
        val request = ReleaseTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 5)

        // Act
        val result = releaseTicketsUseCase.execute(request)

        // Assert
        assertEquals(55, result.availableQuantity.value)
    }

    @Test
    fun `deve mudar status de SOLD_OUT para ACTIVE ao liberar`() = runTest {
        // Arrange
        val ticketType =
                createTicketType(
                        totalQuantity = 100,
                        availableQuantity = 0,
                        status = TicketTypeStatus.SOLD_OUT
                )
        val request = ReleaseTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 5)

        // Act
        val result = releaseTicketsUseCase.execute(request)

        // Assert
        assertEquals(5, result.availableQuantity.value)
        assertEquals(TicketTypeStatus.ACTIVE, result.status)
    }

    @Test
    fun `não deve exceder totalQuantity ao liberar`() = runTest {
        // Arrange
        val ticketType = createTicketType(totalQuantity = 100, availableQuantity = 98)
        val request = ReleaseTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 10)

        // Act
        val result = releaseTicketsUseCase.execute(request)

        // Assert
        assertEquals(100, result.availableQuantity.value) // Limitado ao totalQuantity
    }

    @Test
    fun `deve falhar quando tipo de ingresso não existe`() = runTest {
        // Arrange
        val request =
                ReleaseTicketsRequest(ticketTypeId = UUID.randomUUID().toString(), quantity = 5)

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking { releaseTicketsUseCase.execute(request) }
                }
        assertEquals("Tipo de ingresso não encontrado", exception.message)
    }

    @Test
    fun `deve falhar quando quantidade é zero`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        val request = ReleaseTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 0)

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking { releaseTicketsUseCase.execute(request) }
                }
        assertEquals("Quantidade deve ser maior que zero", exception.message)
    }
}
