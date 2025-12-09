package partners.application.ports.outbound

import java.util.UUID
import partners.domain.Partner
import partners.domain.PartnerStatus

interface IPartnerRepository {
    suspend fun add(partner: Partner): UUID
    suspend fun getById(id: UUID): Partner?
    suspend fun getByUserId(userId: Long): Partner?
    suspend fun getByDocument(document: String): Partner?
    suspend fun getAll(): List<Partner>
    suspend fun getByStatus(status: PartnerStatus): List<Partner>
    suspend fun update(partner: Partner): Boolean
    suspend fun delete(id: UUID): Boolean
}
