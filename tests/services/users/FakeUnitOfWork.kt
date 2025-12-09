package services.users

import users.application.ports.outbound.IUnitOfWork
import users.application.ports.outbound.IUserRepository

/** Fake UnitOfWork para testes unitários. Usa FakeUserRepository internamente. */
class FakeUnitOfWork(override val userRepository: IUserRepository = FakeUserRepository()) :
        IUnitOfWork {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return block()
    }
}
