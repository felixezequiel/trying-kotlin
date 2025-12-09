import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.GetPartnerUseCase
import partners.domain.DocumentType
import partners.infrastructure.persistence.DatabaseContext

class GetPartnerUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var partnerRepository: PartnerRepositoryAdapter
    private lateinit var createPartnerUseCase: CreatePartnerUseCase
    private lateinit var getPartnerUseCase: GetPartnerUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        partnerRepository = PartnerRepositoryAdapter(dbContext)
        createPartnerUseCase = CreatePartnerUseCase(partnerRepository)
        getPartnerUseCase = GetPartnerUseCase(partnerRepository)
    }

    @Test
    fun `deve retornar parceiro por ID`() = runTest {
        // Arrange
        val request =
                CreatePartnerRequest(
                        companyName = "Empresa Teste",
                        tradeName = "Teste LTDA",
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val partnerId = createPartnerUseCase.execute(userId = 1L, request = request)

        // Act
        val partner = getPartnerUseCase.execute(partnerId)

        // Assert
        assertNotNull(partner)
        assertEquals("Empresa Teste", partner?.companyName)
        assertEquals(partnerId, partner?.id)
    }

    @Test
    fun `deve retornar null para ID inexistente`() = runTest {
        // Act
        val partner = getPartnerUseCase.execute(UUID.randomUUID())

        // Assert
        assertNull(partner)
    }

    @Test
    fun `deve retornar parceiro por userId`() = runTest {
        // Arrange
        val request =
                CreatePartnerRequest(
                        companyName = "Empresa do User 5",
                        tradeName = null,
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        createPartnerUseCase.execute(userId = 5L, request = request)

        // Act
        val partner = getPartnerUseCase.executeByUserId(5L)

        // Assert
        assertNotNull(partner)
        assertEquals("Empresa do User 5", partner?.companyName)
        assertEquals(5L, partner?.userId)
    }

    @Test
    fun `deve retornar null para userId sem parceiro`() = runTest {
        // Act
        val partner = getPartnerUseCase.executeByUserId(999L)

        // Assert
        assertNull(partner)
    }
}
