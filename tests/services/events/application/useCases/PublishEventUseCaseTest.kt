import events.adapters.outbound.InMemoryEventStore
import events.adapters.outbound.UnitOfWorkAdapter
import events.application.useCases.PublishEventUseCase
import events.domain.Event
import events.domain.EventStatus
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.events.TestHelpers

class PublishEventUseCaseTest {

    private lateinit var eventStore: InMemoryEventStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var publishEventUseCase: PublishEventUseCase

    @BeforeEach
    fun setUp() {
        eventStore = InMemoryEventStore()
        unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)
        publishEventUseCase = PublishEventUseCase(unitOfWork)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.DRAFT
    ): Event {
        return TestHelpers.createTestEvent(partnerId = partnerId, status = status)
    }

    @Test
    fun `deve publicar evento com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId)
        val eventId = unitOfWork.eventRepository.add(event)

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
        val eventId = unitOfWork.eventRepository.add(event)

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
        val eventId = unitOfWork.eventRepository.add(event)

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
