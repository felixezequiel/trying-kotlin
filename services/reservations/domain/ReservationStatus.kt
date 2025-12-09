package reservations.domain

enum class ReservationStatus {
    ACTIVE, // Reserva ativa, ingressos bloqueados
    CANCELLED, // Cancelada, ingressos liberados
    CONVERTED // Convertida em Order
}
