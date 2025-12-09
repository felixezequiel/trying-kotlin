package partners.domain

import java.time.Instant
import java.util.UUID
import partners.domain.valueObjects.CompanyName
import partners.domain.valueObjects.Document
import partners.domain.valueObjects.PartnerEmail
import partners.domain.valueObjects.Phone

data class Partner(
        val id: UUID = UUID.randomUUID(),
        val userId: Long,
        val companyName: CompanyName,
        val tradeName: String?,
        val document: Document,
        val documentType: DocumentType,
        val email: PartnerEmail,
        val phone: Phone,
        val status: PartnerStatus = PartnerStatus.PENDING,
        val rejectionReason: String? = null,
        val createdAt: Instant = Instant.now(),
        val approvedAt: Instant? = null,
        val updatedAt: Instant = Instant.now()
)
