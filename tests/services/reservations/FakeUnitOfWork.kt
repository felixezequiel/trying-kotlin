package services.reservations

import reservations.application.ports.outbound.IReservationRepository
import reservations.application.ports.outbound.IUnitOfWork

/** Fake UnitOfWork para testes unitários. Usa FakeReservationRepository internamente. */
class FakeUnitOfWork(
        override val reservationRepository: IReservationRepository = FakeReservationRepository()
) : IUnitOfWork {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return block()
    }
}
