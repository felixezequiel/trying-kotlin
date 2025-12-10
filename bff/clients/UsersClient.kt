package bff.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import shared.exceptions.ServiceException

/** Interface para o cliente de Users - permite mocking em testes */
interface IUsersClient {
    suspend fun registerUser(request: RegisterUserRequest): UserResponse
    suspend fun getUserByEmail(email: String): UserResponse?
    suspend fun getAllUsers(): List<UserResponse>
    suspend fun addRoleToUser(userId: Long, role: String): UserResponse
}

/** Cliente HTTP para o serviço de Users */
class UsersClient(private val httpClient: HttpClient, private val baseUrl: String) : IUsersClient {

    override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
        val response =
                httpClient.post("$baseUrl/users") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to register user", response.status.value)
        }

        return response.body()
    }

    override suspend fun getUserByEmail(email: String): UserResponse? {
        val response = httpClient.get("$baseUrl/users") { parameter("email", email) }

        if (response.status == HttpStatusCode.NotFound) {
            return null
        }

        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get user", response.status.value)
        }

        return response.body()
    }

    override suspend fun getAllUsers(): List<UserResponse> {
        val response = httpClient.get("$baseUrl/users/all")

        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to get users", response.status.value)
        }

        return response.body()
    }

    override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
        val response =
                httpClient.post("$baseUrl/users/$userId/roles") {
                    contentType(ContentType.Application.Json)
                    setBody(AddRoleRequest(role = role))
                }

        if (!response.status.isSuccess()) {
            throw ServiceException("Failed to add role to user", response.status.value)
        }

        return response.body()
    }
}

// DTOs específicos para comunicação com o serviço Users
@Serializable data class RegisterUserRequest(val name: String, val email: String)

@Serializable data class AddRoleRequest(val role: String)

@Serializable data class UserResponse(val id: Long, val name: String, val email: String)
