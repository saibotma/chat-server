package app.appella.persistence.postgres

/**
 * From: https://www.postgresql.org/docs/12/errcodes-appendix.html
 */

const val FOREIGN_KEY_VIOLATION = "23503"
const val UNIQUE_VIOLATION = "23505"
const val CHECK_VIOLATION = "23514"
const val RAISE_EXCEPTION = "P0001"
const val SERIALIZATION_FAILURE = "40001"
