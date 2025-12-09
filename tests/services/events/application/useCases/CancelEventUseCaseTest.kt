import events.adapters.outbound.EventRepositoryAdapter
import events.application.useCases.CancelEventUseCase
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

class CancelEventUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter
    private lateinit var cancelEventUseCase: CancelEventUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
        cancelEventUseCase = CancelEventUseCase(eventRepository)
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
    fun `deve cancelar evento DRAFT com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.DRAFT)
        val eventId = eventRepository.add(event)

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
        val eventId = eventRepository.add(event)

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
        val eventId = eventRepository.add(event)

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
        val eventId = eventRepository.add(event)

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
        val eventId = eventRepository.add(event)

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
        val eventId = eventRepository.add(event)

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
