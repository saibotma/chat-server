package persistence.postgres

import app.appella.persistence.postgres.*
import org.jooq.exception.DataAccessException
import org.postgresql.util.PSQLException
import util.Fallible

fun <T, R> T.catchPostgresExceptions(block: T.() -> R): Fallible<PSQLException, R> {
    return try {
        Fallible.Success(block())
    } catch (e: DataAccessException) {
        Fallible.Failure(e.cause as PSQLException)
    }
}

val PSQLException.constraintName: String?
    get() = serverErrorMessage?.constraint

/**
 * Returns whether the exception got thrown because of a foreign key violation.
 * [key] is the name of the column that violates the constraint.
 * In case of compound foreign keys you have to check every column of the foreign key
 * explicitly.
 * In case [key] is null only the state gets checked.
 */
fun PSQLException.isForeignKeyViolation(key: String? = null): Boolean {
    return sqlState == FOREIGN_KEY_VIOLATION && ifNotNull(key) { constraintName?.contains(it) ?: false }
}

/**
 * Returns whether the exception got thrown because of a unique key violation.
 * [key] is the name of either the unique or primary key constraint.
 * [columnName] is the name of a specific column of the key.
 * In case both [key] and [columnName] are null only the state gets checked.
 */
fun PSQLException.isUniqueViolation(key: String? = null, columnName: String? = null): Boolean {
    return sqlState == UNIQUE_VIOLATION
            && ifNotNull(key) { constraintName?.contains(it) ?: false }
            && ifNotNull(columnName) { serverErrorMessage?.detail?.extractKeyColumnNames()?.contains(it) ?: false }
}

/**
 * Returns whether the exception got thrown because of a check violation.
 * [key] is the name of the check constraint.
 * In case [key] is null only the state gets checked.
 */
fun PSQLException.isCheckViolation(key: String? = null): Boolean {
    return sqlState == CHECK_VIOLATION && ifNotNull(key) { constraintName?.equals(it) ?: false }
}

/**
 * Returns whether the exception got thrown because of an exception raised in plsql.
 * [message] is the message of the exception.
 */
fun PSQLException.isPlException(message: String): Boolean {
    return sqlState == RAISE_EXCEPTION && serverErrorMessage?.message?.equals(message) ?: false
}

fun PSQLException.isSerializationFailure(): Boolean {
    return sqlState == SERIALIZATION_FAILURE
}

/**
 * Extracts the column names of the key of a PSQLException detail server error message.
 */
fun String.extractKeyColumnNames(): Set<String> {
    return drop(5).split(")=").first().split(",").map { it.trim() }.toSet()
}

/**
 * Extracts the column values of the key of a PSQLException detail server error message.
 */
fun String.extractKeyColumnValues(): Set<String> {
    return split("=(")[1].split(") ")[0].split(",").map { it.trim() }.toSet()
}

private fun <T> ifNotNull(value: T?, elze: Boolean = true, block: (T) -> Boolean): Boolean {
    return if (value != null) {
        block(value)
    } else {
        elze
    }
}
