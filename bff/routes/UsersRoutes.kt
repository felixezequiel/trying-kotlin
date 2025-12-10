package bff.routes

import bff.clients.AddRoleRequest
import bff.clients.IUsersClient
import bff.clients.RegisterUserRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Rotas REST para o domínio de Users Encaminha requisições para o serviço Users */
fun Route.usersRoutes(usersClient: IUsersClient) {
    route("/api/users") {

        // POST /api/users - Registrar novo usuário
        post {
            val request = call.receive<RegisterUserRequest>()
            val user = usersClient.registerUser(request)
            call.respond(HttpStatusCode.Created, user)
        }

        // GET /api/users?email={email} - Buscar usuário por email
        // Importante: quando o usuário não existe, responder 404 em vez de lançar exceção,
        // pois o UserGatewayAdapter (serviço de partners) depende explicitamente desse status
        // para decidir entre buscar ou criar o usuário.
        get {
            val email =
                    call.request.queryParameters["email"]
                            ?: return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Email parameter is required")
                            )

            val user = usersClient.getUserByEmail(email)

            if (user == null) {
                return@get call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found with email: $email")
                )
            }

            call.respond(user)
        }

        // GET /api/users/all - Listar todos os usuários
        get("/all") {
            val users = usersClient.getAllUsers()
            call.respond(users)
        }

        // POST /api/users/{id}/roles - Adicionar role ao usuário
        post("/{id}/roles") {
            val userId =
                    call.parameters["id"]?.toLongOrNull()
                            ?: return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Invalid user ID")
                            )

            val request = call.receive<AddRoleRequest>()
            val user = usersClient.addRoleToUser(userId, request.role)
            call.respond(user)
        }
    }
}
