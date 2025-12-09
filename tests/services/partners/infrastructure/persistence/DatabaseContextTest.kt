import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import partners.domain.PartnerStatus
import partners.domain.valueObjects.CompanyName
import partners.infrastructure.persistence.DatabaseContext
import services.partners.TestHelpers

class DatabaseContextTest {

        private lateinit var dbContext: DatabaseContext

        @BeforeEach
        fun setUp() {
                dbContext = DatabaseContext()
        }

        @Test
        fun `deve adicionar parceiro com sucesso`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Teste",
                                tradeName = "Teste LTDA",
                                document = "11222333000181"
                        )

                // Act
                val id = dbContext.addPartner(partner)

                // Assert
                assertNotNull(id)
                val found = dbContext.findById(id)
                assertNotNull(found)
                assertEquals("Empresa Teste", found?.companyName?.value)
        }

        @Test
        fun `deve encontrar parceiro por userId`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 5L,
                                companyName = "Empresa do User 5",
                                document = "11222333000181"
                        )
                dbContext.addPartner(partner)

                // Act
                val found = dbContext.findByUserId(5L)

                // Assert
                assertNotNull(found)
                assertEquals(5L, found?.userId)
        }

        @Test
        fun `deve encontrar parceiro por documento`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Teste",
                                document = "61695227000193"
                        )
                dbContext.addPartner(partner)

                // Act
                val found = dbContext.findByDocument("61695227000193")

                // Assert
                assertNotNull(found)
                assertEquals("61695227000193", found?.document?.value)
        }

        @Test
        fun `deve listar parceiros por status`() = runTest {
                // Arrange
                val partner1 =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa 1",
                                document = "33000167000101",
                                email = "empresa1@email.com",
                                status = PartnerStatus.PENDING
                        )
                val partner2 =
                        TestHelpers.createTestPartner(
                                userId = 2L,
                                companyName = "Empresa 2",
                                document = "60746948000112",
                                email = "empresa2@email.com",
                                status = PartnerStatus.APPROVED
                        )
                dbContext.addPartner(partner1)
                dbContext.addPartner(partner2)

                // Act
                val pendingPartners = dbContext.getPartnersByStatus(PartnerStatus.PENDING)
                val approvedPartners = dbContext.getPartnersByStatus(PartnerStatus.APPROVED)

                // Assert
                assertEquals(1, pendingPartners.size)
                assertEquals(1, approvedPartners.size)
        }

        @Test
        fun `deve atualizar parceiro com sucesso`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Nome Original",
                                document = "11222333000181"
                        )
                val id = dbContext.addPartner(partner)
                val updatedPartner = partner.copy(companyName = CompanyName.of("Nome Atualizado"))

                // Act
                val result = dbContext.updatePartner(updatedPartner)

                // Assert
                assertTrue(result)
                val found = dbContext.findById(id)
                assertEquals("Nome Atualizado", found?.companyName?.value)
        }

        @Test
        fun `deve deletar parceiro com sucesso`() = runTest {
                // Arrange
                val partner =
                        TestHelpers.createTestPartner(
                                userId = 1L,
                                companyName = "Empresa Teste",
                                document = "11222333000181"
                        )
                val id = dbContext.addPartner(partner)

                // Act
                val result = dbContext.deletePartner(id)

                // Assert
                assertTrue(result)
                assertNull(dbContext.findById(id))
        }
}
