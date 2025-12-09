package orders.adapters.outbound

import orders.application.ports.outbound.IIssuedTicketRepository
import orders.application.ports.outbound.IOrderRepository
import orders.application.ports.outbound.ITransactionManager
import orders.application.ports.outbound.IUnitOfWork

class UnitOfWorkAdapter(
        override val orderRepository: IOrderRepository,
        override val issuedTicketRepository: IIssuedTicketRepository,
        private val transactionManager: ITransactionManager
) : IUnitOfWork {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
