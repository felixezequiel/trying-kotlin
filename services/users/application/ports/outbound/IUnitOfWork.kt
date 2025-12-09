package users.application.ports.outbound

interface IUnitOfWork {
    val userRepository: IUserRepository
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
