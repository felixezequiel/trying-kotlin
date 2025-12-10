package partners.adapters.outbound

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import partners.application.ports.outbound.IUserGateway
import partners.application.ports.outbound.UserGatewayException

/**
 * Adapter que implementa IUserGateway fazendo chamadas HTTP ao BFF. Conforme ADR-001, comunicação
 * entre serviços sempre via BFF.
 */
class UserGatewayAdapter(private val httpClient: HttpClient, private val bffBaseUrl: String) :
        IUserGateway {

    override suspend fun findOrCreateByEmail(email: String, name: String): Long {
        try {
            // 1. Tenta buscar usuário por email
            val searchResponse =
                    httpClient.get("$bffBaseUrl/api/users") { parameter("email", email) }

            val userId: Long =
                    if (searchResponse.status == HttpStatusCode.OK) {
                        // Usuário existe
                        searchResponse.body<UserResponse>().id
                    } else if (searchResponse.status == HttpStatusCode.NotFound) {
                        // 2. Cria novo usuário
                        val createResponse =
                                httpClient.post("$bffBaseUrl/api/users") {
                                    contentType(ContentType.Application.Json)
                                    setBody(RegisterUserRequest(name = name, email = email))
                                }

                        if (!createResponse.status.isSuccess()) {
                            throw UserGatewayException(
                                    "Falha ao criar usuário: ${createResponse.status}"
                            )
                        }

                        createResponse.body<UserResponse>().id
                    } else {
                        throw UserGatewayException(
                                "Erro ao buscar usuário: ${searchResponse.status}"
                        )
                    }

            // 3. Adiciona role PARTNER ao usuário
            val roleResponse =
                    httpClient.post("$bffBaseUrl/api/users/$userId/roles") {
                        contentType(ContentType.Application.Json)
                        setBody(AddRoleRequest(role = "PARTNER"))
                    }

            if (!roleResponse.status.isSuccess()) {
                throw UserGatewayException(
                        "Falha ao adicionar role PARTNER: ${roleResponse.status}"
                )
            }

            return userId
        } catch (e: UserGatewayException) {
            throw e
        } catch (e: Exception) {
            throw UserGatewayException("Erro na comunicação com serviço de Users: ${e.message}", e)
        }
    }
}

// DTOs para comunicação com o BFF
@Serializable private data class UserResponse(val id: Long, val name: String, val email: String)

@Serializable private data class RegisterUserRequest(val name: String, val email: String)

@Serializable private data class AddRoleRequest(val role: String)
