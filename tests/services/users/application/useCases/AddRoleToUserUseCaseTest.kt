import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import users.adapters.outbound.UnitOfWorkAdapter
import users.adapters.outbound.UserRepositoryAdapter
import users.application.useCases.AddRoleToUserUseCase
import users.application.useCases.UserUseCase
import users.domain.Role
import users.domain.valueObjects.UserEmail
import users.infrastructure.persistence.DatabaseContext

class AddRoleToUserUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var userRepository: UserRepositoryAdapter
    private lateinit var userUseCase: UserUseCase
    private lateinit var addRoleToUserUseCase: AddRoleToUserUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        unitOfWork = UnitOfWorkAdapter(dbContext)
        userRepository = UserRepositoryAdapter(dbContext)
        userUseCase = UserUseCase(unitOfWork)
        addRoleToUserUseCase = AddRoleToUserUseCase(userRepository)
    }

    @Test
    fun `deve adicionar role PARTNER a um usuário`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)
        assertEquals(setOf(Role.CUSTOMER), user?.roles)

        // Act
        val result = addRoleToUserUseCase.execute(user!!.id, Role.PARTNER)

        // Assert
        assertTrue(result)
        val updatedUser = userRepository.getById(user.id)
        assertNotNull(updatedUser)
        assertEquals(setOf(Role.CUSTOMER, Role.PARTNER), updatedUser?.roles)
    }

    @Test
    fun `deve adicionar role ADMIN a um usuário`() = runTest {
        // Arrange
        userUseCase.registerUser("Admin", "admin@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("admin@example.com"))
        assertNotNull(user)

        // Act
        val result = addRoleToUserUseCase.execute(user!!.id, Role.ADMIN)

        // Assert
        assertTrue(result)
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER, Role.ADMIN), updatedUser?.roles)
    }

    @Test
    fun `deve retornar true quando usuário já possui o role`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)

        // Act - tentar adicionar CUSTOMER que já existe
        val result = addRoleToUserUseCase.execute(user!!.id, Role.CUSTOMER)

        // Assert
        assertTrue(result)
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER), updatedUser?.roles)
    }

    @Test
    fun `deve lançar exceção quando usuário não existe`() = runTest {
        // Act & Assert
        try {
            addRoleToUserUseCase.execute(999L, Role.PARTNER)
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Usuário não encontrado", e.message)
        }
    }

    @Test
    fun `deve permitir usuário ter múltiplos roles`() = runTest {
        // Arrange
        userUseCase.registerUser("SuperUser", "super@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("super@example.com"))
        assertNotNull(user)

        // Act
        addRoleToUserUseCase.execute(user!!.id, Role.PARTNER)
        addRoleToUserUseCase.execute(user.id, Role.ADMIN)

        // Assert
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER, Role.PARTNER, Role.ADMIN), updatedUser?.roles)
    }
}
