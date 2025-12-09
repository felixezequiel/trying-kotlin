package orders.application.useCases

import orders.application.ports.outbound.IUnitOfWork
import orders.domain.IssuedTicket

class GetIssuedTicketUseCase(private val unitOfWork: IUnitOfWork) {

    fun execute(code: String): IssuedTicket? {
        return unitOfWork.issuedTicketRepository.findByCode(code)
    }
}
