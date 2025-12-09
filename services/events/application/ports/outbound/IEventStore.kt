package events.application.ports.outbound

/**
 * Interface que define o contrato para um Store de eventos. Qualquer implementação (InMemory,
 * Postgres, etc.) deve fornecer um repositório e um gerenciador de transações.
 */
interface IEventStore {
    val repository: IEventRepository
    val transactionManager: ITransactionManager
}
