import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.tickets.FakeTicketTypeRepository
import services.tickets.FakeUnitOfWork
import tickets.application.dto.CreateTicketTypeRequest
import tickets.application.useCases.CreateTicketTypeUseCase
import tickets.domain.TicketTypeStatus

class CreateTicketTypeUseCaseTest {

        private lateinit var ticketTypeRepository: FakeTicketTypeRepository
        private lateinit var unitOfWork: FakeUnitOfWork
        private lateinit var createTicketTypeUseCase: CreateTicketTypeUseCase

        @BeforeEach
        fun setUp() {
                ticketTypeRepository = FakeTicketTypeRepository()
                unitOfWork = FakeUnitOfWork(ticketTypeRepository)
                createTicketTypeUseCase = CreateTicketTypeUseCase(unitOfWork)
        }

        @Test
        fun `deve criar tipo de ingresso com sucesso`() = runTest {
                // Arrange
                val eventId = UUID.randomUUID()
                val salesStartDate = Instant.now().plus(1, ChronoUnit.DAYS)
                val salesEndDate = Instant.now().plus(30, ChronoUnit.DAYS)

                val request =
                        CreateTicketTypeRequest(
                                eventId = eventId.toString(),
                                name = "VIP",
                                description = "Ingresso VIP com acesso exclusivo",
                                price = "150.00",
                                totalQuantity = 100,
                                maxPerCustomer = 4,
                                salesStartDate = salesStartDate.toString(),
                                salesEndDate = salesEndDate.toString()
                        )

                // Act
                val ticketTypeId = createTicketTypeUseCase.execute(request)

                // Assert
                assertNotNull(ticketTypeId)
                val ticketType = ticketTypeRepository.getById(ticketTypeId)
                assertNotNull(ticketType)
                assertEquals("VIP", ticketType?.name?.value)
                assertEquals("Ingresso VIP com acesso exclusivo", ticketType?.description)
                assertEquals(100, ticketType?.totalQuantity?.value)
                assertEquals(100, ticketType?.availableQuantity?.value)
                assertEquals(4, ticketType?.maxPerCustomer?.value)
                assertEquals(TicketTypeStatus.ACTIVE, ticketType?.status)
        }

        @Test
        fun `deve criar tipo de ingresso gratuito`() = runTest {
                // Arrange
                val eventId = UUID.randomUUID()

                val request =
                        CreateTicketTypeRequest(
                                eventId = eventId.toString(),
                                name = "Entrada Gratuita",
                                description = "Ingresso gratuito",
                                price = "0.00",
                                totalQuantity = 500,
                                maxPerCustomer = 2,
                                salesStartDate = null,
                                salesEndDate = null
                        )

                // Act
                val ticketTypeId = createTicketTypeUseCase.execute(request)

                // Assert
                val ticketType = ticketTypeRepository.getById(ticketTypeId)
                assertNotNull(ticketType)
                assertEquals("0.00", ticketType?.price.toString())
                assertNull(ticketType?.salesStartDate)
                assertNull(ticketType?.salesEndDate)
        }

        @Test
        fun `deve falhar quando preço é negativo`() = runTest {
                // Arrange
                val eventId = UUID.randomUUID()

                val request =
                        CreateTicketTypeRequest(
                                eventId = eventId.toString(),
                                name = "Ingresso",
                                description = "Descrição",
                                price = "-10.00",
                                totalQuantity = 100,
                                maxPerCustomer = 4,
                                salesStartDate = null,
                                salesEndDate = null
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createTicketTypeUseCase.execute(request)
                                }
                        }
                assertEquals("Preço deve ser maior ou igual a zero", exception.message)
        }

        @Test
        fun `deve falhar quando quantidade total é zero`() = runTest {
                // Arrange
                val eventId = UUID.randomUUID()

                val request =
                        CreateTicketTypeRequest(
                                eventId = eventId.toString(),
                                name = "Ingresso",
                                description = "Descrição",
                                price = "50.00",
                                totalQuantity = 0,
                                maxPerCustomer = 4,
                                salesStartDate = null,
                                salesEndDate = null
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createTicketTypeUseCase.execute(request)
                                }
                        }
                assertEquals("Quantidade deve ser maior que zero", exception.message)
        }

        @Test
        fun `deve falhar quando maxPerCustomer é zero`() = runTest {
                // Arrange
                val eventId = UUID.randomUUID()

                val request =
                        CreateTicketTypeRequest(
                                eventId = eventId.toString(),
                                name = "Ingresso",
                                description = "Descrição",
                                price = "50.00",
                                totalQuantity = 100,
                                maxPerCustomer = 0,
                                salesStartDate = null,
                                salesEndDate = null
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createTicketTypeUseCase.execute(request)
                                }
                        }
                assertEquals("Quantidade deve ser pelo menos 1", exception.message)
        }

        @Test
        fun `deve falhar quando eventId é inválido`() = runTest {
                // Arrange
                val request =
                        CreateTicketTypeRequest(
                                eventId = "invalid-uuid",
                                name = "Ingresso",
                                description = "Descrição",
                                price = "50.00",
                                totalQuantity = 100,
                                maxPerCustomer = 4,
                                salesStartDate = null,
                                salesEndDate = null
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createTicketTypeUseCase.execute(request)
                                }
                        }
                assertEquals("Event ID inválido", exception.message)
        }
}
