package events.application.ports.outbound

interface IUnitOfWork {
    val eventRepository: IEventRepository
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
