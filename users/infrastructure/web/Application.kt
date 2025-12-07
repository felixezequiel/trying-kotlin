package users.infrastructure.web

import com.apurebase.kgraphql.GraphQL
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.application.call
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import users.adapters.`in`.UserController
import users.adapters.`in`.UserGraphQLSchema
import users.adapters.out.UnitOfWorkAdapter
import users.application.dto.ErrorResponse
import users.application.useCases.GetUserByEmailUseCase
import users.application.useCases.UserUseCase
import users.infrastructure.persistence.DatabaseContext

fun Application.configureRouting() {
    // Configuração de serialização JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Configuração de tratamento de erros
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
    val userController = UserController(registerUserUseCase, getUserByEmailUseCase)
    val userGraphQLSchema = UserGraphQLSchema(registerUserUseCase, getUserByEmailUseCase)

    // Configuração do GraphQL
    install(GraphQL) {
        playground = true
        schema {
            userGraphQLSchema.configureSchema(this)
        }
    }

    // Configuração de rotas REST
    routing {
        route("/api/users") {
            post {
                userController.registerUser(call)
            }
            get("/{email}") {
                userController.getUserByEmail(call)
            }
        }
    }
}

