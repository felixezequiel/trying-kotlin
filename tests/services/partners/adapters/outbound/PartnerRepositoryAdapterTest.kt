import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.domain.DocumentType
import partners.domain.Partner
import partners.domain.PartnerStatus
import partners.infrastructure.persistence.DatabaseContext

class PartnerRepositoryAdapterTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var partnerRepository: PartnerRepositoryAdapter

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        partnerRepository = PartnerRepositoryAdapter(dbContext)
    }

    @Test
    fun `deve adicionar e buscar parceiro`() = runTest {
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
        val id = partnerRepository.add(partner)
        val found = partnerRepository.getById(id)

        // Assert
        assertNotNull(found)
        assertEquals("Empresa Teste", found?.companyName)
    }

    @Test
    fun `deve buscar por userId`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 10L,
                        companyName = "Empresa do User 10",
                        tradeName = null,
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        partnerRepository.add(partner)

        // Act
        val found = partnerRepository.getByUserId(10L)

        // Assert
        assertNotNull(found)
        assertEquals(10L, found?.userId)
    }

    @Test
    fun `deve buscar por documento`() = runTest {
        // Arrange
        val partner =
                Partner(
                        userId = 1L,
                        companyName = "Empresa Teste",
                        tradeName = null,
                        document = "99999999999999",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        partnerRepository.add(partner)

        // Act
        val found = partnerRepository.getByDocument("99999999999999")

        // Assert
        assertNotNull(found)
        assertEquals("99999999999999", found?.document)
    }

    @Test
    fun `deve listar todos os parceiros`() = runTest {
        // Arrange
        val partner1 =
                Partner(
                        userId = 1L,
                        companyName = "Empresa 1",
                        tradeName = null,
                        document = "11111111111111",
                        documentType = DocumentType.CNPJ,
                        email = "empresa1@email.com",
                        phone = "11999999999"
                )
        val partner2 =
                Partner(
                        userId = 2L,
                        companyName = "Empresa 2",
                        tradeName = null,
                        document = "22222222222222",
                        documentType = DocumentType.CNPJ,
                        email = "empresa2@email.com",
                        phone = "11888888888"
                )
        partnerRepository.add(partner1)
        partnerRepository.add(partner2)

        // Act
        val partners = partnerRepository.getAll()

        // Assert
        assertEquals(2, partners.size)
    }

    @Test
    fun `deve filtrar por status`() = runTest {
        // Arrange
        val partner1 =
                Partner(
                        userId = 1L,
                        companyName = "Empresa Pendente",
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
                        companyName = "Empresa Aprovada",
                        tradeName = null,
                        document = "22222222222222",
                        documentType = DocumentType.CNPJ,
                        email = "empresa2@email.com",
                        phone = "11888888888",
                        status = PartnerStatus.APPROVED
                )
        partnerRepository.add(partner1)
        partnerRepository.add(partner2)

        // Act
        val pending = partnerRepository.getByStatus(PartnerStatus.PENDING)
        val approved = partnerRepository.getByStatus(PartnerStatus.APPROVED)

        // Assert
        assertEquals(1, pending.size)
        assertEquals("Empresa Pendente", pending[0].companyName)
        assertEquals(1, approved.size)
        assertEquals("Empresa Aprovada", approved[0].companyName)
    }

    @Test
    fun `deve atualizar parceiro`() = runTest {
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
        val id = partnerRepository.add(partner)
        val updatedPartner = partner.copy(companyName = "Nome Atualizado")

        // Act
        val result = partnerRepository.update(updatedPartner)

        // Assert
        assertTrue(result)
        val found = partnerRepository.getById(id)
        assertEquals("Nome Atualizado", found?.companyName)
    }

    @Test
    fun `deve deletar parceiro`() = runTest {
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
        val id = partnerRepository.add(partner)

        // Act
        val result = partnerRepository.delete(id)

        // Assert
        assertTrue(result)
        assertNull(partnerRepository.getById(id))
    }
}
