package persistence.jooq

import error.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.postgresql.util.PSQLException
import persistence.postgres.*
import util.snakeToCamelCase

class KotlinDslContext(private val dslContext: DSLContext) {
    /**
     * Starts a transaction that gets executed using the IO dispatcher.
     */
    suspend fun <T> transaction(
        isolationLevel: Int? = null,
        onRollback: suspend (DataAccessException) -> Unit = {},
        block: suspend (KotlinTransactionContext).() -> T
    ): T = withContext(Dispatchers.IO) {
        try {
            dslContext.transactionResult { config ->
                val transactionDslContext = DSL.using(config)
                if (isolationLevel != null) {
                    transactionDslContext.connection { it.transactionIsolation = isolationLevel }
                }
                runBlocking { KotlinTransactionContext(transactionDslContext).block() }
            }
        } catch (e: DataAccessException) {
            onRollback(e)
            val cause = e.cause
            if (cause is PSQLException) {
                if (cause.isSerializationFailure()) {
                    transaction(isolationLevel = isolationLevel, block = block)
                } else {
                    handleGenericPostgresError(cause)
                }
            } else {
                throw cause ?: e
            }
        }
    }
}

/**
 * Handles generic Postgres error that were not catched in the query function.
 */
private fun handleGenericPostgresError(e: PSQLException): Nothing {
    with(e) {
        when {
            isUniqueViolation() -> throw ApiException.duplicate(
                serverErrorMessage?.detail?.extractKeyColumnNames()!!.first().snakeToCamelCase(),
                serverErrorMessage?.detail?.extractKeyColumnValues()!!.first()
            )
            isForeignKeyViolation() -> throw ApiException.dependencyNotFound(
                serverErrorMessage?.detail?.extractKeyColumnNames()!!.first().snakeToCamelCase(),
                serverErrorMessage?.detail?.extractKeyColumnValues()!!.first()
            )
            else -> throw e
        }
    }
}


