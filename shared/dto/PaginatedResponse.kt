package shared.dto

import kotlinx.serialization.Serializable

/**
 * Response paginado para listagens
 */
@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
) {
    companion object {
        fun <T> of(items: List<T>, page: Int, pageSize: Int, totalItems: Long): PaginatedResponse<T> {
            val totalPages = if (totalItems == 0L) 0 else ((totalItems + pageSize - 1) / pageSize).toInt()
            return PaginatedResponse(
                items = items,
                page = page,
                pageSize = pageSize,
                totalItems = totalItems,
                totalPages = totalPages
            )
        }
    }
}
