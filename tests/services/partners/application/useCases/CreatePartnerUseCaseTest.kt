import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.InMemoryPartnerStore
import partners.adapters.outbound.UnitOfWorkAdapter
import partners.application.dto.CreatePartnerRequest
import partners.application.useCases.CreatePartnerUseCase
import partners.domain.DocumentType
import partners.domain.PartnerStatus
import partners.mocks.MockUserGateway

class CreatePartnerUseCaseTest {

        private lateinit var partnerStore: InMemoryPartnerStore
        private lateinit var unitOfWork: UnitOfWorkAdapter
        private lateinit var userGateway: MockUserGateway
        private lateinit var createPartnerUseCase: CreatePartnerUseCase

        @BeforeEach
        fun setUp() {
                partnerStore = InMemoryPartnerStore()
                unitOfWork =
                        UnitOfWorkAdapter(partnerStore.repository, partnerStore.transactionManager)
                userGateway = MockUserGateway()
                createPartnerUseCase = CreatePartnerUseCase(unitOfWork, userGateway)
        }

        @Test
        fun `deve criar parceiro com sucesso`() = runTest {
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

                // Act - userId agora é obtido automaticamente pelo email via userGateway
                val partnerId = createPartnerUseCase.execute(request)

                // Assert
                assertNotNull(partnerId)
                val partner = partnerStore.repository.getById(partnerId)
                assertNotNull(partner)
                assertEquals("Empresa Teste", partner?.companyName?.value)
                assertEquals("Teste LTDA", partner?.tradeName)
                assertEquals("11222333000181", partner?.document?.value)
                assertEquals(DocumentType.CNPJ, partner?.documentType)
                assertEquals(PartnerStatus.PENDING, partner?.status)
                assertEquals(1L, partner?.userId) // Mock retorna userId sequencial
        }

        @Test
        fun `deve criar parceiro com CPF`() = runTest {
                // Arrange
                val request =
                        CreatePartnerRequest(
                                companyName = "João Silva MEI",
                                tradeName = null,
                                document = "52998224725",
                                documentType = DocumentType.CPF,
                                email = "joao@email.com",
                                phone = "11988888888"
                        )

                // Act
                val partnerId = createPartnerUseCase.execute(request)

                // Assert
                val partner = partnerStore.repository.getById(partnerId)
                assertNotNull(partner)
                assertEquals(DocumentType.CPF, partner?.documentType)
                assertNull(partner?.tradeName)
        }

        @Test
        fun `deve falhar quando usuário já possui parceiro - mesmo email`() = runTest {
                // Arrange - Cria primeiro parceiro
                val request1 =
                        CreatePartnerRequest(
                                companyName = "Empresa 1",
                                tradeName = null,
                                document = "11222333000181",
                                documentType = DocumentType.CNPJ,
                                email = "mesma@email.com", // Mesmo email
                                phone = "11999999999"
                        )
                createPartnerUseCase.execute(request1)

                // Tenta criar segundo parceiro com mesmo email (mesmo userId)
                val request2 =
                        CreatePartnerRequest(
                                companyName = "Empresa 2",
                                tradeName = null,
                                document = "61695227000193",
                                documentType = DocumentType.CNPJ,
                                email = "mesma@email.com", // Mesmo email = mesmo userId
                                phone = "11888888888"
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalStateException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createPartnerUseCase.execute(request2)
                                }
                        }
                assertEquals("Usuário já possui um parceiro cadastrado", exception.message)
        }

        @Test
        fun `deve falhar quando documento já existe`() = runTest {
                // Arrange
                val request1 =
                        CreatePartnerRequest(
                                companyName = "Empresa 1",
                                tradeName = null,
                                document = "33000167000101",
                                documentType = DocumentType.CNPJ,
                                email = "empresa1@email.com",
                                phone = "11999999999"
                        )
                createPartnerUseCase.execute(request1)

                val request2 =
                        CreatePartnerRequest(
                                companyName = "Empresa 2",
                                tradeName = null,
                                document = "33000167000101", // Mesmo documento
                                documentType = DocumentType.CNPJ,
                                email = "empresa2@email.com", // Email diferente
                                phone = "11888888888"
                        )

                // Act & Assert
                val exception =
                        assertThrows(IllegalStateException::class.java) {
                                kotlinx.coroutines.runBlocking {
                                        createPartnerUseCase.execute(request2)
                                }
                        }
                assertEquals("Documento já cadastrado por outro parceiro", exception.message)
        }
}
