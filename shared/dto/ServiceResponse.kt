package shared.dto

import kotlinx.serialization.Serializable

/**
 * Response padrão para comunicação entre BFF e serviços
 */
@Serializable
data class ServiceResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> success(data: T): ServiceResponse<T> = ServiceResponse(
            success = true,
            data = data
        )

        fun <T> error(message: String): ServiceResponse<T> = ServiceResponse(
            success = false,
            error = message
        )
    }
}
