package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Document types matching service enum */
@Serializable
enum class DocumentType {
    CPF,
    CNPJ
}

/** Interface para o cliente de Partners */
interface IPartnersClient {
    suspend fun createPartner(request: CreatePartnerRequest): PartnerResponse
    suspend fun getPartnerById(id: String): PartnerResponse?
    suspend fun listPartners(status: String?): List<PartnerResponse>
    suspend fun updatePartner(
            userId: Long,
            id: String,
            request: UpdatePartnerRequest
    ): PartnerResponse
    suspend fun approvePartner(id: String): PartnerResponse
    suspend fun rejectPartner(id: String): PartnerResponse
    suspend fun suspendPartner(id: String): PartnerResponse
    suspend fun reactivatePartner(id: String): PartnerResponse
}

/** Cliente HTTP para o serviço de Partners */
class PartnersClient(private val httpClient: HttpClient, private val baseUrl: String) :
        IPartnersClient {

    override suspend fun createPartner(request: CreatePartnerRequest): PartnerResponse {
        val response =
                httpClient.post("$baseUrl/partners") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to create partner", response.status.value)
        }
        return response.body()
    }

    override suspend fun getPartnerById(id: String): PartnerResponse? {
        val response = httpClient.get("$baseUrl/partners/$id")
        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get partner", response.status.value)
        }
        return response.body()
    }

    override suspend fun listPartners(status: String?): List<PartnerResponse> {
        val response =
                httpClient.get("$baseUrl/partners") { status?.let { parameter("status", it) } }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to list partners", response.status.value)
        }
        return response.body()
    }

    override suspend fun updatePartner(
            userId: Long,
            id: String,
            request: UpdatePartnerRequest
    ): PartnerResponse {
        val response =
                httpClient.put("$baseUrl/partners/$id") {
                    contentType(ContentType.Application.Json)
                    header("X-User-Id", userId.toString())
                    setBody(request)
                }
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to update partner", response.status.value)
        }
        return response.body()
    }

    override suspend fun approvePartner(id: String): PartnerResponse {
        val response = httpClient.post("$baseUrl/partners/$id/approve")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to approve partner", response.status.value)
        }
        return response.body()
    }

    override suspend fun rejectPartner(id: String): PartnerResponse {
        val response = httpClient.post("$baseUrl/partners/$id/reject")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to reject partner", response.status.value)
        }
        return response.body()
    }

    override suspend fun suspendPartner(id: String): PartnerResponse {
        val response = httpClient.post("$baseUrl/partners/$id/suspend")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to suspend partner", response.status.value)
        }
        return response.body()
    }

    override suspend fun reactivatePartner(id: String): PartnerResponse {
        val response = httpClient.post("$baseUrl/partners/$id/reactivate")
        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to reactivate partner", response.status.value)
        }
        return response.body()
    }
}

// DTOs
@Serializable
data class CreatePartnerRequest(
        val companyName: String,
        val tradeName: String? = null,
        val document: String,
        val documentType: DocumentType,
        val email: String,
        val phone: String
)

@Serializable
data class UpdatePartnerRequest(
        val companyName: String? = null,
        val tradeName: String? = null,
        val email: String? = null,
        val phone: String? = null
)

@Serializable
data class PartnerResponse(
        val id: String,
        val userId: Long,
        val companyName: String,
        val tradeName: String? = null,
        val document: String,
        val documentType: DocumentType,
        val email: String,
        val phone: String,
        val status: String,
        val rejectionReason: String? = null,
        val createdAt: String,
        val approvedAt: String? = null
)
