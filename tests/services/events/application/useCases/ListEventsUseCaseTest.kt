import events.adapters.outbound.EventRepositoryAdapter
import events.application.useCases.ListEventsUseCase
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

class ListEventsUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter
    private lateinit var listEventsUseCase: ListEventsUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
        listEventsUseCase = ListEventsUseCase(eventRepository)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.DRAFT,
            name: String = "Evento Teste"
    ): Event {
        return Event(
                partnerId = partnerId,
                name = name,
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
    fun `executePublic deve retornar apenas eventos PUBLISHED`() = runTest {
        // Arrange
        eventRepository.add(createTestEvent(status = EventStatus.DRAFT, name = "Draft"))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED, name = "Published 1"))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED, name = "Published 2"))
        eventRepository.add(createTestEvent(status = EventStatus.CANCELLED, name = "Cancelled"))

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

        eventRepository.add(createTestEvent(partnerId = partnerId1, name = "Partner1 Event1"))
        eventRepository.add(createTestEvent(partnerId = partnerId1, name = "Partner1 Event2"))
        eventRepository.add(createTestEvent(partnerId = partnerId2, name = "Partner2 Event1"))

        // Act
        val events = listEventsUseCase.executeByPartner(partnerId1)

        // Assert
        assertEquals(2, events.size)
        assertTrue(events.all { it.partnerId == partnerId1 })
    }

    @Test
    fun `executeByStatus deve retornar eventos com status específico`() = runTest {
        // Arrange
        eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))

        // Act
        val draftEvents = listEventsUseCase.executeByStatus(EventStatus.DRAFT)

        // Assert
        assertEquals(2, draftEvents.size)
        assertTrue(draftEvents.all { it.status == EventStatus.DRAFT })
    }

    @Test
    fun `executeAll deve retornar todos os eventos`() = runTest {
        // Arrange
        eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))
        eventRepository.add(createTestEvent(status = EventStatus.CANCELLED))

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
