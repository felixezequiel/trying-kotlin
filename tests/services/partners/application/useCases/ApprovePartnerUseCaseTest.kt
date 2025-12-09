import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.ApprovePartnerUseCase
import partners.application.useCases.CreatePartnerUseCase
import partners.domain.DocumentType
import partners.domain.PartnerStatus
import partners.infrastructure.persistence.DatabaseContext

class ApprovePartnerUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var partnerRepository: PartnerRepositoryAdapter
    private lateinit var createPartnerUseCase: CreatePartnerUseCase
    private lateinit var approvePartnerUseCase: ApprovePartnerUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        partnerRepository = PartnerRepositoryAdapter(dbContext)
        createPartnerUseCase = CreatePartnerUseCase(partnerRepository)
        approvePartnerUseCase = ApprovePartnerUseCase(partnerRepository)
    }

    @Test
    fun `deve aprovar parceiro pendente com sucesso`() = runTest {
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
        val approvedPartner = approvePartnerUseCase.execute(partnerId)

        // Assert
        assertEquals(PartnerStatus.APPROVED, approvedPartner.status)
        assertNotNull(approvedPartner.approvedAt)
    }

    @Test
    fun `deve falhar ao aprovar parceiro não pendente`() = runTest {
        // Arrange
        val request =
                CreatePartnerRequest(
                        companyName = "Empresa Teste",
                        tradeName = null,
                        document = "12345678901234",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val partnerId = createPartnerUseCase.execute(userId = 1L, request = request)
        approvePartnerUseCase.execute(partnerId) // Aprova primeiro

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        approvePartnerUseCase.execute(partnerId) // Tenta aprovar novamente
                    }
                }
        assertEquals("Apenas parceiros pendentes podem ser aprovados", exception.message)
    }

    @Test
    fun `deve falhar ao aprovar parceiro inexistente`() = runTest {
        // Arrange
        val fakeId = java.util.UUID.randomUUID()

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking { approvePartnerUseCase.execute(fakeId) }
                }
        assertEquals("Parceiro não encontrado", exception.message)
    }
}
