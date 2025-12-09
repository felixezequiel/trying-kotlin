import events.adapters.outbound.EventRepositoryAdapter
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

class EventRepositoryAdapterTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
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
    fun `deve adicionar evento e retornar ID`() = runTest {
        // Arrange
        val event = createTestEvent()

        // Act
        val eventId = eventRepository.add(event)

        // Assert
        assertNotNull(eventId)
        assertEquals(event.id, eventId)
    }

    @Test
    fun `deve buscar evento por ID`() = runTest {
        // Arrange
        val event = createTestEvent()
        val eventId = eventRepository.add(event)

        // Act
        val foundEvent = eventRepository.getById(eventId)

        // Assert
        assertNotNull(foundEvent)
        assertEquals(event.name, foundEvent?.name)
    }

    @Test
    fun `deve retornar null quando evento não existe`() = runTest {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val foundEvent = eventRepository.getById(nonExistentId)

        // Assert
        assertNull(foundEvent)
    }

    @Test
    fun `deve buscar eventos por partnerId`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        eventRepository.add(createTestEvent(partnerId = partnerId))
        eventRepository.add(createTestEvent(partnerId = partnerId))
        eventRepository.add(createTestEvent(partnerId = UUID.randomUUID()))

        // Act
        val events = eventRepository.getByPartnerId(partnerId)

        // Assert
        assertEquals(2, events.size)
        assertTrue(events.all { it.partnerId == partnerId })
    }

    @Test
    fun `deve buscar eventos por status`() = runTest {
        // Arrange
        eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))

        // Act
        val publishedEvents = eventRepository.getByStatus(EventStatus.PUBLISHED)

        // Assert
        assertEquals(2, publishedEvents.size)
    }

    @Test
    fun `deve buscar eventos publicados`() = runTest {
        // Arrange
        eventRepository.add(createTestEvent(status = EventStatus.DRAFT))
        eventRepository.add(createTestEvent(status = EventStatus.PUBLISHED))

        // Act
        val publishedEvents = eventRepository.getPublishedEvents()

        // Assert
        assertEquals(1, publishedEvents.size)
        assertEquals(EventStatus.PUBLISHED, publishedEvents.first().status)
    }

    @Test
    fun `deve atualizar evento`() = runTest {
        // Arrange
        val event = createTestEvent()
        val eventId = eventRepository.add(event)
        val updatedEvent = event.copy(name = "Nome Atualizado")

        // Act
        val result = eventRepository.update(updatedEvent)

        // Assert
        assertTrue(result)
        val foundEvent = eventRepository.getById(eventId)
        assertEquals("Nome Atualizado", foundEvent?.name)
    }

    @Test
    fun `deve deletar evento`() = runTest {
        // Arrange
        val event = createTestEvent()
        val eventId = eventRepository.add(event)

        // Act
        val result = eventRepository.delete(eventId)

        // Assert
        assertTrue(result)
        assertNull(eventRepository.getById(eventId))
    }

    @Test
    fun `deve retornar false ao deletar evento inexistente`() = runTest {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = eventRepository.delete(nonExistentId)

        // Assert
        assertFalse(result)
    }
}
