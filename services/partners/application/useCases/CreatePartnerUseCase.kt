package partners.application.useCases

import java.util.UUID
import partners.application.dto.CreatePartnerRequest
import partners.application.ports.outbound.IUnitOfWork
import partners.application.ports.outbound.IUserGateway
import partners.domain.Partner
import partners.domain.valueObjects.CompanyName
import partners.domain.valueObjects.Document
import partners.domain.valueObjects.PartnerEmail
import partners.domain.valueObjects.Phone

/**
 * Use Case para criação de Partner com vinculação inteligente de usuário.
 *
 * Fluxo (ADR-011):
 * 1. Valida dados do Partner
 * 2. Busca ou cria usuário pelo email via IUserGateway
 * 3. Adiciona role PARTNER ao usuário
 * 4. Cria Partner vinculado ao userId
 */
class CreatePartnerUseCase(
        private val unitOfWork: IUnitOfWork,
        private val userGateway: IUserGateway
) {

    suspend fun execute(request: CreatePartnerRequest): UUID {
        // Validação encapsulada nos Value Objects
        val companyName = CompanyName.of(request.companyName)
        val document = Document.of(request.document, request.documentType)
        val email = PartnerEmail.of(request.email)
        val phone = Phone.of(request.phone)

        // RN-P03: Documento (CPF/CNPJ) deve ser único
        val existingDocument = unitOfWork.partnerRepository.getByDocument(document)
        if (existingDocument != null) {
            throw IllegalStateException("Documento já cadastrado por outro parceiro")
        }

        // Vinculação inteligente: busca ou cria usuário pelo email
        // Também adiciona role PARTNER ao usuário
        val userId =
                userGateway.findOrCreateByEmail(email = request.email, name = request.companyName)

        // RN-P02: Um User só pode ter um Partner
        val existingPartner = unitOfWork.partnerRepository.getByUserId(userId)
        if (existingPartner != null) {
            throw IllegalStateException("Usuário já possui um parceiro cadastrado")
        }

        return unitOfWork.runInTransaction {
            val partner =
                    Partner(
                            userId = userId,
                            companyName = companyName,
                            tradeName = request.tradeName,
                            document = document,
                            documentType = request.documentType,
                            email = email,
                            phone = phone
                    )

            unitOfWork.partnerRepository.add(partner)
        }
    }
}
