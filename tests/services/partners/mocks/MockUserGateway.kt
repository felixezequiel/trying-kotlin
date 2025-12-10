package partners.mocks

import partners.application.ports.outbound.IUserGateway

/** Mock do IUserGateway para testes */
class MockUserGateway : IUserGateway {
    private var nextUserId = 1L
    private val usersByEmail = mutableMapOf<String, Long>()

    override suspend fun findOrCreateByEmail(email: String, name: String): Long {
        return usersByEmail.getOrPut(email) { nextUserId++ }
    }

    fun reset() {
        nextUserId = 1L
        usersByEmail.clear()
    }
}
