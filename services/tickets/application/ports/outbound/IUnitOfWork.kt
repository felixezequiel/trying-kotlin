package tickets.application.ports.outbound

interface IUnitOfWork {
    val ticketTypeRepository: ITicketTypeRepository
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
