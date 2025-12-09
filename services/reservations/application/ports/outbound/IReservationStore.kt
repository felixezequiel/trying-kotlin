package reservations.application.ports.outbound

/**
 * Interface que define o contrato para um Store de reservations. Qualquer implementação (InMemory,
 * Postgres, etc.) deve fornecer um repositório e um gerenciador de transações.
 */
interface IReservationStore {
    val repository: IReservationRepository
    val transactionManager: ITransactionManager
}
