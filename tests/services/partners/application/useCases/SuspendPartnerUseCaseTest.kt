import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.ApprovePartnerUseCase
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.SuspendPartnerUseCase
import partners.domain.DocumentType
import partners.domain.PartnerStatus
import partners.infrastructure.persistence.DatabaseContext

class SuspendPartnerUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var partnerRepository: PartnerRepositoryAdapter
    private lateinit var createPartnerUseCase: CreatePartnerUseCase
    private lateinit var approvePartnerUseCase: ApprovePartnerUseCase
    private lateinit var suspendPartnerUseCase: SuspendPartnerUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        partnerRepository = PartnerRepositoryAdapter(dbContext)
        createPartnerUseCase = CreatePartnerUseCase(partnerRepository)
        approvePartnerUseCase = ApprovePartnerUseCase(partnerRepository)
        suspendPartnerUseCase = SuspendPartnerUseCase(partnerRepository)
    }

    @Test
    fun `deve suspender parceiro aprovado com sucesso`() = runTest {
        // Arrange
        val request =
                CreatePartnerRequest(
                        companyName = "Empresa Teste",
                        tradeName = null,
                        document = "11222333000181",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val partnerId = createPartnerUseCase.execute(userId = 1L, request = request)
        approvePartnerUseCase.execute(partnerId)

        // Act
        val suspendedPartner = suspendPartnerUseCase.execute(partnerId)

        // Assert
        assertEquals(PartnerStatus.SUSPENDED, suspendedPartner.status)
    }

    @Test
    fun `deve falhar ao suspender parceiro não aprovado`() = runTest {
        // Arrange
        val request =
                CreatePartnerRequest(
                        companyName = "Empresa Teste",
                        tradeName = null,
                        document = "11222333000181",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val partnerId = createPartnerUseCase.execute(userId = 1L, request = request)
        // Parceiro está PENDING

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking { suspendPartnerUseCase.execute(partnerId) }
                }
        assertEquals("Apenas parceiros aprovados podem ser suspensos", exception.message)
    }
}
