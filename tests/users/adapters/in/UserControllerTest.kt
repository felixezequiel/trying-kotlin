import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import users.application.dto.RegisterUserRequest
import users.application.dto.UserResponse
import users.infrastructure.web.configureRouting

class UserControllerTest {

    @Test
    fun `deve registrar usuário com sucesso`() = testApplication {
        application {
            configureRouting()
        }
        
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val request = RegisterUserRequest(
            name = "Alice",
            email = "alice@example.com"
        )

        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val userResponse = response.body<UserResponse>()
        assertEquals("Alice", userResponse.name)
        assertEquals("alice@example.com", userResponse.email)
        assertTrue(userResponse.id > 0)
    }

    @Test
    fun `deve retornar erro ao registrar usuário com dados inválidos`() = testApplication {
        application {
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val request = RegisterUserRequest(
            name = "",
            email = "alice@example.com"
        )

        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `deve retornar erro ao registrar usuário com email duplicado`() = testApplication {
        application {
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val request = RegisterUserRequest(
            name = "Alice",
            email = "alice@example.com"
        )

        // Primeiro registro
        val firstResponse = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        assertEquals(HttpStatusCode.Created, firstResponse.status)

        // Tentativa de registro duplicado
        val duplicateResponse = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        assertEquals(HttpStatusCode.Conflict, duplicateResponse.status)
    }

    @Test
    fun `deve buscar usuário por email com sucesso`() = testApplication {
        application {
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        // Primeiro registra um usuário
        val registerRequest = RegisterUserRequest(
            name = "Bob",
            email = "bob@example.com"
        )

        val registerResponse = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)

        // Busca o usuário
        val getResponse = client.get("/api/users/bob@example.com")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val userResponse = getResponse.body<UserResponse>()
        assertEquals("Bob", userResponse.name)
        assertEquals("bob@example.com", userResponse.email)
    }

    @Test
    fun `deve retornar 404 ao buscar usuário inexistente`() = testApplication {
        application {
            configureRouting()
        }

        val response = client.get("/api/users/inexistente@example.com")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

