package events.domain

import kotlinx.serialization.Serializable

@Serializable
enum class EventStatus {
    DRAFT, // Rascunho, não visível
    PUBLISHED, // Publicado, vendendo ingressos
    CANCELLED, // Cancelado
    FINISHED // Encerrado
}
