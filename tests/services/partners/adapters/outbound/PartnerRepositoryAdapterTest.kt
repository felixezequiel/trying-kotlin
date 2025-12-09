import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.domain.DocumentType
import partners.domain.PartnerStatus
import partners.domain.valueObjects.CompanyName
import partners.domain.valueObjects.Document
import partners.infrastructure.persistence.DatabaseContext
import services.partners.TestHelpers

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
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Teste",
                                tradeName = "Teste LTDA",
                                document = "11222333000181"
                        )

                // Act
                val id = partnerRepository.add(partner)
                val found = partnerRepository.getById(id)

                // Assert
                assertNotNull(found)
                assertEquals("Empresa Teste", found?.companyName?.value)
        }

        @Test
        fun `deve buscar por userId`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 10L,
                                companyName = "Empresa do User 10",
                                document = "11222333000181"
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
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Teste",
                                document = "61695227000193"
                        )
                partnerRepository.add(partner)

                // Act
                val found =
                        partnerRepository.getByDocument(
                                Document.of("61695227000193", DocumentType.CNPJ)
                        )

                // Assert
                assertNotNull(found)
                assertEquals("61695227000193", found?.document?.value)
        }

        @Test
        fun `deve listar todos os parceiros`() = runTest {
                // Arrange
                val partner1 =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa 1",
                                document = "33000167000101",
                                email = "empresa1@email.com"
                        )
                val partner2 =
                        TestHelpers.createTestPartner(
                                userId = 2L,
                                companyName = "Empresa 2",
                                document = "60746948000112",
                                email = "empresa2@email.com"
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
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Pendente",
                                document = "33000167000101",
                                email = "empresa1@email.com",
                                status = PartnerStatus.PENDING
                        )
                val partner2 =
                        TestHelpers.createTestPartner(
                                userId = 2L,
                                companyName = "Empresa Aprovada",
                                document = "60746948000112",
                                email = "empresa2@email.com",
                                status = PartnerStatus.APPROVED
                        )
                partnerRepository.add(partner1)
                partnerRepository.add(partner2)

                // Act
                val pending = partnerRepository.getByStatus(PartnerStatus.PENDING)
                val approved = partnerRepository.getByStatus(PartnerStatus.APPROVED)

                // Assert
                assertEquals(1, pending.size)
                assertEquals("Empresa Pendente", pending[0].companyName.value)
                assertEquals(1, approved.size)
                assertEquals("Empresa Aprovada", approved[0].companyName.value)
        }

        @Test
        fun `deve atualizar parceiro`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Nome Original",
                                document = "11222333000181"
                        )
                val id = partnerRepository.add(partner)
                val updatedPartner = partner.copy(companyName = CompanyName.of("Nome Atualizado"))

                // Act
                val result = partnerRepository.update(updatedPartner)

                // Assert
                assertTrue(result)
                val found = partnerRepository.getById(id)
                assertEquals("Nome Atualizado", found?.companyName?.value)
        }

        @Test
        fun `deve deletar parceiro`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Teste",
                                document = "11222333000181"
                        )
                val id = partnerRepository.add(partner)

                // Act
                val result = partnerRepository.delete(id)

                // Assert
                assertTrue(result)
                assertNull(partnerRepository.getById(id))
        }
}
