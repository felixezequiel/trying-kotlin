package partners.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePartnerRequest(
        val companyName: String? = null,
        val tradeName: String? = null,
        val email: String? = null,
        val phone: String? = null
)
