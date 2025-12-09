package reservations.domain

enum class CancellationType {
    BY_CUSTOMER, // Cliente cancelou
    BY_PARTNER, // Partner cancelou
    BY_ADMIN, // Admin cancelou
    EVENT_CANCELLED // Evento foi cancelado
}
