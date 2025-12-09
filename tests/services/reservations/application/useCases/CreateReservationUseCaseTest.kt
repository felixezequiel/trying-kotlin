package services.reservations.application.useCases

import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reservations.application.dto.CreateReservationItemRequest
import reservations.application.dto.CreateReservationRequest
import reservations.application.useCases.CreateReservationUseCase
import reservations.domain.ReservationStatus
import services.reservations.FakeReservationRepository
import services.reservations.FakeTicketsClient
import services.reservations.TestHelpers

class CreateReservationUseCaseTest {

        private lateinit var reservationRepository: FakeReservationRepository
        private lateinit var ticketsClient: FakeTicketsClient
        private lateinit var createReservationUseCase: CreateReservationUseCase

        @BeforeEach
        fun setUp() {
                reservationRepository = FakeReservationRepository()
                ticketsClient = FakeTicketsClient()
                createReservationUseCase =
                        CreateReservationUseCase(reservationRepository, ticketsClient)
        }

        @Test
        fun `deve criar reserva com sucesso`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val ticketTypeId = UUID.randomUUID()

                val ticketTypeInfo =
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId,
                                eventId = eventId,
                                name = "VIP",
                                price = BigDecimal("150.00"),
                                availableQuantity = 100,
                                maxPerCustomer = 4
                        )
                ticketsClient.addTicketType(ticketTypeInfo)

                val request =
                        CreateReservationRequest(
                                eventId = eventId.toString(),
                                items =
                                        listOf(
                                                CreateReservationItemRequest(
                                                        ticketTypeId = ticketTypeId.toString(),
                                                        quantity = 2
                                                )
                                        )
                        )

                // Act
                val reservation = createReservationUseCase.execute(customerId, request)

                // Assert
                assertNotNull(reservation)
                assertEquals(customerId, reservation.customerId)
                assertEquals(eventId, reservation.eventId)
                assertEquals(ReservationStatus.ACTIVE, reservation.status)
                assertEquals(1, reservation.items.size)
                assertEquals(2, reservation.items[0].quantity.value)
                assertEquals("VIP", reservation.items[0].ticketTypeName)
                assertEquals(BigDecimal("300.00"), reservation.totalAmount.value)

                // Verifica que os ingressos foram reservados
                assertEquals(2, ticketsClient.getReservedQuantity(ticketTypeId))
        }

        @Test
        fun `deve criar reserva com múltiplos itens`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val ticketTypeId1 = UUID.randomUUID()
                val ticketTypeId2 = UUID.randomUUID()

                ticketsClient.addTicketType(
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId1,
                                eventId = eventId,
                                name = "VIP",
                                price = BigDecimal("150.00")
                        )
                )
                ticketsClient.addTicketType(
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId2,
                                eventId = eventId,
                                name = "Normal",
                                price = BigDecimal("50.00")
                        )
                )

                val request =
                        CreateReservationRequest(
                                eventId = eventId.toString(),
                                items =
                                        listOf(
                                                CreateReservationItemRequest(
                                                        ticketTypeId1.toString(),
                                                        2
                                                ),
                                                CreateReservationItemRequest(
                                                        ticketTypeId2.toString(),
                                                        3
                                                )
                                        )
                        )

                // Act
                val reservation = createReservationUseCase.execute(customerId, request)

                // Assert
                assertEquals(2, reservation.items.size)
                // VIP: 2 * 150 = 300, Normal: 3 * 50 = 150, Total = 450
                assertEquals(BigDecimal("450.00"), reservation.totalAmount.value)
        }

        @Test
        fun `deve falhar quando reserva não tem itens`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()

                val request =
                        CreateReservationRequest(eventId = eventId.toString(), items = emptyList())

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createReservationUseCase.execute(customerId, request)
                                }
                        }
                assertEquals("Reserva deve ter pelo menos 1 item", exception.message)
        }

        @Test
        fun `deve falhar quando tipo de ingresso não existe`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val ticketTypeId = UUID.randomUUID()

                val request =
                        CreateReservationRequest(
                                eventId = eventId.toString(),
                                items =
                                        listOf(
                                                CreateReservationItemRequest(
                                                        ticketTypeId.toString(),
                                                        2
                                                )
                                        )
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createReservationUseCase.execute(customerId, request)
                                }
                        }
                assertTrue(exception.message?.contains("Tipo de ingresso não encontrado") == true)
        }

        @Test
        fun `deve falhar quando ticket type é de evento diferente`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val otherEventId = UUID.randomUUID()
                val ticketTypeId = UUID.randomUUID()

                ticketsClient.addTicketType(
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId,
                                eventId = otherEventId, // Evento diferente
                                name = "VIP"
                        )
                )

                val request =
                        CreateReservationRequest(
                                eventId = eventId.toString(),
                                items =
                                        listOf(
                                                CreateReservationItemRequest(
                                                        ticketTypeId.toString(),
                                                        2
                                                )
                                        )
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createReservationUseCase.execute(customerId, request)
                                }
                        }
                assertEquals("Todos os ingressos devem ser do mesmo evento", exception.message)
        }

        @Test
        fun `deve falhar quando quantidade excede maxPerCustomer`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val ticketTypeId = UUID.randomUUID()

                ticketsClient.addTicketType(
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId,
                                eventId = eventId,
                                name = "VIP",
                                maxPerCustomer = 2 // Máximo de 2 por cliente
                        )
                )

                val request =
                        CreateReservationRequest(
                                eventId = eventId.toString(),
                                items =
                                        listOf(
                                                CreateReservationItemRequest(
                                                        ticketTypeId.toString(),
                                                        5
                                                ) // Tentando reservar 5
                                        )
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createReservationUseCase.execute(customerId, request)
                                }
                        }
                assertTrue(exception.message?.contains("Quantidade máxima por cliente") == true)
        }

        @Test
        fun `deve fazer rollback quando falha na reserva de segundo item`() = runTest {
                // Arrange
                val customerId = UUID.randomUUID()
                val eventId = UUID.randomUUID()
                val ticketTypeId1 = UUID.randomUUID()
                val ticketTypeId2 = UUID.randomUUID()

                ticketsClient.addTicketType(
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId1,
                                eventId = eventId,
                                name = "VIP",
                                availableQuantity = 100
                        )
                )
                ticketsClient.addTicketType(
                        TestHelpers.createTestTicketTypeInfo(
                                id = ticketTypeId2,
                                eventId = eventId,
                                name = "Normal",
                                availableQuantity = 1, // Só tem 1 disponível
                                maxPerCustomer = 10 // Permite até 10 por cliente
                        )
                )

                val request =
                        CreateReservationRequest(
                                eventId = eventId.toString(),
                                items =
                                        listOf(
                                                CreateReservationItemRequest(
                                                        ticketTypeId1.toString(),
                                                        2
                                                ),
                                                CreateReservationItemRequest(
                                                        ticketTypeId2.toString(),
                                                        3 // Dentro do maxPerCustomer mas excede
                                                        // availableQuantity
                                                        ) // Vai falhar
                                        )
                        )

                // Act & Assert
                assertThrows(IllegalStateException::class.java) {
                        kotlinx.coroutines.runBlocking {
                                createReservationUseCase.execute(customerId, request)
                        }
                }

                // Verifica que o rollback foi feito - primeiro item deve ter sido liberado
                assertEquals(0, ticketsClient.getReservedQuantity(ticketTypeId1))
                assertEquals(0, reservationRepository.count())
        }
}
