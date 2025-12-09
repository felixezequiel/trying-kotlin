package orders.application.useCases

import orders.application.ports.outbound.IIssuedTicketRepository
import orders.domain.IssuedTicket

class GetIssuedTicketUseCase(private val issuedTicketRepository: IIssuedTicketRepository) {

    fun execute(code: String): IssuedTicket? {
        return issuedTicketRepository.findByCode(code)
    }
}
