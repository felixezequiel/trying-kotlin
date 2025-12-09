package users.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    CUSTOMER,   // Pode comprar ingressos (atribuído no registro)
    PARTNER,    // Pode criar eventos (requer aprovação)
    ADMIN       // Gestão geral (atribuído manualmente)
}
