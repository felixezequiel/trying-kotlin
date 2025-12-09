package tickets.application.ports.outbound

/**
 * Interface que define o contrato para um Store de tickets. Qualquer implementação (InMemory,
 * Postgres, etc.) deve fornecer um repositório e um gerenciador de transações.
 */
interface ITicketStore {
    val repository: ITicketTypeRepository
    val transactionManager: ITransactionManager
}
