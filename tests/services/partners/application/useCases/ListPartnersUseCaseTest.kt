import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.ApprovePartnerUseCase
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.ListPartnersUseCase
import partners.domain.DocumentType
import partners.domain.PartnerStatus
import partners.infrastructure.persistence.DatabaseContext

class ListPartnersUseCaseTest {

    private lateinit var dbContext: DatabaseContext
    private lateinit var partnerRepository: PartnerRepositoryAdapter
    private lateinit var createPartnerUseCase: CreatePartnerUseCase
    private lateinit var approvePartnerUseCase: ApprovePartnerUseCase
    private lateinit var listPartnersUseCase: ListPartnersUseCase

    @BeforeEach
    fun setUp() {
        dbContext = DatabaseContext()
        partnerRepository = PartnerRepositoryAdapter(dbContext)
        createPartnerUseCase = CreatePartnerUseCase(partnerRepository)
        approvePartnerUseCase = ApprovePartnerUseCase(partnerRepository)
        listPartnersUseCase = ListPartnersUseCase(partnerRepository)
    }

    @Test
    fun `deve listar todos os parceiros`() = runTest {
        // Arrange
        createPartnerUseCase.execute(
                userId = 1L,
                request =
                        CreatePartnerRequest(
                                companyName = "Empresa 1",
                                tradeName = null,
                                document = "11111111111111",
                                documentType = DocumentType.CNPJ,
                                email = "empresa1@email.com",
                                phone = "11999999999"
                        )
        )
        createPartnerUseCase.execute(
                userId = 2L,
                request =
                        CreatePartnerRequest(
                                companyName = "Empresa 2",
                                tradeName = null,
                                document = "22222222222222",
                                documentType = DocumentType.CNPJ,
                                email = "empresa2@email.com",
                                phone = "11888888888"
                        )
        )

        // Act
        val partners = listPartnersUseCase.execute()

        // Assert
        assertEquals(2, partners.size)
    }

    @Test
    fun `deve listar parceiros por status`() = runTest {
        // Arrange
        val partnerId1 =
                createPartnerUseCase.execute(
                        userId = 1L,
                        request =
                                CreatePartnerRequest(
                                        companyName = "Empresa 1",
                                        tradeName = null,
                                        document = "11111111111111",
                                        documentType = DocumentType.CNPJ,
                                        email = "empresa1@email.com",
                                        phone = "11999999999"
                                )
                )
        createPartnerUseCase.execute(
                userId = 2L,
                request =
                        CreatePartnerRequest(
                                companyName = "Empresa 2",
                                tradeName = null,
                                document = "22222222222222",
                                documentType = DocumentType.CNPJ,
                                email = "empresa2@email.com",
                                phone = "11888888888"
                        )
        )
        approvePartnerUseCase.execute(partnerId1) // Aprova apenas o primeiro

        // Act
        val pendingPartners = listPartnersUseCase.executeByStatus(PartnerStatus.PENDING)
        val approvedPartners = listPartnersUseCase.executeByStatus(PartnerStatus.APPROVED)

        // Assert
        assertEquals(1, pendingPartners.size)
        assertEquals("Empresa 2", pendingPartners[0].companyName)
        assertEquals(1, approvedPartners.size)
        assertEquals("Empresa 1", approvedPartners[0].companyName)
    }

    @Test
    fun `deve retornar lista vazia quando não há parceiros`() = runTest {
        // Act
        val partners = listPartnersUseCase.execute()

        // Assert
        assertTrue(partners.isEmpty())
    }
}
