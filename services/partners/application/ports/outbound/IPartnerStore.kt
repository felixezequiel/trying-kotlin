package partners.application.ports.outbound

/**
 * Interface que define o contrato para um Store de partners. Qualquer implementação (InMemory,
 * Postgres, etc.) deve fornecer um repositório e um gerenciador de transações.
 */
interface IPartnerStore {
    val repository: IPartnerRepository
    val transactionManager: ITransactionManager
}
