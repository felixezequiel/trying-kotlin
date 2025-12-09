package reservations.infrastructure.persistence

import java.util.UUID
import reservations.domain.Reservation

/**
 * Contexto de banco de dados para o serviço de reservas. Armazenamento em memória até integração
 * com banco real.
 */
class DatabaseContext {
    private val reservations = mutableListOf<Reservation>()

    suspend fun <T> executeTransaction(block: suspend () -> T): T {
        val snapshot = reservations.toList()
        try {
            return block()
        } catch (e: Exception) {
            reservations.clear()
            reservations.addAll(snapshot)
            throw e
        }
    }

    fun addReservation(reservation: Reservation): UUID {
        reservations.add(reservation)
        return reservation.id
    }

    fun findById(id: UUID): Reservation? {
        return reservations.find { it.id == id }
    }

    fun findByCustomerId(customerId: UUID): List<Reservation> {
        return reservations.filter { it.customerId == customerId }
    }

    fun findByEventId(eventId: UUID): List<Reservation> {
        return reservations.filter { it.eventId == eventId }
    }

    fun updateReservation(reservation: Reservation): Boolean {
        val index = reservations.indexOfFirst { it.id == reservation.id }
        if (index == -1) return false
        reservations[index] = reservation
        return true
    }
}
