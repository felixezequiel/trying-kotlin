package bff.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import shared.utils.EnvConfig

/** Container para todos os clientes de serviços */
class ServiceClients {
    private val httpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(
                            Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                    )
                }
            }

    val users: IUsersClient =
            UsersClient(
                    httpClient = httpClient,
                    baseUrl = EnvConfig.get("USERS_SERVICE_URL", "http://localhost:8081")
            )

    val events: IEventsClient =
            EventsClient(
                    httpClient = httpClient,
                    baseUrl = EnvConfig.get("EVENTS_SERVICE_URL", "http://localhost:8082")
            )

    val partners: IPartnersClient =
            PartnersClient(
                    httpClient = httpClient,
                    baseUrl = EnvConfig.get("PARTNERS_SERVICE_URL", "http://localhost:8083")
            )

    val tickets: ITicketsClient =
            TicketsClient(
                    httpClient = httpClient,
                    baseUrl = EnvConfig.get("TICKETS_SERVICE_URL", "http://localhost:8084")
            )

    val orders: IOrdersClient =
            OrdersClient(
                    httpClient = httpClient,
                    baseUrl = EnvConfig.get("ORDERS_SERVICE_URL", "http://localhost:8085")
            )

    val reservations: IReservationsClient =
            ReservationsClient(
                    httpClient = httpClient,
                    baseUrl = EnvConfig.get("RESERVATIONS_SERVICE_URL", "http://localhost:8086")
            )
}
