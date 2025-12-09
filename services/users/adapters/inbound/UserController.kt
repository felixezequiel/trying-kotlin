package users.adapters.inbound

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import users.application.dto.AddRoleRequest
import users.application.dto.ErrorResponse
import users.application.dto.RegisterUserRequest
import users.application.dto.UserResponse
import users.application.useCases.AddRoleToUserUseCase
import users.application.useCases.GetAllUsersUseCase
import users.application.useCases.GetUserByEmailUseCase
import users.application.useCases.GetUserByIdUseCase
import users.application.useCases.RemoveRoleFromUserUseCase
import users.application.useCases.UserUseCase
import users.domain.Role

class UserController(
    private val registerUserUseCase: UserUseCase,
    private val getUserByEmailUseCase: GetUserByEmailUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val addRoleToUserUseCase: AddRoleToUserUseCase,
    private val removeRoleFromUserUseCase: RemoveRoleFromUserUseCase
) {
    suspend fun registerUser(call: ApplicationCall) {
        try {
            val request = call.receive<RegisterUserRequest>()
            registerUserUseCase.registerUser(request.name, request.email)
            
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

    suspend fun getAllUsers(call: ApplicationCall) {
        try {
            val users = getAllUsersUseCase.execute()
            call.respond(HttpStatusCode.OK, users.map { UserResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Erro interno do servidor: ${e.message}"))
        }
    }

    suspend fun getUserById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                return
            }

            val user = getUserByIdUseCase.execute(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, UserResponse.fromDomain(user))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Erro interno do servidor: ${e.message}"))
        }
    }

    suspend fun addRoleToUser(call: ApplicationCall) {
        try {
            val userId = call.parameters["id"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                return
            }

            val request = call.receive<AddRoleRequest>()
            addRoleToUserUseCase.execute(userId, request.role)
            
            val user = getUserByIdUseCase.execute(userId)
            if (user != null) {
                call.respond(HttpStatusCode.OK, UserResponse.fromDomain(user))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuário não encontrado"))
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "Usuário não encontrado"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Erro interno do servidor: ${e.message}"))
        }
    }

    suspend fun removeRoleFromUser(call: ApplicationCall) {
        try {
            val userId = call.parameters["id"]?.toLongOrNull()
            val roleParam = call.parameters["role"]
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                return
            }
            
            if (roleParam == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Role é obrigatório"))
                return
            }

            val role = try {
                Role.valueOf(roleParam.uppercase())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Role inválido: $roleParam"))
                return
            }

            removeRoleFromUserUseCase.execute(userId, role)
            
            val user = getUserByIdUseCase.execute(userId)
            if (user != null) {
                call.respond(HttpStatusCode.OK, UserResponse.fromDomain(user))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuário não encontrado"))
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "Usuário não encontrado"))
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Operação não permitida"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Erro interno do servidor: ${e.message}"))
        }
    }
}
