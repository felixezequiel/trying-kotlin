package orders.application.useCases

import orders.application.ports.outbound.IUnitOfWork
import orders.domain.IssuedTicket
import orders.domain.TicketStatus

class ValidateTicketUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(code: String): IssuedTicket {
        return unitOfWork.runInTransaction {
            // Busca o ingresso pelo código
            val ticket =
                    unitOfWork.issuedTicketRepository.findByCode(code)
                            ?: throw IllegalArgumentException("Ingresso não encontrado")

            // Valida status
            when (ticket.status) {
                TicketStatus.USED -> throw IllegalStateException("Ingresso já utilizado")
                TicketStatus.CANCELLED -> throw IllegalStateException("Ingresso cancelado")
                TicketStatus.VALID -> {
                    // Marca como usado
                    val usedTicket = ticket.use()
                    unitOfWork.issuedTicketRepository.update(usedTicket)
                }
            }
        }
    }
}
