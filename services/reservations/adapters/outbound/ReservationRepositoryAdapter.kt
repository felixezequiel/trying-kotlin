package reservations.adapters.outbound

import java.util.UUID
import reservations.application.ports.outbound.IReservationRepository
import reservations.domain.Reservation
import reservations.infrastructure.persistence.DatabaseContext

class ReservationRepositoryAdapter(private val dbContext: DatabaseContext) :
        IReservationRepository {

    override fun save(reservation: Reservation): Reservation {
        dbContext.addReservation(reservation)
        return reservation
    }

    override fun findById(id: UUID): Reservation? {
        return dbContext.findById(id)
    }

    override fun findByCustomerId(customerId: UUID): List<Reservation> {
        return dbContext.findByCustomerId(customerId)
    }

    override fun findByEventId(eventId: UUID): List<Reservation> {
        return dbContext.findByEventId(eventId)
    }

    override fun update(reservation: Reservation): Reservation {
        dbContext.updateReservation(reservation)
        return reservation
    }
}
