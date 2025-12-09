package partners.application.useCases

import java.time.Instant
import java.util.UUID
import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner
import partners.domain.PartnerStatus

class RejectPartnerUseCase(private val partnerRepository: IPartnerRepository) {

    suspend fun execute(partnerId: UUID, reason: String): Partner {
        val partner =
                partnerRepository.getById(partnerId)
                        ?: throw IllegalArgumentException("Parceiro não encontrado")

        if (partner.status != PartnerStatus.PENDING) {
            throw IllegalStateException("Apenas parceiros pendentes podem ser rejeitados")
        }

        // RN-P05: Rejeição requer motivo
        if (reason.isBlank()) {
            throw IllegalArgumentException("Motivo da rejeição é obrigatório")
        }

        val rejectedPartner =
                partner.copy(
                        status = PartnerStatus.REJECTED,
                        rejectionReason = reason,
                        updatedAt = Instant.now()
                )

        partnerRepository.update(rejectedPartner)
        return rejectedPartner
    }
}
