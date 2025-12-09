package partners.application.useCases

import java.time.Instant
import java.util.UUID
import partners.application.ports.outbound.IUnitOfWork
import partners.domain.Partner
import partners.domain.PartnerStatus

class ReactivatePartnerUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(partnerId: UUID): Partner {
        return unitOfWork.runInTransaction {
            val partner =
                    unitOfWork.partnerRepository.getById(partnerId)
                            ?: throw IllegalArgumentException("Parceiro não encontrado")

            if (partner.status != PartnerStatus.SUSPENDED) {
                throw IllegalStateException("Apenas parceiros suspensos podem ser reativados")
            }

            val reactivatedPartner =
                    partner.copy(status = PartnerStatus.APPROVED, updatedAt = Instant.now())

            unitOfWork.partnerRepository.update(reactivatedPartner)
            reactivatedPartner
        }
    }
}
