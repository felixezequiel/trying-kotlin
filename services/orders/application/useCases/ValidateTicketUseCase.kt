package orders.application.useCases

import orders.application.ports.outbound.IIssuedTicketRepository
import orders.domain.IssuedTicket
import orders.domain.TicketStatus

class ValidateTicketUseCase(private val issuedTicketRepository: IIssuedTicketRepository) {

    fun execute(code: String): IssuedTicket {
        // Busca o ingresso pelo código
        val ticket =
                issuedTicketRepository.findByCode(code)
                        ?: throw IllegalArgumentException("Ingresso não encontrado")

        // Valida status
        when (ticket.status) {
            TicketStatus.USED -> throw IllegalStateException("Ingresso já utilizado")
            TicketStatus.CANCELLED -> throw IllegalStateException("Ingresso cancelado")
            TicketStatus.VALID -> {
                // Marca como usado
                val usedTicket = ticket.use()
                return issuedTicketRepository.update(usedTicket)
            }
        }
    }
}
