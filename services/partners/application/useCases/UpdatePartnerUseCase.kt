package partners.application.useCases

import java.time.Instant
import java.util.UUID
import partners.application.dto.UpdatePartnerRequest
import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner
import partners.domain.valueObjects.CompanyName
import partners.domain.valueObjects.PartnerEmail
import partners.domain.valueObjects.Phone

class UpdatePartnerUseCase(private val partnerRepository: IPartnerRepository) {

    suspend fun execute(partnerId: UUID, userId: Long, request: UpdatePartnerRequest): Partner {
        val partner =
                partnerRepository.getById(partnerId)
                        ?: throw IllegalArgumentException("Parceiro não encontrado")

        // Apenas o próprio parceiro pode atualizar seus dados
        if (partner.userId != userId) {
            throw IllegalStateException("Você não tem permissão para atualizar este parceiro")
        }

        val updatedPartner =
                partner.copy(
                        companyName = request.companyName?.let { CompanyName.of(it) }
                                        ?: partner.companyName,
                        tradeName = request.tradeName ?: partner.tradeName,
                        email = request.email?.let { PartnerEmail.of(it) } ?: partner.email,
                        phone = request.phone?.let { Phone.of(it) } ?: partner.phone,
                        updatedAt = Instant.now()
                )

        partnerRepository.update(updatedPartner)
        return updatedPartner
    }
}
