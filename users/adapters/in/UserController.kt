package users.adapters.`in`

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import users.application.dto.ErrorResponse
import users.application.dto.RegisterUserRequest
import users.application.dto.UserResponse
import users.application.useCases.GetUserByEmailUseCase
import users.application.useCases.UserUseCase

class UserController(
    private val registerUserUseCase: UserUseCase,
    private val getUserByEmailUseCase: GetUserByEmailUseCase
) {
    suspend fun registerUser(call: ApplicationCall) {
        try {
            val request = call.receive<RegisterUserRequest>()
            registerUserUseCase.registerUser(request.name, request.email)
            
            // Busca o usuário criado para retornar
            val user = getUserByEmailUseCase.execute(request.email)
            if (user != null) {
                call.respond(HttpStatusCode.Created, UserResponse.fromDomain(user))
            } else {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Usuário criado mas não encontrado"))
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.Conflict, ErrorResponse(e.message ?: "Conflito ao criar usuário"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Erro interno do servidor: ${e.message}"))
        }
    }

    suspend fun getUserByEmail(call: ApplicationCall) {
        try {
            val email = call.parameters["email"]
            if (email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email é obrigatório"))
                return
            }

            val user = getUserByEmailUseCase.execute(email)
            if (user != null) {
                call.respond(HttpStatusCode.OK, UserResponse.fromDomain(user))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Erro interno do servidor: ${e.message}"))
        }
    }
}

