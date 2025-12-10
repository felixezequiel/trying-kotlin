package bff.routes

import bff.clients.IUsersClient
import bff.clients.RegisterUserRequest
import bff.clients.UserResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import shared.exceptions.NotFoundException
import shared.exceptions.ServiceException

class UsersRoutesTest {

    private fun ApplicationTestBuilder.configureTestApplication(usersClient: IUsersClient) {
        application {
            install(ServerContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(StatusPages) {
                exception<NotFoundException> { call, cause ->
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
                }
                exception<ServiceException> { call, cause ->
                    call.respond(
                        HttpStatusCode.fromValue(cause.statusCode),
                        mapOf("error" to cause.message)
                    )
                }
            }
            routing {
                usersRoutes(usersClient)
            }
        }
    }

    private fun ApplicationTestBuilder.createJsonClient() = createClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    @Test
    fun `POST api users deve registrar usuário com sucesso`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                return UserResponse(id = 123L, name = request.name, email = request.email)
            }
            override suspend fun getUserByEmail(email: String): UserResponse? = null
            override suspend fun getAllUsers(): List<UserResponse> = emptyList()
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(RegisterUserRequest(name = "Alice", email = "alice@example.com"))
        }

        // Assert
        assertEquals(HttpStatusCode.Created, response.status)
        val userResponse = response.body<UserResponse>()
        assertEquals(123L, userResponse.id)
        assertEquals("Alice", userResponse.name)
        assertEquals("alice@example.com", userResponse.email)
    }

    @Test
    fun `POST api users deve propagar erro do serviço`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                throw ServiceException("Email already exists", 409)
            }
            override suspend fun getUserByEmail(email: String): UserResponse? = null
            override suspend fun getAllUsers(): List<UserResponse> = emptyList()
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(RegisterUserRequest(name = "Alice", email = "alice@example.com"))
        }

        // Assert
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `GET api users deve buscar usuário por email`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                throw NotImplementedError()
            }
            override suspend fun getUserByEmail(email: String): UserResponse? {
                return if (email == "alice@example.com") {
                    UserResponse(id = 123L, name = "Alice", email = email)
                } else null
            }
            override suspend fun getAllUsers(): List<UserResponse> = emptyList()
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.get("/api/users?email=alice@example.com")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val userResponse = response.body<UserResponse>()
        assertEquals("Alice", userResponse.name)
    }

    @Test
    fun `GET api users deve retornar 400 sem parâmetro email`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                throw NotImplementedError()
            }
            override suspend fun getUserByEmail(email: String): UserResponse? = null
            override suspend fun getAllUsers(): List<UserResponse> = emptyList()
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.get("/api/users")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET api users deve retornar 404 quando usuário não encontrado`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                throw NotImplementedError()
            }
            override suspend fun getUserByEmail(email: String): UserResponse? = null
            override suspend fun getAllUsers(): List<UserResponse> = emptyList()
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.get("/api/users?email=inexistente@example.com")

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET api users all deve listar todos os usuários`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                throw NotImplementedError()
            }
            override suspend fun getUserByEmail(email: String): UserResponse? = null
            override suspend fun getAllUsers(): List<UserResponse> {
                return listOf(
                    UserResponse(id = 1L, name = "Alice", email = "alice@example.com"),
                    UserResponse(id = 2L, name = "Bob", email = "bob@example.com")
                )
            }
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.get("/api/users/all")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val users = response.body<List<UserResponse>>()
        assertEquals(2, users.size)
    }

    @Test
    fun `GET api users all deve retornar lista vazia`() = testApplication {
        // Arrange
        val mockUsersClient = object : IUsersClient {
            override suspend fun registerUser(request: RegisterUserRequest): UserResponse {
                throw NotImplementedError()
            }
            override suspend fun getUserByEmail(email: String): UserResponse? = null
            override suspend fun getAllUsers(): List<UserResponse> = emptyList()
            override suspend fun addRoleToUser(userId: Long, role: String): UserResponse {
                throw NotImplementedError()
            }
        }
        configureTestApplication(mockUsersClient)
        val client = createJsonClient()

        // Act
        val response = client.get("/api/users/all")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val users = response.body<List<UserResponse>>()
        assertTrue(users.isEmpty())
    }
}
