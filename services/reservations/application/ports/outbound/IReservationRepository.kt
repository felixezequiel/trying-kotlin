package reservations.application.ports.outbound

import java.util.UUID
import reservations.domain.Reservation

interface IReservationRepository {
    fun save(reservation: Reservation): Reservation
    fun findById(id: UUID): Reservation?
    fun findByCustomerId(customerId: UUID): List<Reservation>
    fun findByEventId(eventId: UUID): List<Reservation>
    fun update(reservation: Reservation): Reservation
}
