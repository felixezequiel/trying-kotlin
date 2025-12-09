import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.tickets.TestHelpers
import tickets.adapters.outbound.TicketTypeRepositoryAdapter
import tickets.application.dto.ReserveTicketsRequest
import tickets.application.useCases.ReserveTicketsUseCase
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.infrastructure.persistence.DatabaseContext

class ReserveTicketsUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var ticketTypeRepository: TicketTypeRepositoryAdapter
    private lateinit var reserveTicketsUseCase: ReserveTicketsUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        ticketTypeRepository = TicketTypeRepositoryAdapter(dbContext)
        reserveTicketsUseCase = ReserveTicketsUseCase(ticketTypeRepository)
    }

    private suspend fun createTicketType(
            availableQuantity: Int = 100,
            maxPerCustomer: Int = 4,
            status: TicketTypeStatus = TicketTypeStatus.ACTIVE
    ): TicketType {
        val ticketType =
                TestHelpers.createTestTicketType(
                        availableQuantity = availableQuantity,
                        maxPerCustomer = maxPerCustomer,
                        status = status
                )
        ticketTypeRepository.add(ticketType)
        return ticketType
    }

    @Test
    fun `deve reservar ingressos com sucesso`() = runTest {
        // Arrange
        val ticketType = createTicketType(availableQuantity = 100)
        val request = ReserveTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 2)

        // Act
        val response = reserveTicketsUseCase.execute(request)

        // Assert
        assertTrue(response.success)
        assertEquals(ticketType.id.toString(), response.ticketTypeId)
        assertEquals(2, response.reservedQuantity)
        assertEquals("100.00", response.unitPrice)
        assertEquals(98, response.remainingQuantity)

        // Verificar que o estoque foi decrementado
        val updatedTicketType = ticketTypeRepository.getById(ticketType.id)
        assertEquals(98, updatedTicketType?.availableQuantity?.value)
    }

    @Test
    fun `deve falhar quando quantidade excede maxPerCustomer`() = runTest {
        // Arrange
        val ticketType = createTicketType(maxPerCustomer = 4)
        val request = ReserveTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 5)

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking { reserveTicketsUseCase.execute(request) }
                }
        assertTrue(exception.message?.contains("máximo permitido") == true)
    }

    @Test
    fun `deve falhar quando estoque insuficiente`() = runTest {
        // Arrange
        val ticketType = createTicketType(availableQuantity = 2)
        val request = ReserveTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 3)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking { reserveTicketsUseCase.execute(request) }
                }
        assertTrue(exception.message?.contains("insuficiente") == true)
    }

    @Test
    fun `deve falhar quando tipo de ingresso não está ativo`() = runTest {
        // Arrange
        val ticketType = createTicketType(status = TicketTypeStatus.PAUSED)
        val request = ReserveTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 1)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking { reserveTicketsUseCase.execute(request) }
                }
        assertTrue(exception.message?.contains("não está disponível") == true)
    }

    @Test
    fun `deve mudar status para SOLD_OUT quando estoque zera`() = runTest {
        // Arrange
        val ticketType = createTicketType(availableQuantity = 2)
        val request = ReserveTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 2)

        // Act
        val response = reserveTicketsUseCase.execute(request)

        // Assert
        assertTrue(response.success)
        assertEquals(0, response.remainingQuantity)

        val updatedTicketType = ticketTypeRepository.getById(ticketType.id)
        assertEquals(TicketTypeStatus.SOLD_OUT, updatedTicketType?.status)
    }

    @Test
    fun `deve falhar quando quantidade é zero`() = runTest {
        // Arrange
        val ticketType = createTicketType()
        val request = ReserveTicketsRequest(ticketTypeId = ticketType.id.toString(), quantity = 0)

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking { reserveTicketsUseCase.execute(request) }
                }
        assertEquals("Quantidade deve ser maior que zero", exception.message)
    }
}
