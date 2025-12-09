package tickets.domain

enum class TicketTypeStatus {
    ACTIVE, // Disponível para venda
    PAUSED, // Pausado temporariamente
    SOLD_OUT, // Esgotado
    INACTIVE // Desativado
}
