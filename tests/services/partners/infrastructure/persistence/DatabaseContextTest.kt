import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.domain.DocumentType
import partners.domain.Partner
import partners.domain.PartnerStatus
import partners.infrastructure.persistence.DatabaseContext

class DatabaseContextTest {

    private lateinit var dbContext: DatabaseContext

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
    }

    @Test
    fun `deve adicionar parceiro com sucesso`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 1L,
                        companyName = "Empresa Teste",
                        tradeName = "Teste LTDA",
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )

        // Act
        val id = dbContext.addPartner(partner)

        // Assert
        assertNotNull(id)
        val found = dbContext.findById(id)
        assertNotNull(found)
        assertEquals("Empresa Teste", found?.companyName)
    }

    @Test
    fun `deve encontrar parceiro por userId`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 5L,
                        companyName = "Empresa do User 5",
                        tradeName = null,
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        dbContext.addPartner(partner)

        // Act
        val found = dbContext.findByUserId(5L)

        // Assert
        assertNotNull(found)
        assertEquals(5L, found?.userId)
    }

    @Test
    fun `deve encontrar parceiro por documento`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 1L,
                        companyName = "Empresa Teste",
                        tradeName = null,
                        document = "98765432109876",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        dbContext.addPartner(partner)

        // Act
        val found = dbContext.findByDocument("98765432109876")

        // Assert
        assertNotNull(found)
        assertEquals("98765432109876", found?.document)
    }

    @Test
    fun `deve listar parceiros por status`() = runTest {
        // Arrange
        val partner1 =
                Partner(
                        userId = 1L,
                        companyName = "Empresa 1",
                        tradeName = null,
                        document = "11111111111111",
                        documentType = DocumentType.CNPJ,
                        email = "empresa1@email.com",
                        phone = "11999999999",
                        status = PartnerStatus.PENDING
                )
        val partner2 =
                Partner(
                        userId = 2L,
                        companyName = "Empresa 2",
                        tradeName = null,
                        document = "22222222222222",
                        documentType = DocumentType.CNPJ,
                        email = "empresa2@email.com",
                        phone = "11888888888",
                        status = PartnerStatus.APPROVED
                )
        dbContext.addPartner(partner1)
        dbContext.addPartner(partner2)

        // Act
        val pendingPartners = dbContext.getPartnersByStatus(PartnerStatus.PENDING)
        val approvedPartners = dbContext.getPartnersByStatus(PartnerStatus.APPROVED)

        // Assert
        assertEquals(1, pendingPartners.size)
        assertEquals(1, approvedPartners.size)
    }

    @Test
    fun `deve atualizar parceiro com sucesso`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 1L,
                        companyName = "Nome Original",
                        tradeName = null,
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val id = dbContext.addPartner(partner)
        val updatedPartner = partner.copy(companyName = "Nome Atualizado")

        // Act
        val result = dbContext.updatePartner(updatedPartner)

        // Assert
        assertTrue(result)
        val found = dbContext.findById(id)
        assertEquals("Nome Atualizado", found?.companyName)
    }

    @Test
    fun `deve deletar parceiro com sucesso`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 1L,
                        companyName = "Empresa Teste",
                        tradeName = null,
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val id = dbContext.addPartner(partner)

        // Act
        val result = dbContext.deletePartner(id)

        // Assert
        assertTrue(result)
        assertNull(dbContext.findById(id))
    }
}
