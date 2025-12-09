package partners.adapters.outbound

import java.util.UUID
import partners.application.ports.outbound.IPartnerRepository
import partners.domain.Partner
import partners.domain.PartnerStatus
import partners.domain.valueObjects.Document
import partners.infrastructure.persistence.DatabaseContext

class PartnerRepositoryAdapter(private val dbContext: DatabaseContext) : IPartnerRepository {

    override suspend fun add(partner: Partner): UUID {
        return dbContext.addPartner(partner)
    }

    override suspend fun getById(id: UUID): Partner? {
        return dbContext.findById(id)
    }

    override suspend fun getByUserId(userId: Long): Partner? {
        return dbContext.findByUserId(userId)
    }

    override suspend fun getByDocument(document: Document): Partner? {
        // Infrastructure faz a conversão para tipo primitivo
        return dbContext.findByDocument(document.value)
    }

    override suspend fun getAll(): List<Partner> {
        return dbContext.getAllPartners()
    }

    override suspend fun getByStatus(status: PartnerStatus): List<Partner> {
        return dbContext.getPartnersByStatus(status)
    }

    override suspend fun update(partner: Partner): Boolean {
        return dbContext.updatePartner(partner)
    }

    override suspend fun delete(id: UUID): Boolean {
        return dbContext.deletePartner(id)
    }
}
