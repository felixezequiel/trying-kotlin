package partners.application.useCases

import java.util.UUID
import partners.application.ports.outbound.IUnitOfWork
import partners.domain.Partner

class GetPartnerUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(partnerId: UUID): Partner? {
        return unitOfWork.partnerRepository.getById(partnerId)
    }

    suspend fun executeByUserId(userId: Long): Partner? {
        return unitOfWork.partnerRepository.getByUserId(userId)
    }
}
