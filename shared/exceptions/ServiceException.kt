package shared.exceptions

/**
 * Exceção base para erros de serviço
 */
open class ServiceException(
    message: String,
    val statusCode: Int = 500,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Recurso não encontrado (404)
 */
class NotFoundException(
    message: String = "Resource not found"
) : ServiceException(message, 404)

/**
 * Requisição inválida (400)
 */
class BadRequestException(
    message: String = "Bad request"
) : ServiceException(message, 400)

/**
 * Conflito de dados (409)
 */
class ConflictException(
    message: String = "Conflict"
) : ServiceException(message, 409)

/**
 * Não autorizado (401)
 */
class UnauthorizedException(
    message: String = "Unauthorized"
) : ServiceException(message, 401)

/**
 * Acesso negado (403)
 */
class ForbiddenException(
    message: String = "Forbidden"
) : ServiceException(message, 403)
