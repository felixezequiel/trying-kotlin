import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.InMemoryPartnerStore
import partners.adapters.outbound.UnitOfWorkAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.ApprovePartnerUseCase
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.ListPartnersUseCase
import partners.domain.DocumentType
import partners.domain.PartnerStatus
import partners.mocks.MockUserGateway

class ListPartnersUseCaseTest {

        private lateinit var partnerStore: InMemoryPartnerStore
        private lateinit var unitOfWork: UnitOfWorkAdapter
        private lateinit var userGateway: MockUserGateway
        private lateinit var createPartnerUseCase: CreatePartnerUseCase
        private lateinit var approvePartnerUseCase: ApprovePartnerUseCase
        private lateinit var listPartnersUseCase: ListPartnersUseCase

        @BeforeEach
        fun setUp() {
                partnerStore = InMemoryPartnerStore()
                unitOfWork =
                        UnitOfWorkAdapter(partnerStore.repository, partnerStore.transactionManager)
                userGateway = MockUserGateway()
                createPartnerUseCase = CreatePartnerUseCase(unitOfWork, userGateway)
                approvePartnerUseCase = ApprovePartnerUseCase(unitOfWork)
                listPartnersUseCase = ListPartnersUseCase(unitOfWork)
        }

        @Test
        fun `deve listar todos os parceiros`() = runTest {
                // Arrange
                createPartnerUseCase.execute(
                        CreatePartnerRequest(
                                companyName = "Empresa 1",
                                tradeName = null,
                                document = "11222333000181",
                                documentType = DocumentType.CNPJ,
                                email = "empresa1@email.com",
                                phone = "11999999999"
                        )
                )
                createPartnerUseCase.execute(
                        CreatePartnerRequest(
                                companyName = "Empresa 2",
                                tradeName = null,
                                document = "61695227000193",
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
                                CreatePartnerRequest(
                                        companyName = "Empresa 1",
                                        tradeName = null,
                                        document = "11222333000181",
                                        documentType = DocumentType.CNPJ,
                                        email = "empresa1@email.com",
                                        phone = "11999999999"
                                )
                        )
                createPartnerUseCase.execute(
                        CreatePartnerRequest(
                                companyName = "Empresa 2",
                                tradeName = null,
                                document = "61695227000193",
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
                assertEquals("Empresa 2", pendingPartners[0].companyName.value)
                assertEquals(1, approvedPartners.size)
                assertEquals("Empresa 1", approvedPartners[0].companyName.value)
        }

        @Test
        fun `deve retornar lista vazia quando não há parceiros`() = runTest {
                // Act
                val partners = listPartnersUseCase.execute()

                // Assert
                assertTrue(partners.isEmpty())
        }
}
