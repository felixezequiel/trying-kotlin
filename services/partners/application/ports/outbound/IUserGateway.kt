package partners.application.ports.outbound

/**
 * Gateway para comunicação com o serviço de Users. Implementação faz chamadas HTTP via BFF conforme
 * ADR-001.
 */
interface IUserGateway {
    /**
     * Busca usuário por email. Se não existir, cria um novo. Em ambos os casos, adiciona a role
     * PARTNER ao usuário.
     * @param email Email do usuário
     * @param name Nome do usuário (usado na criação)
     * @return userId do usuário encontrado ou criado
     * @throws UserGatewayException se houver falha na comunicação
     */
    suspend fun findOrCreateByEmail(email: String, name: String): Long
}

/** Exceção lançada quando há falha na comunicação com o serviço de Users */
class UserGatewayException(message: String, cause: Throwable? = null) :
        RuntimeException(message, cause)
