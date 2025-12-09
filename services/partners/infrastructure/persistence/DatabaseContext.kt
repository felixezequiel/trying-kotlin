package partners.infrastructure.persistence

import java.time.Instant
import java.util.UUID
import partners.domain.Partner
import partners.domain.PartnerStatus

class DatabaseContext {
    private val partners = mutableListOf<Partner>()

    suspend fun <T> executeTransaction(block: suspend () -> T): T {
        println("Iniciando transação...")
        val snapshot = partners.toList()
        try {
            val result = block()
            println("Transação concluída com sucesso (commit).")
            return result
        } catch (e: Exception) {
            println("Transação falhou (rollback): ${e.message}")
            partners.clear()
            partners.addAll(snapshot)
            throw e
        }
    }

    fun addPartner(partner: Partner): UUID {
        val now = Instant.now()
        partners.add(partner.copy(createdAt = now, updatedAt = now))
        return partner.id
    }

    fun findById(id: UUID): Partner? {
        return partners.find { it.id == id }
    }

    fun findByUserId(userId: Long): Partner? {
        return partners.find { it.userId == userId }
    }

    fun findByDocument(document: String): Partner? {
        return partners.find { it.document.value == document }
    }

    fun getAllPartners(): List<Partner> {
        return partners.toList()
    }

    fun getPartnersByStatus(status: PartnerStatus): List<Partner> {
        return partners.filter { it.status == status }
    }

    fun updatePartner(partner: Partner): Boolean {
        val index = partners.indexOfFirst { it.id == partner.id }
        if (index == -1) return false
        partners[index] = partner.copy(updatedAt = Instant.now())
        return true
    }

    fun deletePartner(id: UUID): Boolean {
        return partners.removeIf { it.id == id }
    }
}
