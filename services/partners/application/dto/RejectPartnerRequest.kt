package partners.application.dto

import kotlinx.serialization.Serializable

@Serializable data class RejectPartnerRequest(val reason: String)
