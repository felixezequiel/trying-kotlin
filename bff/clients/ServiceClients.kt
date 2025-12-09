package bff.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import shared.utils.EnvConfig

/**
 * Container para todos os clientes de serviços
 */
class ServiceClients {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    
    val users = UsersClient(
        httpClient = httpClient,
        baseUrl = EnvConfig.get("USERS_SERVICE_URL", "http://localhost:8081")
    )
    
    // Adicione novos clientes aqui conforme novos serviços forem criados
    // val orders = OrdersClient(httpClient, EnvConfig.get("ORDERS_SERVICE_URL", "http://localhost:8082"))
    // val products = ProductsClient(httpClient, EnvConfig.get("PRODUCTS_SERVICE_URL", "http://localhost:8083"))
}
