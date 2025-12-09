package users.infrastructure.web

import com.apurebase.kgraphql.GraphQL
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.application.call
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import users.adapters.inbound.UserController
import users.adapters.inbound.UserGraphQLSchema
import users.adapters.outbound.UnitOfWorkAdapter
import users.application.dto.ErrorResponse
import users.application.useCases.GetUserByEmailUseCase
import users.application.useCases.GetAllUsersUseCase
import users.application.useCases.UserUseCase
import users.infrastructure.persistence.DatabaseContext

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Erro interno: ${cause.message}")
            )
        }
    }

    // Inicialização das dependências
    val dbContext = DatabaseContext()
    val unitOfWork = UnitOfWorkAdapter(dbContext)
    val registerUserUseCase = UserUseCase(unitOfWork)
    val getUserByEmailUseCase = GetUserByEmailUseCase(unitOfWork)
    val getAllUsersUseCase = GetAllUsersUseCase(unitOfWork)
    val userController = UserController(registerUserUseCase, getUserByEmailUseCase, getAllUsersUseCase)
    val userGraphQLSchema = UserGraphQLSchema(registerUserUseCase, getUserByEmailUseCase, getAllUsersUseCase)

    // Configuração do GraphQL
    install(GraphQL) {
        playground = true
        schema {
            userGraphQLSchema.configureSchema(this)
        }
    }

    // Configuração de rotas REST
    routing {
        route("/users") {
            post {
                userController.registerUser(call)
            }
            get {
                val email = call.request.queryParameters["email"]
                if (email != null) {
                    userController.getUserByEmail(call)
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email parameter required"))
                }
            }
            get("/all") {
                userController.getAllUsers(call)
            }
        }
    }
}
