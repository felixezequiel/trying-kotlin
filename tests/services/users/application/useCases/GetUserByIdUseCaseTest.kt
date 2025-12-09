import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import users.adapters.outbound.UnitOfWorkAdapter
import users.adapters.outbound.UserRepositoryAdapter
import users.application.useCases.GetUserByIdUseCase
import users.application.useCases.UserUseCase
import users.domain.Role
import users.infrastructure.persistence.DatabaseContext

class GetUserByIdUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var userRepository: UserRepositoryAdapter
    private lateinit var userUseCase: UserUseCase
    private lateinit var getUserByIdUseCase: GetUserByIdUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        unitOfWork = UnitOfWorkAdapter(dbContext)
        userRepository = UserRepositoryAdapter(dbContext)
        userUseCase = UserUseCase(unitOfWork)
        getUserByIdUseCase = GetUserByIdUseCase(userRepository)
    }

    @Test
    fun `deve retornar usuário quando existe`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail("alice@example.com")
        assertNotNull(user)

        // Act
        val foundUser = getUserByIdUseCase.execute(user!!.id)

        // Assert
        assertNotNull(foundUser)
        assertEquals("Alice", foundUser?.name)
        assertEquals("alice@example.com", foundUser?.email)
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
        val user = userRepository.getUserByEmail("super@example.com")
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
