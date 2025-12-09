package reservations.adapters.outbound

import java.util.UUID
import reservations.application.ports.outbound.IReservationRepository
import reservations.application.ports.outbound.IReservationStore
import reservations.application.ports.outbound.ITransactionManager
import reservations.domain.Reservation

/**
 * Store em memória que gerencia reservations e transações. Implementa IReservationStore para
 * garantir que qualquer implementação (Postgres, etc.) tenha o mesmo contrato.
 */
class InMemoryReservationStore : IReservationStore {
    private val reservations = mutableListOf<Reservation>()

    override val repository: IReservationRepository = InMemoryReservationRepository()
    override val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()

    private inner class InMemoryReservationRepository : IReservationRepository {
        override fun save(reservation: Reservation): Reservation {
            reservations.add(reservation)
            return reservation
        }

        override fun findById(id: UUID): Reservation? {
            return reservations.find { it.id == id }
        }

        override fun findByCustomerId(customerId: UUID): List<Reservation> {
            return reservations.filter { it.customerId == customerId }
        }

        override fun findByEventId(eventId: UUID): List<Reservation> {
            return reservations.filter { it.eventId == eventId }
        }

        override fun update(reservation: Reservation): Reservation {
            val index = reservations.indexOfFirst { it.id == reservation.id }
            if (index != -1) {
                reservations[index] = reservation
            }
            return reservation
        }
    }

    private inner class InMemoryTransactionManagerImpl : ITransactionManager {
        override suspend fun <T> execute(block: suspend () -> T): T {
            println("Iniciando transação...")
            val snapshot = reservations.toList()
            try {
                val result = block()
                println("Transação concluída com sucesso (commit).")
                return result
            } catch (e: Exception) {
                println("Transação falhou (rollback): ${e.message}")
                reservations.clear()
                reservations.addAll(snapshot)
                throw e
            }
        }
    }
}
