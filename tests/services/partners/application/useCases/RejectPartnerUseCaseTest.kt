import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.InMemoryPartnerStore
import partners.adapters.outbound.UnitOfWorkAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.RejectPartnerUseCase
import partners.domain.DocumentType
import partners.domain.PartnerStatus

class RejectPartnerUseCaseTest {

    private lateinit var partnerStore: InMemoryPartnerStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var createPartnerUseCase: CreatePartnerUseCase
    private lateinit var rejectPartnerUseCase: RejectPartnerUseCase

    @BeforeEach
    fun setUp() {
        partnerStore = InMemoryPartnerStore()
        unitOfWork = UnitOfWorkAdapter(partnerStore.repository, partnerStore.transactionManager)
        createPartnerUseCase = CreatePartnerUseCase(unitOfWork)
        rejectPartnerUseCase = RejectPartnerUseCase(unitOfWork)
    }

    @Test
    fun `deve rejeitar parceiro pendente com sucesso`() = runTest {
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

        // Act
        val rejectedPartner = rejectPartnerUseCase.execute(partnerId, "Documentação incompleta")

        // Assert
        assertEquals(PartnerStatus.REJECTED, rejectedPartner.status)
        assertEquals("Documentação incompleta", rejectedPartner.rejectionReason)
    }

    @Test
    fun `deve falhar ao rejeitar sem motivo`() = runTest {
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

        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    kotlinx.coroutines.runBlocking { rejectPartnerUseCase.execute(partnerId, "") }
                }
        assertEquals("Motivo da rejeição é obrigatório", exception.message)
    }

    @Test
    fun `deve falhar ao rejeitar parceiro não pendente`() = runTest {
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
        rejectPartnerUseCase.execute(partnerId, "Primeiro motivo") // Rejeita primeiro

        // Act & Assert
        val exception =
                assertThrows(IllegalStateException::class.java) {
                    kotlinx.coroutines.runBlocking {
                        rejectPartnerUseCase.execute(partnerId, "Segundo motivo")
                    }
                }
        assertEquals("Apenas parceiros pendentes podem ser rejeitados", exception.message)
    }
}
