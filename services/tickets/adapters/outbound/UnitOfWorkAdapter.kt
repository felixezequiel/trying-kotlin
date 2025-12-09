package tickets.adapters.outbound

import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.application.ports.outbound.ITransactionManager
import tickets.application.ports.outbound.IUnitOfWork

class UnitOfWorkAdapter(
        override val ticketTypeRepository: ITicketTypeRepository,
        private val transactionManager: ITransactionManager
) : IUnitOfWork {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
