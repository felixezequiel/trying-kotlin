package partners.application.useCases

import java.time.Instant
import java.util.UUID
import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner
import partners.domain.PartnerStatus

class SuspendPartnerUseCase(private val partnerRepository: IPartnerRepository) {

    suspend fun execute(partnerId: UUID): Partner {
        val partner =
                partnerRepository.getById(partnerId)
                        ?: throw IllegalArgumentException("Parceiro não encontrado")

        if (partner.status != PartnerStatus.APPROVED) {
            throw IllegalStateException("Apenas parceiros aprovados podem ser suspensos")
        }

        val suspendedPartner =
                partner.copy(status = PartnerStatus.SUSPENDED, updatedAt = Instant.now())

        partnerRepository.update(suspendedPartner)
        return suspendedPartner
    }
}
