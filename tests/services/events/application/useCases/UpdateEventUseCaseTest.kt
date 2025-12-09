import events.adapters.outbound.EventRepositoryAdapter
import events.application.dto.UpdateEventRequest
import events.application.dto.VenueRequest
import events.application.useCases.UpdateEventUseCase
import events.domain.Event
import events.domain.EventStatus
import events.infrastructure.persistence.DatabaseContext
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.events.TestHelpers

class UpdateEventUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var eventRepository: EventRepositoryAdapter
    private lateinit var updateEventUseCase: UpdateEventUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        eventRepository = EventRepositoryAdapter(dbContext)
        updateEventUseCase = UpdateEventUseCase(eventRepository)
    }

    private fun createTestEvent(
            partnerId: UUID = UUID.randomUUID(),
            status: EventStatus = EventStatus.DRAFT
    ): Event {
        return TestHelpers.createTestEvent(
                partnerId = partnerId,
                name = "Evento Original",
                description = "Descrição original",
                venueName = "Local Original",
                status = status
        )
    }

    @Test
    fun `deve atualizar nome do evento com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId)
        val eventId = eventRepository.add(event)

        val request = UpdateEventRequest(name = "Novo Nome do Evento")

        // Act
        val updatedEvent = updateEventUseCase.execute(eventId, partnerId, request)

        // Assert
        assertEquals("Novo Nome do Evento", updatedEvent.name.value)
        assertEquals("Descrição original", updatedEvent.description.value)
    }

    @Test
    fun `deve atualizar venue do evento com sucesso`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId)
        val eventId = eventRepository.add(event)

        val request =
                UpdateEventRequest(
                        venue =
                                VenueRequest(
                                        name = "Novo Local",
                                        address = "Nova Rua, 100",
                                        city = "Rio de Janeiro",
                                        state = "RJ",
                                        zipCode = "20000-000",
                                        capacity = 2000
                                )
                )

        // Act
        val updatedEvent = updateEventUseCase.execute(eventId, partnerId, request)

        // Assert
        assertEquals("Novo Local", updatedEvent.venue.name)
        assertEquals("Rio de Janeiro", updatedEvent.venue.city)
        assertEquals(2000, updatedEvent.venue.capacity)
    }

    @Test
    fun `deve falhar quando evento não existe`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val nonExistentEventId = UUID.randomUUID()
        val request = UpdateEventRequest(name = "Novo Nome")

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        updateEventUseCase.execute(nonExistentEventId, partnerId, request)
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

        val request = UpdateEventRequest(name = "Novo Nome")

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        updateEventUseCase.execute(eventId, otherPartnerId, request)
                    }
                }
        assertEquals("Você não tem permissão para editar este evento", exception.message)
    }

    @Test
    fun `deve falhar quando evento está cancelado`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.CANCELLED)
        val eventId = eventRepository.add(event)

        val request = UpdateEventRequest(name = "Novo Nome")

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        updateEventUseCase.execute(eventId, partnerId, request)
                    }
                }
        assertEquals("Não é possível editar evento CANCELLED", exception.message)
    }

    @Test
    fun `deve falhar quando evento está finalizado`() = runTest {
        // Arrange
        val partnerId = UUID.randomUUID()
        val event = createTestEvent(partnerId = partnerId, status = EventStatus.FINISHED)
        val eventId = eventRepository.add(event)

        val request = UpdateEventRequest(name = "Novo Nome")

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        updateEventUseCase.execute(eventId, partnerId, request)
                    }
                }
        assertEquals("Não é possível editar evento FINISHED", exception.message)
    }
}
