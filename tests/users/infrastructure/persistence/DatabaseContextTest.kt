import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import users.domain.User
import users.infrastructure.persistence.DatabaseContext

class DatabaseContextTest {

    private lateinit var dbContext: DatabaseContext

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
    }

    @Test
    fun `deve adicionar usuário com sucesso`() = runTest {
        val user = User(name = "Alice", email = "alice@example.com")
        val userId = dbContext.addUser(user)

        assertEquals(1L, userId)
        val foundUser = dbContext.findUserByEmail("alice@example.com")
        assertNotNull(foundUser)
        assertEquals("Alice", foundUser?.name)
        assertEquals("alice@example.com", foundUser?.email)
        assertEquals(1L, foundUser?.id)
    }

    @Test
    fun `deve lançar exceção ao tentar adicionar usuário com email duplicado`() = runTest {
        val user1 = User(name = "Alice", email = "alice@example.com")
        val user2 = User(name = "Bob", email = "alice@example.com")

        dbContext.addUser(user1)

        try {
            dbContext.addUser(user2)
            fail("Deveria ter lançado IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("E-mail já cadastrado", e.message)
        }
    }

    @Test
    fun `deve retornar null quando usuário não existe`() = runTest {
        val foundUser = dbContext.findUserByEmail("inexistente@example.com")
        assertNull(foundUser)
    }

    @Test
    fun `deve atribuir IDs sequenciais aos usuários`() = runTest {
        val user1 = User(name = "Alice", email = "alice@example.com")
        val user2 = User(name = "Bob", email = "bob@example.com")
        val user3 = User(name = "Charlie", email = "charlie@example.com")

        val id1 = dbContext.addUser(user1)
        val id2 = dbContext.addUser(user2)
        val id3 = dbContext.addUser(user3)

        assertEquals(1L, id1)
        assertEquals(2L, id2)
        assertEquals(3L, id3)

        val foundUser1 = dbContext.findUserByEmail("alice@example.com")
        val foundUser2 = dbContext.findUserByEmail("bob@example.com")
        val foundUser3 = dbContext.findUserByEmail("charlie@example.com")

        assertEquals(1L, foundUser1?.id)
        assertEquals(2L, foundUser2?.id)
        assertEquals(3L, foundUser3?.id)
    }

    @Test
    fun `deve executar transação com sucesso`() = runTest {
        var executed = false
        val result = dbContext.executeTransaction {
            executed = true
            "success"
        }

        assertTrue(executed)
        assertEquals("success", result)
    }

    @Test
    fun `deve fazer rollback quando transação falha`() = runTest {
        val user = User(name = "Alice", email = "alice@example.com")
        
        try {
            dbContext.executeTransaction {
                dbContext.addUser(user)
                throw RuntimeException("Erro simulado")
            }
        } catch (e: RuntimeException) {
            // Exceção esperada - rollback deve ter ocorrido
        }

        // Verifica que o usuário não foi adicionado devido ao rollback
        val foundUser = dbContext.findUserByEmail("alice@example.com")
        assertNull(foundUser)
    }
}

