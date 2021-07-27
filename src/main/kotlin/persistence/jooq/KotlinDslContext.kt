package persistence.jooq

import app.appella.error.*
import app.appella.extensions.*
import app.appella.persistence.jooq.KotlinTransactionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.postgresql.util.PSQLException

// TODO adjust naming
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
            isCheckViolation("temporal_updated_at_not_before_created_at_check") -> {
                throw ApiException.updatedAtBeforeCreatedAt()
            }
            isCheckViolation("lesson_invalid_lock_integrity") -> {
                throw ApiException.lessonIsLockedByAccountButNotByTeacher()
            }
            isPlException("lesson_student_attendance_not_set_but_lesson_locked") -> {
                throw ApiException.lessonIsLocked()
            }
            else -> throw e
        }
    }
}


