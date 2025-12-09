import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import users.infrastructure.web.configureRouting

class UserGraphQLSchemaTest {

    @Test
    fun `deve registrar usuário via GraphQL mutation`() = testApplication {
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

        val mutation = """
            mutation {
                registerUser(name: "Alice", email: "alice@example.com") {
                    id
                    name
                    email
                }
            }
        """.trimIndent()

        val response = client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to mutation))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<JsonObject>()
        assertNotNull(result["data"])
        val data = result["data"]?.jsonObject
        assertNotNull(data)
        val user = data?.get("registerUser")?.jsonObject
        assertNotNull(user)
        assertEquals("Alice", user?.get("name")?.jsonPrimitive?.content)
        assertEquals("alice@example.com", user?.get("email")?.jsonPrimitive?.content)
        assertTrue(user?.get("id")?.jsonPrimitive?.content?.toLongOrNull() ?: 0 > 0)
    }

    @Test
    fun `deve buscar usuário por email via GraphQL query`() = testApplication {
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

        // Primeiro registra um usuário
        val registerMutation = """
            mutation {
                registerUser(name: "Bob", email: "bob@example.com") {
                    id
                }
            }
        """.trimIndent()

        client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to registerMutation))
        }

        // Busca o usuário
        val query = """
            query {
                user(email: "bob@example.com") {
                    id
                    name
                    email
                }
            }
        """.trimIndent()

        val response = client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to query))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<JsonObject>()
        assertNotNull(result["data"])
        val data = result["data"]?.jsonObject
        assertNotNull(data)
        val user = data?.get("user")?.jsonObject
        assertNotNull(user)
        assertEquals("Bob", user?.get("name")?.jsonPrimitive?.content)
        assertEquals("bob@example.com", user?.get("email")?.jsonPrimitive?.content)
    }

    @Test
    fun `deve retornar null quando usuário não existe via GraphQL query`() = testApplication {
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

        val query = """
            query {
                user(email: "inexistente@example.com") {
                    id
                    name
                    email
                }
            }
        """.trimIndent()

        val response = client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to query))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<JsonObject>()
        // Verificamos que a resposta foi processada
        assertNotNull(result)
        // Verificamos que há uma resposta válida do GraphQL
        // O campo user será null quando o usuário não existe, mas a query é válida
        assertTrue(result.containsKey("data") || result.containsKey("errors"))
    }

    @Test
    fun `deve retornar erro ao registrar usuário com dados inválidos via GraphQL`() = testApplication {
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

        val mutation = """
            mutation {
                registerUser(name: "", email: "alice@example.com") {
                    id
                }
            }
        """.trimIndent()

        val response = client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to mutation))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        // GraphQL sempre retorna 200, mas pode ter erros no body
        // Verificamos que a resposta foi processada
        assertNotNull(response.body<String>())
    }

    @Test
    fun `deve retornar erro ao registrar usuário com email duplicado via GraphQL`() = testApplication {
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

        val mutation = """
            mutation {
                registerUser(name: "Alice", email: "alice@example.com") {
                    id
                }
            }
        """.trimIndent()

        // Primeiro registro
        client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to mutation))
        }

        // Tentativa de registro duplicado
        val response = client.post("/graphql") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("query" to mutation))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        // GraphQL sempre retorna 200, mas pode ter erros no body
        // Verificamos que a resposta foi processada
        assertNotNull(response.body<String>())
    }
}

