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

class DatabaseContextTest {

    private lateinit var dbContext: DatabaseContext

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
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
    fun `deve adicionar evento`() {
        // Arrange
        val event = createTestEvent()

        // Act
        val eventId = dbContext.addEvent(event)

        // Assert
        assertNotNull(eventId)
        val foundEvent = dbContext.findById(eventId)
        assertNotNull(foundEvent)
    }

    @Test
    fun `deve encontrar evento por ID`() {
        // Arrange
        val event = createTestEvent()
        val eventId = dbContext.addEvent(event)

        // Act
        val foundEvent = dbContext.findById(eventId)

        // Assert
        assertNotNull(foundEvent)
        assertEquals(event.name, foundEvent?.name)
    }

    @Test
    fun `deve encontrar eventos por partnerId`() {
        // Arrange
        val partnerId = UUID.randomUUID()
        dbContext.addEvent(createTestEvent(partnerId = partnerId))
        dbContext.addEvent(createTestEvent(partnerId = partnerId))
        dbContext.addEvent(createTestEvent(partnerId = UUID.randomUUID()))

        // Act
        val events = dbContext.findByPartnerId(partnerId)

        // Assert
        assertEquals(2, events.size)
    }

    @Test
    fun `deve encontrar eventos por status`() {
        // Arrange
        dbContext.addEvent(createTestEvent(status = EventStatus.DRAFT))
        dbContext.addEvent(createTestEvent(status = EventStatus.PUBLISHED))

        // Act
        val draftEvents = dbContext.findByStatus(EventStatus.DRAFT)

        // Assert
        assertEquals(1, draftEvents.size)
    }

    @Test
    fun `deve encontrar eventos publicados`() {
        // Arrange
        dbContext.addEvent(createTestEvent(status = EventStatus.DRAFT))
        dbContext.addEvent(createTestEvent(status = EventStatus.PUBLISHED))
        dbContext.addEvent(createTestEvent(status = EventStatus.PUBLISHED))

        // Act
        val publishedEvents = dbContext.findPublishedEvents()

        // Assert
        assertEquals(2, publishedEvents.size)
    }

    @Test
    fun `deve atualizar evento`() {
        // Arrange
        val event = createTestEvent()
        val eventId = dbContext.addEvent(event)
        val updatedEvent = event.copy(name = "Nome Atualizado")

        // Act
        val result = dbContext.updateEvent(updatedEvent)

        // Assert
        assertTrue(result)
        val foundEvent = dbContext.findById(eventId)
        assertEquals("Nome Atualizado", foundEvent?.name)
    }

    @Test
    fun `deve deletar evento`() {
        // Arrange
        val event = createTestEvent()
        val eventId = dbContext.addEvent(event)

        // Act
        val result = dbContext.deleteEvent(eventId)

        // Assert
        assertTrue(result)
        assertNull(dbContext.findById(eventId))
    }

    @Test
    fun `deve retornar todos os eventos`() {
        // Arrange
        dbContext.addEvent(createTestEvent())
        dbContext.addEvent(createTestEvent())
        dbContext.addEvent(createTestEvent())

        // Act
        val allEvents = dbContext.getAllEvents()

        // Assert
        assertEquals(3, allEvents.size)
    }

    @Test
    fun `executeTransaction deve fazer rollback em caso de erro`() = runTest {
        // Arrange
        val event = createTestEvent()
        dbContext.addEvent(event)

        // Act & Assert
        try {
            dbContext.executeTransaction {
                dbContext.addEvent(createTestEvent())
                throw RuntimeException("Erro simulado")
            }
        } catch (e: RuntimeException) {
            // Expected
        }

        // Verificar que o rollback foi feito
        val allEvents = dbContext.getAllEvents()
        assertEquals(1, allEvents.size)
    }
}
