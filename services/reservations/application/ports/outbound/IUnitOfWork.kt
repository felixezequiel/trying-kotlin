package reservations.application.ports.outbound

interface IUnitOfWork {
    val reservationRepository: IReservationRepository
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
