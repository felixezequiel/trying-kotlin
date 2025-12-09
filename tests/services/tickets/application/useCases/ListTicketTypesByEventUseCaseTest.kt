import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.tickets.TestHelpers
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.useCases.ListTicketTypesByEventUseCase
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.infrastructure.persistence.DatabaseContext

class ListTicketTypesByEventUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var ticketTypeRepository: TicketTypeRepositoryAdapter
    private lateinit var listTicketTypesByEventUseCase: ListTicketTypesByEventUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)
        listTicketTypesByEventUseCase = ListTicketTypesByEventUseCase(ticketTypeRepository)
    }

    private suspend fun createTicketType(
            eventId: UUID,
            name: String,
            status: TicketTypeStatus = TicketTypeStatus.ACTIVE
    ): TicketType {
        val ticketType =
                TestHelpers.createTestTicketType(eventId = eventId, name = name, status = status)
        ticketTypeRepository.add(ticketType)
        return ticketType
    }

    @Test
    fun `deve listar todos os tipos de ingresso de um evento`() = runTest {
        // Arrange
        val eventId = UUID.randomUUID()
        createTicketType(eventId, "VIP")
        createTicketType(eventId, "Pista")
        createTicketType(eventId, "Camarote")

        // Outro evento
        val otherEventId = UUID.randomUUID()
        createTicketType(otherEventId, "Outro")

        // Act
        val result = listTicketTypesByEventUseCase.execute(eventId)

        // Assert
        assertEquals(3, result.size)
        assertTrue(result.any { it.name.value == "VIP" })
        assertTrue(result.any { it.name.value == "Pista" })
        assertTrue(result.any { it.name.value == "Camarote" })
    }

    @Test
    fun `deve retornar lista vazia quando evento não tem ingressos`() = runTest {
        // Arrange
        val eventId = UUID.randomUUID()

        // Act
        val result = listTicketTypesByEventUseCase.execute(eventId)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deve listar apenas tipos ativos quando usar executeActive`() = runTest {
        // Arrange
        val eventId = UUID.randomUUID()
        createTicketType(eventId, "VIP", TicketTypeStatus.ACTIVE)
        createTicketType(eventId, "Pista", TicketTypeStatus.PAUSED)
        createTicketType(eventId, "Camarote", TicketTypeStatus.INACTIVE)

        // Act
        val result = listTicketTypesByEventUseCase.executeActive(eventId)

        // Assert
        assertEquals(1, result.size)
        assertEquals("VIP", result[0].name.value)
    }
}
