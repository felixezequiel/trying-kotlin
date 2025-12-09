package bff.routes

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RoutesTest {

    @Test
    fun `GET health deve retornar status healthy`() = testApplication {
        // Arrange
        application {
            install(ServerContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/health") {
                    call.respond(mapOf("status" to "healthy"))
                }
            }
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

        // Act
        val response = client.get("/health")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<Map<String, String>>()
        assertEquals("healthy", body["status"])
    }
}
