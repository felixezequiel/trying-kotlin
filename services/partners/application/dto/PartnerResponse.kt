package partners.application.dto

import kotlinx.serialization.Serializable
import partners.domain.DocumentType
import partners.domain.Partner
import partners.domain.PartnerStatus

@Serializable
data class PartnerResponse(
        val id: String,
        val userId: Long,
        val companyName: String,
        val tradeName: String?,
        val document: String,
        val documentType: DocumentType,
        val email: String,
        val phone: String,
        val status: PartnerStatus,
        val rejectionReason: String?,
        val createdAt: String,
        val approvedAt: String?
) {
    companion object {
        fun fromDomain(partner: Partner): PartnerResponse {
            return PartnerResponse(
                    id = partner.id.toString(),
                    userId = partner.userId,
                    companyName = partner.companyName,
                    tradeName = partner.tradeName,
                    document = partner.document,
                    documentType = partner.documentType,
                    email = partner.email,
                    phone = partner.phone,
                    status = partner.status,
                    rejectionReason = partner.rejectionReason,
                    createdAt = partner.createdAt.toString(),
                    approvedAt = partner.approvedAt?.toString()
            )
        }
    }
}
