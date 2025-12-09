package partners.application.ports.outbound

interface IUnitOfWork {
    val partnerRepository: IPartnerRepository
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
