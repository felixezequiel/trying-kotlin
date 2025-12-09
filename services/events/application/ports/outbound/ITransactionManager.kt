package events.application.ports.outbound

interface ITransactionManager {
    suspend fun <T> execute(block: suspend () -> T): T
}
