package bff.clients

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import shared.exceptions.ServiceException

class UsersClientTest {

    private lateinit var usersClient: UsersClient
    private val baseUrl = "http://localhost:8081"

    private fun createMockClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
        return HttpClient(MockEngine { request ->
            handler(request)
        }) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    @Test
    fun `deve registrar usuário com sucesso`() = runTest {
        // Arrange
        val mockClient = createMockClient { request: HttpRequestData ->
            assertEquals("$baseUrl/users", request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            
            respond(
                content = """{"id": 123, "name": "Alice", "email": "alice@example.com"}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)
        val request = RegisterUserRequest(name = "Alice", email = "alice@example.com")

        // Act
        val result = usersClient.registerUser(request)

        // Assert
        assertEquals(123L, result.id)
        assertEquals("Alice", result.name)
        assertEquals("alice@example.com", result.email)
    }

    @Test
    fun `deve lançar exceção ao falhar registro de usuário`() = runTest {
        // Arrange
        val mockClient = createMockClient { _: HttpRequestData ->
            respond(
                content = """{"error": "Email already exists"}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)
        val request = RegisterUserRequest(name = "Alice", email = "alice@example.com")

        // Act & Assert
        val exception = assertThrows(ServiceException::class.java) {
            kotlinx.coroutines.runBlocking {
                usersClient.registerUser(request)
            }
        }
        assertEquals(409, exception.statusCode)
    }

    @Test
    fun `deve buscar usuário por email com sucesso`() = runTest {
        // Arrange
        val mockClient = createMockClient { request: HttpRequestData ->
            assertEquals("$baseUrl/users?email=alice%40example.com", request.url.toString())
            assertEquals(HttpMethod.Get, request.method)
            
            respond(
                content = """{"id": 123, "name": "Alice", "email": "alice@example.com"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)

        // Act
        val result = usersClient.getUserByEmail("alice@example.com")

        // Assert
        assertNotNull(result)
        assertEquals(123L, result?.id)
        assertEquals("Alice", result?.name)
        assertEquals("alice@example.com", result?.email)
    }

    @Test
    fun `deve retornar null quando usuário não encontrado`() = runTest {
        // Arrange
        val mockClient = createMockClient { _: HttpRequestData ->
            respond(
                content = """{"error": "Not found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)

        // Act
        val result = usersClient.getUserByEmail("inexistente@example.com")

        // Assert
        assertNull(result)
    }

    @Test
    fun `deve lançar exceção ao falhar busca por email`() = runTest {
        // Arrange
        val mockClient = createMockClient { _: HttpRequestData ->
            respond(
                content = """{"error": "Internal server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)

        // Act & Assert
        val exception = assertThrows(ServiceException::class.java) {
            kotlinx.coroutines.runBlocking {
                usersClient.getUserByEmail("alice@example.com")
            }
        }
        assertEquals(500, exception.statusCode)
    }

    @Test
    fun `deve listar todos os usuários com sucesso`() = runTest {
        // Arrange
        val mockClient = createMockClient { request: HttpRequestData ->
            assertEquals("$baseUrl/users/all", request.url.toString())
            assertEquals(HttpMethod.Get, request.method)
            
            respond(
                content = """[
                    {"id": 1, "name": "Alice", "email": "alice@example.com"},
                    {"id": 2, "name": "Bob", "email": "bob@example.com"}
                ]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)

        // Act
        val result = usersClient.getAllUsers()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Alice", result[0].name)
        assertEquals("Bob", result[1].name)
    }

    @Test
    fun `deve retornar lista vazia quando não há usuários`() = runTest {
        // Arrange
        val mockClient = createMockClient { _: HttpRequestData ->
            respond(
                content = """[]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)

        // Act
        val result = usersClient.getAllUsers()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deve lançar exceção ao falhar listagem de usuários`() = runTest {
        // Arrange
        val mockClient = createMockClient { _: HttpRequestData ->
            respond(
                content = """{"error": "Service unavailable"}""",
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        usersClient = UsersClient(mockClient, baseUrl)

        // Act & Assert
        val exception = assertThrows(ServiceException::class.java) {
            kotlinx.coroutines.runBlocking {
                usersClient.getAllUsers()
            }
        }
        assertEquals(503, exception.statusCode)
    }
}
