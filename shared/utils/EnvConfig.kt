package shared.utils

/**
 * Utilitário para leitura de variáveis de ambiente
 */
object EnvConfig {
    fun get(key: String, default: String = ""): String =
        System.getenv(key) ?: default

    fun getInt(key: String, default: Int = 0): Int =
        System.getenv(key)?.toIntOrNull() ?: default

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        System.getenv(key)?.toBoolean() ?: default

    fun require(key: String): String =
        System.getenv(key) ?: throw IllegalStateException("Environment variable $key is required")
}
