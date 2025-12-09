import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import users.adapters.outbound.UnitOfWorkAdapter
import users.adapters.outbound.UserRepositoryAdapter
import users.application.useCases.AddRoleToUserUseCase
import users.application.useCases.RemoveRoleFromUserUseCase
import users.application.useCases.UserUseCase
import users.domain.Role
import users.domain.valueObjects.UserEmail
import users.infrastructure.persistence.DatabaseContext

class RemoveRoleFromUserUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var userRepository: UserRepositoryAdapter
    private lateinit var userUseCase: UserUseCase
    private lateinit var addRoleToUserUseCase: AddRoleToUserUseCase
    private lateinit var removeRoleFromUserUseCase: RemoveRoleFromUserUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        unitOfWork = UnitOfWorkAdapter(dbContext)
        userRepository = UserRepositoryAdapter(dbContext)
        userUseCase = UserUseCase(unitOfWork)
        addRoleToUserUseCase = AddRoleToUserUseCase(userRepository)
        removeRoleFromUserUseCase = RemoveRoleFromUserUseCase(userRepository)
    }

    @Test
    fun `deve remover role PARTNER de um usuário`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)
        addRoleToUserUseCase.execute(user!!.id, Role.PARTNER)

        val userWithPartner = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER, Role.PARTNER), userWithPartner?.roles)

        // Act
        val result = removeRoleFromUserUseCase.execute(user.id, Role.PARTNER)

        // Assert
        assertTrue(result)
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER), updatedUser?.roles)
    }

    @Test
    fun `deve lançar exceção ao tentar remover CUSTOMER quando é o único role`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)
        assertEquals(setOf(Role.CUSTOMER), user?.roles)

        // Act & Assert
        try {
            removeRoleFromUserUseCase.execute(user!!.id, Role.CUSTOMER)
            fail("Deveria ter lançado IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals(
                    "Não é possível remover o role CUSTOMER quando é o único role do usuário",
                    e.message
            )
        }
    }

    @Test
    fun `deve permitir remover CUSTOMER quando usuário tem outros roles`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)
        addRoleToUserUseCase.execute(user!!.id, Role.PARTNER)

        // Act
        val result = removeRoleFromUserUseCase.execute(user.id, Role.CUSTOMER)

        // Assert
        assertTrue(result)
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.PARTNER), updatedUser?.roles)
    }

    @Test
    fun `deve retornar true quando usuário não possui o role`() = runTest {
        // Arrange
        userUseCase.registerUser("Alice", "alice@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("alice@example.com"))
        assertNotNull(user)

        // Act - tentar remover PARTNER que não existe
        val result = removeRoleFromUserUseCase.execute(user!!.id, Role.PARTNER)

        // Assert
        assertTrue(result)
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER), updatedUser?.roles)
    }

    @Test
    fun `deve lançar exceção quando usuário não existe`() = runTest {
        // Act & Assert
        try {
            removeRoleFromUserUseCase.execute(999L, Role.PARTNER)
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Usuário não encontrado", e.message)
        }
    }

    @Test
    fun `deve remover múltiplos roles de um usuário`() = runTest {
        // Arrange
        userUseCase.registerUser("SuperUser", "super@example.com")
        val user = userRepository.getUserByEmail(UserEmail.of("super@example.com"))
        assertNotNull(user)
        addRoleToUserUseCase.execute(user!!.id, Role.PARTNER)
        addRoleToUserUseCase.execute(user.id, Role.ADMIN)

        val userWithAllRoles = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER, Role.PARTNER, Role.ADMIN), userWithAllRoles?.roles)

        // Act
        removeRoleFromUserUseCase.execute(user.id, Role.PARTNER)
        removeRoleFromUserUseCase.execute(user.id, Role.ADMIN)

        // Assert
        val updatedUser = userRepository.getById(user.id)
        assertEquals(setOf(Role.CUSTOMER), updatedUser?.roles)
    }
}
