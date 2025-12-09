package orders.application.ports.outbound

interface IUnitOfWork {
    val orderRepository: IOrderRepository
    val issuedTicketRepository: IIssuedTicketRepository

    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
