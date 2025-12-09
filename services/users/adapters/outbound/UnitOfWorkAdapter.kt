package users.adapters.outbound

import users.infrastructure.persistence.DatabaseContext
import users.application.ports.outbound.IUnitOfWork
import users.application.ports.outbound.IUserRepository

class UnitOfWorkAdapter(private val dbContext: DatabaseContext) : IUnitOfWork {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return dbContext.executeTransaction(block)
    }

    override fun userRepository(): IUserRepository {
        return UserRepositoryAdapter(dbContext)
    }
}
