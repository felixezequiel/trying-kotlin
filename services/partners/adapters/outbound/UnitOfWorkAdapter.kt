package partners.adapters.outbound

import partners.application.ports.outbound.IPartnerRepository
import partners.application.ports.outbound.ITransactionManager
import partners.application.ports.outbound.IUnitOfWork

class UnitOfWorkAdapter(
        override val partnerRepository: IPartnerRepository,
        private val transactionManager: ITransactionManager
) : IUnitOfWork {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
