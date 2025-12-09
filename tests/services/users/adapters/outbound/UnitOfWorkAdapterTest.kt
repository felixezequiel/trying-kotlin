import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.users.TestHelpers
import users.adapters.outbound.UnitOfWorkAdapter
import users.domain.valueObjects.UserEmail
import users.infrastructure.persistence.DatabaseContext

class UnitOfWorkAdapterTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var unitOfWork: UnitOfWorkAdapter

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        unitOfWork = UnitOfWorkAdapter(dbContext)
    }

    @Test
    fun `deve executar bloco dentro de transação`() = runTest {
        var executed = false
        val result =
                unitOfWork.runInTransaction {
                    executed = true
                    "test result"
                }

        assertTrue(executed)
        assertEquals("test result", result)
    }

    @Test
    fun `deve retornar repositório de usuário`() = runTest {
        val userRepository = unitOfWork.userRepository()
        assertNotNull(userRepository)
    }

    @Test
    fun `deve permitir operações com repositório dentro de transação`() = runTest {
        unitOfWork.runInTransaction {
            val userRepository = unitOfWork.userRepository()
            val user = TestHelpers.createTestUser(name = "Alice", email = "alice@example.com")
            val userId = userRepository.add(user)

            assertTrue(userId > 0)
            val foundUser = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
            assertNotNull(foundUser)
            assertEquals("Alice", foundUser?.name?.value)
        }
    }

    @Test
    fun `deve fazer rollback quando exceção ocorre na transação`() = runTest {
        try {
            unitOfWork.runInTransaction {
                val userRepository = unitOfWork.userRepository()
                val user = TestHelpers.createTestUser(name = "Alice", email = "alice@example.com")
                userRepository.add(user)
                throw RuntimeException("Erro simulado")
            }
        } catch (e: RuntimeException) {
            // Exceção esperada - rollback deve ter ocorrido
        }

        // Verifica que o usuário não foi adicionado devido ao rollback
        val userRepository = unitOfWork.userRepository()
        val foundUser = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNull(foundUser)
    }
}
