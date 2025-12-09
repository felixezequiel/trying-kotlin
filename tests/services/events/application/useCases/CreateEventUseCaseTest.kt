import events.adapters.outbound.InMemoryEventStore
import events.adapters.outbound.UnitOfWorkAdapter
import events.application.dto.CreateEventRequest
import events.application.dto.VenueRequest
import events.application.useCases.CreateEventUseCase
import events.domain.EventStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateEventUseCaseTest {

        private lateinit var eventStore: InMemoryEventStore
        private lateinit var unitOfWork: UnitOfWorkAdapter
        private lateinit var createEventUseCase: CreateEventUseCase

        @BeforeEach
        fun setUp() {
                eventStore = InMemoryEventStore()
                unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)
                createEventUseCase = CreateEventUseCase(unitOfWork)
        }

        @Test
        fun `deve criar evento com sucesso`() = runTest {
                // Arrange
                val partnerId = UUID.randomUUID()
                val startDate = Instant.now().plus(7, ChronoUnit.DAYS)
                val endDate = Instant.now().plus(8, ChronoUnit.DAYS)

                val request =
                        CreateEventRequest(
                                name = "Show de Rock",
                                description = "Um grande show de rock",
                                venue =
                                        VenueRequest(
                                                name = "Arena Show",
                                                address = "Rua Principal, 100",
                                                city = "São Paulo",
                                                state = "SP",
                                                zipCode = "01000-000",
                                                capacity = 5000
                                        ),
                                startDate = startDate.toString(),
                                endDate = endDate.toString(),
                                imageUrl = "https://example.com/image.jpg"
                        )

                // Act
                val eventId = createEventUseCase.execute(partnerId, request)

                // Assert
                assertNotNull(eventId)
                val event = unitOfWork.eventRepository.getById(eventId)
                assertNotNull(event)
                assertEquals("Show de Rock", event?.name?.value)
                assertEquals("Um grande show de rock", event?.description?.value)
                assertEquals(EventStatus.DRAFT, event?.status)
                assertEquals(partnerId, event?.partnerId)
                assertEquals("Arena Show", event?.venue?.name)
                assertEquals(5000, event?.venue?.capacity)
        }

        @Test
        fun `deve criar evento sem imagem`() = runTest {
                // Arrange
                val partnerId = UUID.randomUUID()
                val startDate = Instant.now().plus(7, ChronoUnit.DAYS)
                val endDate = Instant.now().plus(8, ChronoUnit.DAYS)

                val request =
                        CreateEventRequest(
                                name = "Evento Simples",
                                description = "Descrição do evento",
                                venue =
                                        VenueRequest(
                                                name = "Local Teste",
                                                address = "Rua Teste, 1",
                                                city = "Rio de Janeiro",
                                                state = "RJ",
                                                zipCode = "20000-000",
                                                capacity = null
                                        ),
                                startDate = startDate.toString(),
                                endDate = endDate.toString(),
                                imageUrl = null
                        )

                // Act
                val eventId = createEventUseCase.execute(partnerId, request)

                // Assert
                val event = unitOfWork.eventRepository.getById(eventId)
                assertNotNull(event)
                assertNull(event?.imageUrl)
                assertNull(event?.venue?.capacity)
        }

        @Test
        fun `deve falhar quando data de início é passada`() = runTest {
                // Arrange
                val partnerId = UUID.randomUUID()
                val startDate = Instant.now().minus(1, ChronoUnit.DAYS)
                val endDate = Instant.now().plus(1, ChronoUnit.DAYS)

                val request =
                        CreateEventRequest(
                                name = "Evento Passado",
                                description = "Descrição",
                                venue =
                                        VenueRequest(
                                                name = "Local",
                                                address = "Rua",
                                                city = "Cidade",
                                                state = "UF",
                                                zipCode = "00000-000",
                                                capacity = null
                                        ),
                                startDate = startDate.toString(),
                                endDate = endDate.toString()
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createEventUseCase.execute(partnerId, request)
                                }
                        }
                assertEquals("Data de início deve ser futura", exception.message)
        }

        @Test
        fun `deve falhar quando data de término é antes da data de início`() = runTest {
                // Arrange
                val partnerId = UUID.randomUUID()
                val startDate = Instant.now().plus(7, ChronoUnit.DAYS)
                val endDate = Instant.now().plus(5, ChronoUnit.DAYS)

                val request =
                        CreateEventRequest(
                                name = "Evento Inválido",
                                description = "Descrição",
                                venue =
                                        VenueRequest(
                                                name = "Local",
                                                address = "Rua",
                                                city = "Cidade",
                                                state = "UF",
                                                zipCode = "00000-000",
                                                capacity = null
                                        ),
                                startDate = startDate.toString(),
                                endDate = endDate.toString()
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createEventUseCase.execute(partnerId, request)
                                }
                        }
                assertEquals("Data de término deve ser após a data de início", exception.message)
        }
}
