import events.adapters.outbound.EventRepositoryAdapter
import events.application.useCases.GetEventUseCase
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

class GetEventUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter
    private lateinit var getEventUseCase: GetEventUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
        getEventUseCase = GetEventUseCase(eventRepository)
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
    fun `deve buscar evento por ID`() = runTest {
        // Arrange
        val event = createTestEvent()
        val eventId = eventRepository.add(event)

        // Act
        val foundEvent = getEventUseCase.execute(eventId)

        // Assert
        assertNotNull(foundEvent)
        assertEquals(eventId, foundEvent?.id)
    }

    @Test
    fun `deve retornar null quando evento não existe`() = runTest {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val foundEvent = getEventUseCase.execute(nonExistentId)

        // Assert
        assertNull(foundEvent)
    }

    @Test
    fun `executePublic deve retornar evento PUBLISHED`() = runTest {
        // Arrange
        val event = createTestEvent(status = EventStatus.PUBLISHED)
        val eventId = eventRepository.add(event)

        // Act
        val foundEvent = getEventUseCase.executePublic(eventId)

        // Assert
        assertNotNull(foundEvent)
        assertEquals(EventStatus.PUBLISHED, foundEvent?.status)
    }

    @Test
    fun `executePublic deve retornar null para evento DRAFT`() = runTest {
        // Arrange
        val event = createTestEvent(status = EventStatus.DRAFT)
        val eventId = eventRepository.add(event)

        // Act
        val foundEvent = getEventUseCase.executePublic(eventId)

        // Assert
        assertNull(foundEvent)
    }

    @Test
    fun `executeForPartner deve retornar evento do próprio partner`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.DRAFT)
        val eventId = eventRepository.add(event)

        // Act
        val foundEvent = getEventUseCase.executeForPartner(eventId, partnerId)

        // Assert
        assertNotNull(foundEvent)
        assertEquals(partnerId, foundEvent?.partnerId)
    }

    @Test
    fun `executeForPartner deve retornar null para evento de outro partner`() = runTest {
        // Arrange
        val ownerPartnerId = UUID.randomUUID()
        val otherPartnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = ownerPartnerId)
        val eventId = eventRepository.add(event)

        // Act
        val foundEvent = getEventUseCase.executeForPartner(eventId, otherPartnerId)

        // Assert
        assertNull(foundEvent)
    }
}
