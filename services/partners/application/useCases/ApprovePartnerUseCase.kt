package partners.application.useCases

import java.time.Instant
import java.util.UUID
import partners.application.ports.outbound.IUnitOfWork
import partners.domain.Partner
import partners.domain.PartnerStatus

class ApprovePartnerUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(partnerId: UUID): Partner {
        return unitOfWork.runInTransaction {
            val partner =
                    unitOfWork.partnerRepository.getById(partnerId)
                            ?: throw IllegalArgumentException("Parceiro não encontrado")

            if (partner.status != PartnerStatus.PENDING) {
                throw IllegalStateException("Apenas parceiros pendentes podem ser aprovados")
            }

            val now = Instant.now()
            val approvedPartner =
                    partner.copy(status = PartnerStatus.APPROVED, approvedAt = now, updatedAt = now)

            unitOfWork.partnerRepository.update(approvedPartner)
            approvedPartner
        }
    }
}
