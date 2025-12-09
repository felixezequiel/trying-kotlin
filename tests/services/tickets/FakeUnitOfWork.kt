package services.tickets

import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.application.ports.outbound.IUnitOfWork

/** Fake UnitOfWork para testes unitários. Usa FakeTicketTypeRepository internamente. */
class FakeUnitOfWork(
        override val ticketTypeRepository: ITicketTypeRepository = FakeTicketTypeRepository()
) : IUnitOfWork {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return block()
    }
}
