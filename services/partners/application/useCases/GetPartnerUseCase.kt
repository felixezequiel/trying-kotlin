package partners.application.useCases

import java.util.UUID
import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner

class GetPartnerUseCase(private val partnerRepository: IPartnerRepository) {

    suspend fun execute(partnerId: UUID): Partner? {
        return partnerRepository.getById(partnerId)
    }

    suspend fun executeByUserId(userId: Long): Partner? {
        return partnerRepository.getByUserId(userId)
    }
}
