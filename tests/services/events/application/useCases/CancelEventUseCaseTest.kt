import events.adapters.outbound.InMemoryEventStore
import events.adapters.outbound.UnitOfWorkAdapter
import events.application.useCases.CancelEventUseCase
import events.domain.Event
import events.domain.EventStatus
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.events.TestHelpers

class CancelEventUseCaseTest {

    private lateinit var eventStore: InMemoryEventStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var cancelEventUseCase: CancelEventUseCase

    @BeforeEach
    fun setUp() {
        eventStore = InMemoryEventStore()
        unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)
        cancelEventUseCase = CancelEventUseCase(unitOfWork)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.DRAFT
    ): Event {
        return TestHelpers.createTestEvent(partnerId = partnerId, status = status)
    }

    @Test
    fun `deve cancelar evento DRAFT com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.DRAFT)
        val eventId = unitOfWork.eventRepository.add(event)

        // Act
        val cancelledEvent = cancelEventUseCase.execute(eventId, partnerId)

        // Assert
        assertEquals(EventStatus.CANCELLED, cancelledEvent.status)
    }

    @Test
    fun `deve cancelar evento PUBLISHED com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.PUBLISHED)
        val eventId = unitOfWork.eventRepository.add(event)

        // Act
        val cancelledEvent = cancelEventUseCase.execute(eventId, partnerId)

        // Assert
        assertEquals(EventStatus.CANCELLED, cancelledEvent.status)
    }

    @Test
    fun `admin deve poder cancelar evento de qualquer partner`() = runTest {
        // Arrange
        val ownerPartnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = ownerPartnerId, status = EventStatus.PUBLISHED)
        val eventId = unitOfWork.eventRepository.add(event)

        // Act
        val cancelledEvent = cancelEventUseCase.execute(eventId, partnerId = null, isAdmin = true)

        // Assert
        assertEquals(EventStatus.CANCELLED, cancelledEvent.status)
    }

    @Test
    fun `deve falhar quando evento já está cancelado`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.CANCELLED)
        val eventId = unitOfWork.eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        cancelEventUseCase.execute(eventId, partnerId)
                    }
                }
        assertEquals("Evento já está cancelado", exception.message)
    }

    @Test
    fun `deve falhar quando evento já está finalizado`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.FINISHED)
        val eventId = unitOfWork.eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        cancelEventUseCase.execute(eventId, partnerId)
                    }
                }
        assertEquals("Não é possível cancelar evento já finalizado", exception.message)
    }

    @Test
    fun `deve falhar quando partner não é dono e não é admin`() = runTest {
        // Arrange
        val ownerPartnerId = UUID.randomUUID()
        val otherPartnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = ownerPartnerId)
        val eventId = unitOfWork.eventRepository.add(event)

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        cancelEventUseCase.execute(eventId, otherPartnerId, isAdmin = false)
                    }
                }
        assertEquals("Você não tem permissão para cancelar este evento", exception.message)
    }
}
