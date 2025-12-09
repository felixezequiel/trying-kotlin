package orders.application.ports.outbound

/**
 * Interface que define o contrato para um Store de orders. Qualquer implementação (InMemory,
 * Postgres, etc.) deve fornecer os repositórios e um gerenciador de transações.
 */
interface IOrderStore {
    val orderRepository: IOrderRepository
    val issuedTicketRepository: IIssuedTicketRepository
    val transactionManager: ITransactionManager
}
