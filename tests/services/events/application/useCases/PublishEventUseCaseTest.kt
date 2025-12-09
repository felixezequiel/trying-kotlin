import events.adapters.outbound.EventRepositoryAdapter
import events.application.useCases.PublishEventUseCase
import events.domain.Event
import events.domain.EventStatus
import events.domain.Venue
import events.infrastructure.persistence.DatabaseContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PublishEventUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter
    private lateinit var publishEventUseCase: PublishEventUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
        publishEventUseCase = PublishEventUseCase(eventRepository)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.DRAFT
    ): Event {
        return Event(
                partnerId = partnerId,
                name = "Evento Teste",
                description = "Descrição do evento",
                venue =
                        Venue(
                                name = "Local Teste",
                                address = "Rua Teste, 1",
                                city = "São Paulo",
                                state = "SP",
                                zipCode = "01000-000",
                                capacity = 1000
                        ),
                startDate = Instant.now().plus(7, ChronoUnit.DAYS),
                endDate = Instant.now().plus(8, ChronoUnit.DAYS),
                status = status
        )
    }

    @Test
    fun `deve publicar evento com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId)
        val eventId = eventRepository.add(event)

        // Act
        val publishedEvent = publishEventUseCase.execute(eventId, partnerId)

        // Assert
        assertEquals(EventStatus.PUBLISHED, publishedEvent.status)
        assertNotNull(publishedEvent.publishedAt)
    }

    @Test
    fun `deve falhar quando evento não existe`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val nonExistentEventId = UUID.randomUUID()

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        publishEventUseCase.execute(nonExistentEventId, partnerId)
                    }
                }
        assertEquals("Evento não encontrado", exception.message)
    }

    @Test
    fun `deve falhar quando partner não é dono do evento`() = runTest {
        // Arrange
        val ownerPartnerId = UUID.randomUUID()
        val otherPartnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = ownerPartnerId)
        val eventId = eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        publishEventUseCase.execute(eventId, otherPartnerId)
                    }
                }
        assertEquals("Você não tem permissão para publicar este evento", exception.message)
    }

    @Test
    fun `deve falhar quando evento já está publicado`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.PUBLISHED)
        val eventId = eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        publishEventUseCase.execute(eventId, partnerId)
                    }
                }
        assertEquals("Apenas eventos em DRAFT podem ser publicados", exception.message)
    }
}
