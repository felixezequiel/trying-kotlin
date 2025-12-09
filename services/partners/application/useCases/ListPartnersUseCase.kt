package partners.application.useCases

import partners.application.ports.outbound.IUnitOfWork
import partners.domain.Partner
import partners.domain.PartnerStatus

class ListPartnersUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(): List<Partner> {
        return unitOfWork.partnerRepository.getAll()
    }

    suspend fun executeByStatus(status: PartnerStatus): List<Partner> {
        return unitOfWork.partnerRepository.getByStatus(status)
    }
}
