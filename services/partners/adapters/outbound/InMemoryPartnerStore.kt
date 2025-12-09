package partners.adapters.outbound

import java.time.Instant
import java.util.UUID
import partners.application.ports.outbound.IPartnerRepository
import partners.application.ports.outbound.IPartnerStore
import partners.application.ports.outbound.ITransactionManager
import partners.domain.Partner
import partners.domain.PartnerStatus
import partners.domain.valueObjects.Document

/**
 * Store em memória que gerencia partners e transações. Implementa IPartnerStore para garantir que
 * qualquer implementação (Postgres, etc.) tenha o mesmo contrato.
 */
class InMemoryPartnerStore : IPartnerStore {
    private val partners = mutableListOf<Partner>()

    override val repository: IPartnerRepository = InMemoryPartnerRepository()
    override val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()

    private inner class InMemoryPartnerRepository : IPartnerRepository {
        override suspend fun add(partner: Partner): UUID {
            val now = Instant.now()
            partners.add(partner.copy(createdAt = now, updatedAt = now))
            return partner.id
        }

        override suspend fun getById(id: UUID): Partner? {
            return partners.find { it.id == id }
        }

        override suspend fun getByUserId(userId: Long): Partner? {
            return partners.find { it.userId == userId }
        }

        override suspend fun getByDocument(document: Document): Partner? {
            return partners.find { it.document.value == document.value }
        }

        override suspend fun getAll(): List<Partner> {
            return partners.toList()
        }

        override suspend fun getByStatus(status: PartnerStatus): List<Partner> {
            return partners.filter { it.status == status }
        }

        override suspend fun update(partner: Partner): Boolean {
            val index = partners.indexOfFirst { it.id == partner.id }
            if (index == -1) return false
            partners[index] = partner.copy(updatedAt = Instant.now())
            return true
        }

        override suspend fun delete(id: UUID): Boolean {
            return partners.removeIf { it.id == id }
        }
    }

    private inner class InMemoryTransactionManagerImpl : ITransactionManager {
        override suspend fun <T> execute(block: suspend () -> T): T {
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
    }
}
