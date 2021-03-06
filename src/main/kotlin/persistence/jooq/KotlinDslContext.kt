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
import java.sql.BatchUpdateException
import java.sql.SQLException

class KotlinDslContext(private val dslContext: DSLContext) {
    // Uses for overriding the dsl context during tests.
    var overrideDSLContext: DSLContext? = null

    /**
     * Starts a transaction that gets executed using the IO dispatcher.
     */
    suspend fun <T> transaction(
        isolationLevel: Int? = null,
        onRollback: suspend (DataAccessException) -> Unit = {},
        block: suspend (KotlinTransactionContext).() -> T
    ): T = withContext(Dispatchers.IO) {
        try {
            (if (overrideDSLContext != null) overrideDSLContext!! else dslContext).transactionResult { config ->
                val transactionDslContext = DSL.using(config)
                if (isolationLevel != null) {
                    transactionDslContext.connection { it.transactionIsolation = isolationLevel }
                }
                runBlocking { KotlinTransactionContext(transactionDslContext).block() }
            }
        } catch (e: DataAccessException) {
            onRollback(e)
            val cause = e.cause
            val psqlException: PSQLException = if (cause is PSQLException) {
                cause
            } else if (cause is BatchUpdateException && cause.cause is PSQLException) {
                cause.cause as PSQLException
            } else {
                throw cause ?: e
            }

            if (psqlException.isSerializationFailure()) {
                transaction(isolationLevel = isolationLevel, block = block)
            } else {
                handleGenericPostgresError(psqlException)
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
            isUniqueViolation() -> throw PlatformApiException.duplicate(
                serverErrorMessage?.detail?.extractKeyColumnNames()!!.first().snakeToCamelCase(),
                serverErrorMessage?.detail?.extractKeyColumnValues()!!.first()
            )
            isForeignKeyViolation() -> throw PlatformApiException.dependencyNotFound(
                serverErrorMessage?.detail?.extractKeyColumnNames()!!.first().snakeToCamelCase(),
                serverErrorMessage?.detail?.extractKeyColumnValues()!!.first()
            )
            else -> throw e
        }
    }
}


