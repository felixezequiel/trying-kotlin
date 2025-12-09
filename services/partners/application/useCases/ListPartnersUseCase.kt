package partners.application.useCases

import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner
import partners.domain.PartnerStatus

class ListPartnersUseCase(private val partnerRepository: IPartnerRepository) {

    suspend fun execute(): List<Partner> {
        return partnerRepository.getAll()
    }

    suspend fun executeByStatus(status: PartnerStatus): List<Partner> {
        return partnerRepository.getByStatus(status)
    }
}
