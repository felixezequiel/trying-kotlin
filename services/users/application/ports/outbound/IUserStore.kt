package users.application.ports.outbound

/**
 * Interface que define o contrato para um Store de users. Qualquer implementação (InMemory,
 * Postgres, etc.) deve fornecer um repositório e um gerenciador de transações.
 */
interface IUserStore {
    val repository: IUserRepository
    val transactionManager: ITransactionManager
}
