package users.adapters.inbound

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import users.application.dto.UserResponse
import users.application.useCases.GetUserByEmailUseCase
import users.application.useCases.GetAllUsersUseCase
import users.application.useCases.UserUseCase
import users.domain.User

class UserGraphQLSchema(
    private val registerUserUseCase: UserUseCase,
    private val getUserByEmailUseCase: GetUserByEmailUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) {
    fun configureSchema(schema: SchemaBuilder) {
        schema.type<User> {
            description = "Representa um usuário do sistema"
        }

        schema.type<UserResponse> {
            description = "Resposta com dados do usuário"
        }

        schema.query("user") {
            description = "Busca um usuário pelo email"
            resolver { email: String ->
                val user = getUserByEmailUseCase.execute(email)
                user?.let { UserResponse.fromDomain(it) }
            }
        }

        schema.query("users") {
            description = "Lista todos os usuários"
            resolver { ->
                getAllUsersUseCase.execute().map { UserResponse.fromDomain(it) }
            }
        }

        schema.mutation("registerUser") {
            description = "Registra um novo usuário no sistema"
            resolver { name: String, email: String ->
                try {
                    registerUserUseCase.registerUser(name, email)
                    val user = getUserByEmailUseCase.execute(email)
                    user?.let { UserResponse.fromDomain(it) }
                        ?: throw IllegalStateException("Usuário criado mas não encontrado")
                } catch (e: IllegalArgumentException) {
                    throw GraphQLException(e.message ?: "Dados inválidos", "BAD_REQUEST")
                } catch (e: IllegalStateException) {
                    if (e.message?.contains("já existe") == true) {
                        throw GraphQLException(e.message ?: "Conflito ao criar usuário", "CONFLICT")
                    }
                    throw GraphQLException(e.message ?: "Erro ao criar usuário", "INTERNAL_ERROR")
                }
            }
        }
    }
}

class GraphQLException(
    message: String,
    val code: String
) : Exception(message)
