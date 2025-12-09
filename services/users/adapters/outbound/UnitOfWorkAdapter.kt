package users.adapters.outbound

import users.application.ports.outbound.ITransactionManager
import users.application.ports.outbound.IUnitOfWork
import users.application.ports.outbound.IUserRepository

class UnitOfWorkAdapter(
        override val userRepository: IUserRepository,
        private val transactionManager: ITransactionManager
) : IUnitOfWork {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
