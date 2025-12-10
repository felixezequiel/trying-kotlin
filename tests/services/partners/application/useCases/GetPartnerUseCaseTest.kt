import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.InMemoryPartnerStore
import partners.adapters.outbound.UnitOfWorkAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.GetPartnerUseCase
import partners.domain.DocumentType
import partners.mocks.MockUserGateway

class GetPartnerUseCaseTest {

    private lateinit var partnerStore: InMemoryPartnerStore
    private lateinit var unitOfWork: UnitOfWorkAdapter
    private lateinit var userGateway: MockUserGateway
    private lateinit var createPartnerUseCase: CreatePartnerUseCase
    private lateinit var getPartnerUseCase: GetPartnerUseCase

    @BeforeEach
    fun setUp() {
        partnerStore = InMemoryPartnerStore()
        unitOfWork = UnitOfWorkAdapter(partnerStore.repository, partnerStore.transactionManager)
        userGateway = MockUserGateway()
        createPartnerUseCase = CreatePartnerUseCase(unitOfWork, userGateway)
        getPartnerUseCase = GetPartnerUseCase(unitOfWork)
    }

    @Test
    fun `deve retornar parceiro por ID`() = runTest {
        // Arrange
        val request =
                CreatePartnerRequest(
                        companyName = "Empresa Teste",
                        tradeName = "Teste LTDA",
                        document = "11222333000181",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        val partnerId = createPartnerUseCase.execute(request)

        // Act
        val partner = getPartnerUseCase.execute(partnerId)

        // Assert
        assertNotNull(partner)
        assertEquals("Empresa Teste", partner?.companyName?.value)
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
                        companyName = "Empresa do User",
                        tradeName = null,
                        document = "11222333000181",
                        documentType = DocumentType.CNPJ,
                        email = "contato@empresa.com",
                        phone = "11999999999"
                )
        createPartnerUseCase.execute(request)

        // Act - userId é 1 pois é o primeiro email no mock
        val partner = getPartnerUseCase.executeByUserId(1L)

        // Assert
        assertNotNull(partner)
        assertEquals("Empresa do User", partner?.companyName?.value)
        assertEquals(1L, partner?.userId)
    }

    @Test
    fun `deve retornar null para userId sem parceiro`() = runTest {
        // Act
        val partner = getPartnerUseCase.executeByUserId(999L)

        // Assert
        assertNull(partner)
    }
}
