package partners.application.useCases

import java.util.UUID
import partners.application.dto.CreatePartnerRequest
import partners.application.ports.outbound.IUnitOfWork
import partners.domain.Partner
import partners.domain.valueObjects.CompanyName
import partners.domain.valueObjects.Document
import partners.domain.valueObjects.PartnerEmail
import partners.domain.valueObjects.Phone

class CreatePartnerUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(userId: Long, request: CreatePartnerRequest): UUID {
        return unitOfWork.runInTransaction {
            // Validação encapsulada nos Value Objects
            val companyName = CompanyName.of(request.companyName)
            val document = Document.of(request.document, request.documentType)
            val email = PartnerEmail.of(request.email)
            val phone = Phone.of(request.phone)

            // RN-P02: Um User só pode ter um Partner
            val existingPartner = unitOfWork.partnerRepository.getByUserId(userId)
            if (existingPartner != null) {
                throw IllegalStateException("Usuário já possui um parceiro cadastrado")
            }

            // RN-P03: Documento (CPF/CNPJ) deve ser único
            val existingDocument = unitOfWork.partnerRepository.getByDocument(document)
            if (existingDocument != null) {
                throw IllegalStateException("Documento já cadastrado por outro parceiro")
            }

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
