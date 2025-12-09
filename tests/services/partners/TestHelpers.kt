package services.partners

import java.util.UUID
import partners.domain.DocumentType
import partners.domain.Partner
import partners.domain.PartnerStatus
import partners.domain.valueObjects.CompanyName
import partners.domain.valueObjects.Document
import partners.domain.valueObjects.PartnerEmail
import partners.domain.valueObjects.Phone

object TestHelpers {
    fun createTestPartner(
            id: UUID = UUID.randomUUID(),
            userId: Long = 1L,
            companyName: String = "Test Company",
            tradeName: String? = "Test Trade",
            document: String = "12345678901234", // CNPJ válido
            documentType: DocumentType = DocumentType.CNPJ,
            email: String = "test@company.com",
            phone: String = "11999999999",
            status: PartnerStatus = PartnerStatus.PENDING
    ): Partner {
        return Partner(
                id = id,
                userId = userId,
                companyName = CompanyName.of(companyName),
                tradeName = tradeName,
                document = Document.of(document, documentType),
                documentType = documentType,
                email = PartnerEmail.of(email),
                phone = Phone.of(phone),
                status = status
        )
    }
}
