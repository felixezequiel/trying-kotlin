import events.adapters.outbound.InMemoryEventStore
import events.adapters.outbound.UnitOfWorkAdapter
import events.application.useCases.ListEventsUseCase
import events.domain.Event
import events.domain.EventStatus
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.events.TestHelpers

class ListEventsUseCaseTest {

    private lateinit var eventStore: InMemoryEventStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var listEventsUseCase: ListEventsUseCase

    @BeforeEach
    fun setUp() {
        eventStore = InMemoryEventStore()
        unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)
        listEventsUseCase = ListEventsUseCase(unitOfWork)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.DRAFT,
            name: String = "Evento Teste"
    ): Event {
        return TestHelpers.createTestEvent(partnerId = partnerId, status = status, name = name)
    }

    @Test
    fun `executePublic deve retornar apenas eventos PUBLISHED`() = runTest {
        // Arrange
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.DRAFT, name = "Draft"))
        unitOfWork.eventRepository.add(
                createTestEvent(status = EventStatus.PUBLISHED, name = "Published 1")
        )
        unitOfWork.eventRepository.add(
                createTestEvent(status = EventStatus.PUBLISHED, name = "Published 2")
        )
        unitOfWork.eventRepository.add(
                createTestEvent(status = EventStatus.CANCELLED, name = "Cancelled")
        )

        // Act
        val events = listEventsUseCase.executePublic()

        // Assert
        assertEquals(2, events.size)
        assertTrue(events.all { it.status == EventStatus.PUBLISHED })
    }

    @Test
    fun `executeByPartner deve retornar apenas eventos do partner`() = runTest {
        // Arrange
        val partnerId1 = UUID.randomUUID()
        val partnerId2 = UUID.randomUUID()

        unitOfWork.eventRepository.add(
                createTestEvent(partnerId = partnerId1, name = "Partner1 Event1")
        )
        unitOfWork.eventRepository.add(
                createTestEvent(partnerId = partnerId1, name = "Partner1 Event2")
        )
        unitOfWork.eventRepository.add(
                createTestEvent(partnerId = partnerId2, name = "Partner2 Event1")
        )

        // Act
        val events = listEventsUseCase.executeByPartner(partnerId1)

        // Assert
        assertEquals(2, events.size)
        assertTrue(events.all { it.partnerId == partnerId1 })
    }

    @Test
    fun `executeByStatus deve retornar eventos com status específico`() = runTest {
        // Arrange
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))

        // Act
        val draftEvents = listEventsUseCase.executeByStatus(EventStatus.DRAFT)

        // Assert
        assertEquals(2, draftEvents.size)
        assertTrue(draftEvents.all { it.status == EventStatus.DRAFT })
    }

    @Test
    fun `executeAll deve retornar todos os eventos`() = runTest {
        // Arrange
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))
        unitOfWork.eventRepository.add(createTestEvent(status = EventStatus.CANCELLED))

        // Act
        val allEvents = listEventsUseCase.executeAll()

        // Assert
        assertEquals(3, allEvents.size)
    }

    @Test
    fun `deve retornar lista vazia quando não há eventos`() = runTest {
        // Act
        val events = listEventsUseCase.executePublic()

        // Assert
        assertTrue(events.isEmpty())
    }
}
