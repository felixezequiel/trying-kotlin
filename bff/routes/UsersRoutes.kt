package bff.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import bff.clients.UsersClient
import bff.clients.RegisterUserRequest
import shared.exceptions.NotFoundException

/**
 * Rotas REST para o domínio de Users
 * Encaminha requisições para o serviço Users
 */
fun Route.usersRoutes(usersClient: UsersClient) {
    route("/api/users") {
        
        // POST /api/users - Registrar novo usuário
        post {
            val request = call.receive<RegisterUserRequest>()
            val user = usersClient.registerUser(request)
            call.respond(HttpStatusCode.Created, user)
        }
        
        // GET /api/users?email={email} - Buscar usuário por email
        get {
            val email = call.request.queryParameters["email"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email parameter is required"))
            
            val user = usersClient.getUserByEmail(email)
                ?: throw NotFoundException("User not found with email: $email")
            
            call.respond(user)
        }
        
        // GET /api/users/all - Listar todos os usuários
        get("/all") {
            val users = usersClient.getAllUsers()
            call.respond(users)
        }
    }
}
