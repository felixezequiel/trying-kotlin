package events.adapters.outbound

import events.application.ports.outbound.IEventRepository
import events.application.ports.outbound.ITransactionManager
import events.application.ports.outbound.IUnitOfWork

class UnitOfWorkAdapter(
        override val eventRepository: IEventRepository,
        private val transactionManager: ITransactionManager
) : IUnitOfWork {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
