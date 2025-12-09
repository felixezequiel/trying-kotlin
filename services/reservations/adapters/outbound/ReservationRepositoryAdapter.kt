package reservations.adapters.outbound

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import reservations.application.ports.outbound.IReservationRepository
import reservations.domain.Reservation
import reservations.infrastructure.persistence.DatabaseContext

class ReservationRepositoryAdapter(private val dbContext: DatabaseContext) :
        IReservationRepository {

    // In-memory storage (será substituído por banco de dados real)
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
}
