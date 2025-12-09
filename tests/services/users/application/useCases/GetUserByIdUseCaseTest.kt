import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.users.FakeUnitOfWork
import services.users.FakeUserRepository
import users.application.useCases.GetUserByIdUseCase
import users.application.useCases.UserUseCase
import users.domain.Role
import users.domain.valueObjects.UserEmail

class GetUserByIdUseCaseTest {

    private lateinit var userRepository: FakeUserRepository
    private lateinit var unitOfWork: FakeUnitOfWork
    private lateinit var userUseCase: UserUseCase
    private lateinit var getUserByIdUseCase: GetUserByIdUseCase

    @BeforeEach
    fun setUp() {
        userRepository = FakeUserRepository()
        unitOfWork = FakeUnitOfWork(userRepository)
        userUseCase = UserUseCase(unitOfWork)
        getUserByIdUseCase = GetUserByIdUseCase(unitOfWork)
    }

    @Test
    fun `deve retornar usuário quando existe`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)

        // Act
        val foundUser = getUserByIdUseCase.execute(user!!.id)

        // Assert
        assertNotNull(foundUser)
        assertEquals("Alice", foundUser?.name?.value)
        assertEquals("alice@example.com", foundUser?.email?.value)
        assertEquals(setOf(Role.CUSTOMER), foundUser?.roles)
    }

    @Test
    fun `deve retornar null quando usuário não existe`() = runTest {
        // Act
        val foundUser = getUserByIdUseCase.execute(999L)

        // Assert
        assertNull(foundUser)
    }

    @Test
    fun `deve retornar usuário com múltiplos roles`() = runTest {
        // Arrange
        userUseCase.registerUser("SuperUser", "super@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("super@example.com"))
        assertNotNull(user)
        userRepository.addRole(user!!.id, Role.PARTNER)
        userRepository.addRole(user.id, Role.ADMIN)

        // Act
        val foundUser = getUserByIdUseCase.execute(user.id)

        // Assert
        assertNotNull(foundUser)
        assertEquals(setOf(Role.CUSTOMER, Role.PARTNER, Role.ADMIN), foundUser?.roles)
    }
}
