package services.reservations.domain

import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reservations.domain.CancellationType
import reservations.domain.Reservation
import reservations.domain.ReservationItem
import reservations.domain.ReservationStatus
import reservations.domain.valueObjects.Price
import reservations.domain.valueObjects.Quantity

class ReservationTest {

    @Test
    fun `deve criar reserva com sucesso`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val item =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.positive(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )

        // Act
        val reservation =
                Reservation.create(customerId = customerId, eventId = eventId, items = listOf(item))

        // Assert
        assertEquals(customerId, reservation.customerId)
        assertEquals(eventId, reservation.eventId)
        assertEquals(ReservationStatus.ACTIVE, reservation.status)
        assertEquals(1, reservation.items.size)
        assertEquals(BigDecimal("200.00"), reservation.totalAmount.value)
        assertNull(reservation.cancelledAt)
        assertNull(reservation.convertedAt)
    }

    @Test
    fun `deve calcular total com múltiplos itens`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val item1 =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.positive(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        val item2 =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "Normal",
                        quantity = Quantity.positive(3),
                        unitPrice = Price.of(BigDecimal("50.00"))
                )

        // Act
        val reservation =
                Reservation.create(
                        customerId = customerId,
                        eventId = eventId,
                        items = listOf(item1, item2)
                )

        // Assert
        // VIP: 2 * 100 = 200, Normal: 3 * 50 = 150, Total = 350
        assertEquals(BigDecimal("350.00"), reservation.totalAmount.value)
    }

    @Test
    fun `deve falhar quando reserva não tem itens`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    Reservation.create(
                            customerId = customerId,
                            eventId = eventId,
                            items = emptyList()
                    )
                }
        assertEquals("Reserva deve ter pelo menos 1 item", exception.message)
    }

    @Test
    fun `deve cancelar reserva com sucesso`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val item =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.positive(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        val reservation =
                Reservation.create(customerId = customerId, eventId = eventId, items = listOf(item))

        // Act
        val cancelled =
                reservation.cancel(
                        cancelledBy = customerId,
                        reason = "Não poderei ir",
                        cancellationType = CancellationType.BY_CUSTOMER
                )

        // Assert
        assertEquals(ReservationStatus.CANCELLED, cancelled.status)
        assertEquals(customerId, cancelled.cancelledBy)
        assertEquals("Não poderei ir", cancelled.cancellationReason)
        assertEquals(CancellationType.BY_CUSTOMER, cancelled.cancellationType)
        assertNotNull(cancelled.cancelledAt)
    }

    @Test
    fun `deve falhar ao cancelar reserva já cancelada`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val item =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.positive(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        val reservation =
                Reservation.create(customerId = customerId, eventId = eventId, items = listOf(item))
        val cancelled =
                reservation.cancel(
                        cancelledBy = customerId,
                        reason = null,
                        cancellationType = CancellationType.BY_CUSTOMER
                )

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    cancelled.cancel(
                            cancelledBy = customerId,
                            reason = null,
                            cancellationType = CancellationType.BY_CUSTOMER
                    )
                }
        assertEquals("Só pode cancelar reserva ACTIVE", exception.message)
    }

    @Test
    fun `deve converter reserva com sucesso`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val item =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.positive(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        val reservation =
                Reservation.create(customerId = customerId, eventId = eventId, items = listOf(item))

        // Act
        val converted = reservation.convert(orderId)

        // Assert
        assertEquals(ReservationStatus.CONVERTED, converted.status)
        assertEquals(orderId, converted.orderId)
        assertNotNull(converted.convertedAt)
    }

    @Test
    fun `deve falhar ao converter reserva cancelada`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val item =
                ReservationItem.create(
                        ticketTypeId = UUID.randomUUID(),
                        ticketTypeName = "VIP",
                        quantity = Quantity.positive(2),
                        unitPrice = Price.of(BigDecimal("100.00"))
                )
        val reservation =
                Reservation.create(customerId = customerId, eventId = eventId, items = listOf(item))
        val cancelled =
                reservation.cancel(
                        cancelledBy = customerId,
                        reason = null,
                        cancellationType = CancellationType.BY_CUSTOMER
                )

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) { cancelled.convert(orderId) }
        assertEquals("Só pode converter reserva ACTIVE", exception.message)
    }
}
