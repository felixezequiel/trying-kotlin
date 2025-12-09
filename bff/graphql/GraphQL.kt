package bff.graphql

import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*
import bff.clients.ServiceClients
import bff.clients.RegisterUserRequest

/**
 * Configura o schema GraphQL do BFF
 */
fun Application.configureGraphQL(serviceClients: ServiceClients) {
    install(GraphQL) {
        playground = true
        
        schema {
            // Queries
            query("users") {
                resolver { ->
                    serviceClients.users.getAllUsers()
                }
            }
            
            query("userByEmail") {
                resolver { email: String ->
                    serviceClients.users.getUserByEmail(email)
                }
            }
            
            // Mutations
            mutation("registerUser") {
                resolver { name: String, email: String ->
                    serviceClients.users.registerUser(
                        RegisterUserRequest(name = name, email = email)
                    )
                }
            }
            
            // Adicione queries e mutations para outros serviços aqui
        }
    }
}
