import events.adapters.outbound.EventRepositoryAdapter
import events.application.useCases.FinishEventUseCase
import events.domain.Event
import events.domain.EventStatus
import events.infrastructure.persistence.DatabaseContext
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.events.TestHelpers

class FinishEventUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter
    private lateinit var finishEventUseCase: FinishEventUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
        finishEventUseCase = FinishEventUseCase(eventRepository)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.PUBLISHED
    ): Event {
        return TestHelpers.createTestEvent(partnerId = partnerId, status = status)
    }

    @Test
    fun `deve finalizar evento PUBLISHED com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.PUBLISHED)
        val eventId = eventRepository.add(event)

        // Act
        val finishedEvent = finishEventUseCase.execute(eventId, partnerId)

        // Assert
        assertEquals(EventStatus.FINISHED, finishedEvent.status)
    }

    @Test
    fun `admin deve poder finalizar evento de qualquer partner`() = runTest {
        // Arrange
        val ownerPartnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = ownerPartnerId, status = EventStatus.PUBLISHED)
        val eventId = eventRepository.add(event)

        // Act
        val finishedEvent = finishEventUseCase.execute(eventId, partnerId = null, isAdmin = true)

        // Assert
        assertEquals(EventStatus.FINISHED, finishedEvent.status)
    }

    @Test
    fun `deve falhar quando evento não está publicado`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.DRAFT)
        val eventId = eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        finishEventUseCase.execute(eventId, partnerId)
                    }
                }
        assertEquals("Apenas eventos publicados podem ser finalizados", exception.message)
    }

    @Test
    fun `deve falhar quando partner não é dono e não é admin`() = runTest {
        // Arrange
        val ownerPartnerId = UUID.randomUUID()
        val otherPartnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = ownerPartnerId, status = EventStatus.PUBLISHED)
        val eventId = eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        finishEventUseCase.execute(eventId, otherPartnerId, isAdmin = false)
                    }
                }
        assertEquals("Você não tem permissão para finalizar este evento", exception.message)
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
                        finishEventUseCase.execute(nonExistentEventId, partnerId)
                    }
                }
        assertEquals("Evento não encontrado", exception.message)
    }
}
