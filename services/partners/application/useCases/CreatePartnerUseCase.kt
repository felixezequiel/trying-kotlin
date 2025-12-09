package partners.application.useCases

import java.util.UUID
import partners.application.dto.CreatePartnerRequest
import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner

class CreatePartnerUseCase(private val partnerRepository: IPartnerRepository) {

    suspend fun execute(userId: Long, request: CreatePartnerRequest): UUID {
        // RN-P02: Um User só pode ter um Partner
        val existingPartner = partnerRepository.getByUserId(userId)
        if (existingPartner != null) {
            throw IllegalStateException("Usuário já possui um parceiro cadastrado")
        }

        // RN-P03: Documento (CPF/CNPJ) deve ser único
        val existingDocument = partnerRepository.getByDocument(request.document)
        if (existingDocument != null) {
            throw IllegalStateException("Documento já cadastrado por outro parceiro")
        }

        val partner =
                Partner(
                        userId = userId,
                        companyName = request.companyName,
                        tradeName = request.tradeName,
                        document = request.document,
                        documentType = request.documentType,
                        email = request.email,
                        phone = request.phone
                )

        return partnerRepository.add(partner)
    }
}
