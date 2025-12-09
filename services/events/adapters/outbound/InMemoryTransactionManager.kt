package events.adapters.outbound

import events.application.ports.outbound.ITransactionManager
import events.infrastructure.persistence.DatabaseContext

class InMemoryTransactionManager(private val dbContext: DatabaseContext) : ITransactionManager {
    override suspend fun <T> execute(block: suspend () -> T): T {
        return dbContext.executeTransaction(block)
    }
}
