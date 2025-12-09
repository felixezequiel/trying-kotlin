package partners.application.useCases

import java.time.Instant
import java.util.UUID
import partners.application.ports.outbound.IUnitOfWork
import partners.domain.Partner
import partners.domain.PartnerStatus

class SuspendPartnerUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(partnerId: UUID): Partner {
        return unitOfWork.runInTransaction {
            val partner =
                    unitOfWork.partnerRepository.getById(partnerId)
                            ?: throw IllegalArgumentException("Parceiro não encontrado")

            if (partner.status != PartnerStatus.APPROVED) {
                throw IllegalStateException("Apenas parceiros aprovados podem ser suspensos")
            }

            val suspendedPartner =
                    partner.copy(status = PartnerStatus.SUSPENDED, updatedAt = Instant.now())

            unitOfWork.partnerRepository.update(suspendedPartner)
            suspendedPartner
        }
    }
}
