import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.users.FakeUnitOfWork
import services.users.FakeUserRepository
import users.application.useCases.UserUseCase
import users.domain.valueObjects.UserEmail

class UserUseCaseTest {

    private lateinit var userRepository: FakeUserRepository
    private lateinit var unitOfWork: FakeUnitOfWork
    private lateinit var userUseCase: UserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = FakeUserRepository()
        unitOfWork = FakeUnitOfWork(userRepository)
        userUseCase = UserUseCase(unitOfWork)
    }

    @Test
    fun `deve registrar usuário com sucesso`() = runTest {
        userUseCase.registerUser("Alice", "alice@example.com")

        val foundUser = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(foundUser)
        assertEquals("Alice", foundUser?.name?.value)
        assertEquals("alice@example.com", foundUser?.email?.value)
    }

    @Test
    fun `deve lançar exceção quando nome está vazio`() = runTest {
        try {
            userUseCase.registerUser("", "alice@example.com")
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Nome não pode estar vazio", e.message)
        }
    }

    @Test
    fun `deve lançar exceção quando email está vazio`() = runTest {
        try {
            userUseCase.registerUser("Alice", "")
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Email não pode estar vazio", e.message)
        }
    }

    @Test
    fun `deve lançar exceção quando nome está em branco`() = runTest {
        try {
            userUseCase.registerUser("   ", "alice@example.com")
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Nome não pode estar vazio", e.message)
        }
    }

    @Test
    fun `deve lançar exceção quando email está em branco`() = runTest {
        try {
            userUseCase.registerUser("Alice", "   ")
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Email não pode estar vazio", e.message)
        }
    }

    @Test
    fun `deve lançar exceção ao tentar registrar usuário com email duplicado`() = runTest {
        userUseCase.registerUser("Alice", "alice@example.com")

        try {
            userUseCase.registerUser("Bob", "alice@example.com")
            fail("Deveria ter lançado IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Usuário com este e-mail já existe.", e.message)
        }
    }

    @Test
    fun `deve registrar múltiplos usuários com emails diferentes`() = runTest {
        userUseCase.registerUser("Alice", "alice@example.com")
        userUseCase.registerUser("Bob", "bob@example.com")
        userUseCase.registerUser("Charlie", "charlie@example.com")

        val user1 = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        val user2 = userRepository.getUserByEmail(UserEmail.of("bob@example.com"))
        val user3 = userRepository.getUserByEmail(UserEmail.of("charlie@example.com"))

        assertNotNull(user1)
        assertNotNull(user2)
        assertNotNull(user3)
        assertEquals("Alice", user1?.name?.value)
        assertEquals("Bob", user2?.name?.value)
        assertEquals("Charlie", user3?.name?.value)
    }

    @Test
    fun `deve fazer rollback quando exceção ocorre durante registro`() = runTest {
        userUseCase.registerUser("Alice", "alice@example.com")

        // Tenta registrar com email duplicado
        try {
            userUseCase.registerUser("Bob", "alice@example.com")
            fail("Deveria ter lançado IllegalStateException")
        } catch (e: IllegalStateException) {
            // Exceção esperada
        }

        // Verifica que apenas o primeiro usuário foi registrado
        val user1 = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        val user2 = userRepository.getUserByEmail(UserEmail.of("bob@example.com"))

        assertNotNull(user1)
        assertNull(user2)
    }
}
