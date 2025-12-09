import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import users.domain.User
import users.infrastructure.persistence.DatabaseContext
import users.adapters.outbound.UserRepositoryAdapter

class UserRepositoryAdapterTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var userRepository: UserRepositoryAdapter

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        userRepository = UserRepositoryAdapter(dbContext)
    }

    @Test
    fun `deve adicionar usuário e retornar ID`() = runTest {
        val user = User(name = "Alice", email = "alice@example.com")
        val userId = userRepository.add(user)

        assertTrue(userId > 0)
        val foundUser = userRepository.getUserByEmail("alice@example.com")
        assertNotNull(foundUser)
        assertEquals("Alice", foundUser?.name)
        assertEquals("alice@example.com", foundUser?.email)
    }

    @Test
    fun `deve retornar usuário existente por email`() = runTest {
        val user = User(name = "Bob", email = "bob@example.com")
        userRepository.add(user)

        val foundUser = userRepository.getUserByEmail("bob@example.com")
        assertNotNull(foundUser)
        assertEquals("Bob", foundUser?.name)
        assertEquals("bob@example.com", foundUser?.email)
    }

    @Test
    fun `deve retornar null quando usuário não existe`() = runTest {
        val foundUser = userRepository.getUserByEmail("inexistente@example.com")
        assertNull(foundUser)
    }

    @Test
    fun `deve lançar exceção ao adicionar usuário com email duplicado`() = runTest {
        val user1 = User(name = "Alice", email = "alice@example.com")
        val user2 = User(name = "Bob", email = "alice@example.com")

        userRepository.add(user1)

        try {
            userRepository.add(user2)
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("E-mail já cadastrado", e.message)
        }
    }
}

