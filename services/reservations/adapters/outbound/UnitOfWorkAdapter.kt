package reservations.adapters.outbound

import reservations.application.ports.outbound.IReservationRepository
import reservations.application.ports.outbound.ITransactionManager
import reservations.application.ports.outbound.IUnitOfWork

class UnitOfWorkAdapter(
        override val reservationRepository: IReservationRepository,
        private val transactionManager: ITransactionManager
) : IUnitOfWork {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
