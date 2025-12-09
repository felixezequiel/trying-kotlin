package users.application.ports.outbound

interface IUnitOfWork {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
    fun userRepository(): IUserRepository
}
