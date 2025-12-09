package services.reservations

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import reservations.application.ports.outbound.IReservationRepository
import reservations.domain.Reservation

/** Fake repository para testes unitários. */
class FakeReservationRepository : IReservationRepository {
    private val reservations = ConcurrentHashMap<UUID, Reservation>()

    override fun save(reservation: Reservation): Reservation {
        reservations[reservation.id] = reservation
        return reservation
    }

    override fun findById(id: UUID): Reservation? {
        return reservations[id]
    }

    override fun findByCustomerId(customerId: UUID): List<Reservation> {
        return reservations.values.filter { it.customerId == customerId }
    }

    override fun findByEventId(eventId: UUID): List<Reservation> {
        return reservations.values.filter { it.eventId == eventId }
    }

    override fun update(reservation: Reservation): Reservation {
        reservations[reservation.id] = reservation
        return reservation
    }

    fun clear() {
        reservations.clear()
    }

    fun count(): Int = reservations.size
}
